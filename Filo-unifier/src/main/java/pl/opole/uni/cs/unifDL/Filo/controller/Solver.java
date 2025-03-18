package pl.opole.uni.cs.unifDL.Filo.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.logging.Level;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import pl.opole.uni.cs.unifDL.Filo.model.GenericGoal;
import pl.opole.uni.cs.unifDL.Filo.model.Goal;
import pl.opole.uni.cs.unifDL.Filo.model.Model;
import pl.opole.uni.cs.unifDL.Filo.model.Solution;
import pl.opole.uni.cs.unifDL.Filo.renderer.ShortFormProvider;
import pl.opole.uni.cs.unifDL.Filo.view.Renderer;

/*
 * Solving engine
 * 
 * @author: Barbara Morawska
 * @author: Dariusz Marzec
 * 
 * 
 */

public class Solver {

	private static OWLClass owlThingAlias = null;
	private AtomManager atomManager;
	private Model model;
	private Renderer renderer;

	private ShortcutsManager shmanager;
	private Solution solution;
	public Boolean solved = false;
	private String message;
	private String result;

	
	private int numberOfConstants = 0;
	private int numberOfUserVariables = 0;
	private static int maxNumberOfComputationVariables = 0;
	public static int numberOfDecidedDuringPreprocessing = 0;
	private static int numberOfDecidedByComputingShortcuts = 0;
	private static long solvingTime = 0;

	public Solution getSolution() {
		return solution;
	}

	public AtomManager getAtomManager() {
		return atomManager;
	}

	public static long getSolvingTime() {
		return solvingTime;
	}

	public String getMessage() {
		return message;
	}

	public int getNumberOfConstants() {
		return numberOfConstants;
	}

	public int getNumberOfUserVariables() {
		return numberOfUserVariables;
	}

	public int getMaxNumberOfComputationVariables() {
		return maxNumberOfComputationVariables;
	}

	public int getNumberOfDecidedDuringPreprocessing() {
		return numberOfDecidedDuringPreprocessing;
	}

	public int getNumberOfDecidedByComputingShortcuts() {
		return numberOfDecidedByComputingShortcuts;
	}

	public String getResult() {
		return result;
	}

	private void resetSolver() {
		atomManager = null;
		model = null;
		renderer = null;
		shmanager = null;
		solution = null;
		message = null;
		result = null;
		resetStatistics();
		SharedData.setRunFlag(true);
	}

	private void resetStatistics() {
		numberOfConstants = 0;
		numberOfUserVariables = 0;
		maxNumberOfComputationVariables = 0;
		numberOfDecidedDuringPreprocessing = 0;
		numberOfDecidedByComputingShortcuts = 0;
		solvingTime = 0;
	}

	public boolean ini(File mainFilename) {

		resetSolver();
		long startTime = System.nanoTime();
		boolean ret = true;

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

		FiloLogger.log(Level.INFO, "Solver : Input file " + mainFilename);

		OWLOntology ontology = loadOntology(mainFilename.getPath(), manager);

		if (ontology == null) {
			FiloLogger.log(Level.WARNING, "Solver : Ontology is null");
			ret = false;
			return ret;
		} else {
			FiloLogger.log(Level.INFO, "Ontology successfully loaded");
		}
		ShortFormProvider provider = new ShortFormProvider(manager);
		ModelProvider filoModel = new ModelProvider(provider);

		filoModel.setupModel(ontology, owlThingAlias, false);

		FiloLogger.log(Level.INFO, "First flattening done");
		FiloLogger.log(Level.FINE, "Solver : Printing model with system variables:");
		FiloLogger.log(Level.FINE, " Solver : ModelProvider : \n" + filoModel.printModel(true));

		model = filoModel.getModel();
		atomManager = model.getAtomManager();
		renderer = new Renderer(atomManager, provider);

		message = filoModel.printModel(false);

		if (solution != null) {
			solution = null;
			FiloLogger.log(Level.FINE, "Solution reset to null");
		}

		// Stats
		numberOfConstants = atomManager.getConstants().size();
		FiloLogger.log(Level.FINE, "Solver.ini: Number of constants: " + numberOfConstants);
		numberOfUserVariables = atomManager.getUserVariables().size();
		FiloLogger.log(Level.FINE, "Solver.ini: Number of user variables: " + numberOfUserVariables);

		long endTime = System.nanoTime();

		solvingTime = solvingTime + (endTime - startTime);

		return ret;

	}

	public boolean solve1() {
		if (SharedData.getRunFlag()) {
			long startTime = System.nanoTime();
			result = "";
			///////////// normalization ///////////////////

			FiloLogger.log(Level.FINE,
					"Solver : Normalization is done as part of setting up the generic goal in ModelReader");

			FiloLogger.log(Level.FINE, "Solver : Flattening II is done in setting up generic goal.\n "
					+ "Next goal provider is created and it is setting up the generic goal from the model.");

			///////////////// SOLVER////////////////////
			// ini has already instantiated model and atomManager

			GoalProvider goalprovider = new GoalProvider(model, atomManager);

			if (GoalProvider.getSuccess()) {
				numberOfDecidedDuringPreprocessing++;
				FiloLogger.log(Level.INFO,
						"Solver : There are no constants, the problem is unifiable with the empty solution");
				result = "The problem has no constants. The problem is unifiable with the empty solution.";
				solved = true;
				solution = new Solution(atomManager);
			} else {
				solved = true;

				for (Integer constant : atomManager.getConstants()) {
					if (solved && SharedData.getRunFlag()) {
						FiloLogger.log(Level.INFO, "Solver : Checking unifiability for constant "
								+ renderer.getShortForm(atomManager.printConceptName(constant)));
						solved = false;

						////////////// GENERIC GOAL/////////////
						GenericGoal gengoal = goalprovider.setupGenericGoal(atomManager, constant);
						FiloLogger.log(Level.INFO, "Full flattening done (generic goal)");
						FiloLogger.log(Level.FINE, "Solver : Printing generic goal:\n");
						gengoal.print(atomManager);
						int nbrVar = atomManager.getVariables().size();
						FiloLogger.log(Level.FINE, "Solver : Generic goal has " + nbrVar + " variables ");
						int nbrChoices = (int) Math.pow(3, nbrVar);
						FiloLogger.log(Level.FINE, "Solver : There are " + nbrChoices + " choices to check");

						if (maxNumberOfComputationVariables < atomManager.getVariables().size()) {
							maxNumberOfComputationVariables = atomManager.getVariables().size();
						}
						// CHOICE//////////

						// Define this in Choice
						new Choice(atomManager);
						Choice.fixChoice(gengoal.getFlatSubsumptions(), atomManager, constant, true);

						nbrVar = Choice.choiceTable.size();
						int nbrBin = Choice.binaryChoiceTable.size();

						FiloLogger.log(Level.FINE, "Solver : After fixing choice for some variables there are " + nbrVar
								+ " variables for ternary choice  and " + nbrBin + " variables for binary choice.");
						FiloLogger.log(Level.FINE, "Solver : There are "
								+ (int) (Math.pow(3, nbrVar) * Math.pow(2, nbrBin)) + " choices to check ");
						Choice.printChoice(atomManager);
						Choice.printBinaryChoice(atomManager);
						Choice.printFixedChoice(atomManager);

						Goal goal = null;
						Solution solutionA = new Solution(atomManager);

						if (Choice.isConsistent(atomManager)) {
 
							solved = processFirstChoice(goalprovider, gengoal, solutionA);
						}

						if (!solved && SharedData.getRunFlag()) {

							///////////////// If first choice is not consistent////////////////////
							int check = 0;
							boolean termination = false;
							///////////////// NO RECURSION ///////////////////
							while (check >=0) {
								check = Choice.nextChoiceReversed(atomManager, SharedData.getRunFlag());
								if (check > 0 && SharedData.getRunFlag()) {

									goal = goalprovider.setupGoal(gengoal, atomManager);
									if (goal != null && GoalProvider.getSuccess()) {
										numberOfDecidedDuringPreprocessing++;
										FiloLogger.log(Level.FINE,
												"Solver : SUCCESS!\nAll goal subsumptions have been solved by Implicit Solver.\nThe goal is unifiable");
										solutionA.addToSolution(atomManager);
										if (solution == null)
											solution = solutionA;
										else {
											solutionA.merge(solution);
											solution = solutionA;
										}
										solved = true;
										break;
									} else {
										if (goal != null && SharedData.getRunFlag()) {
											numberOfDecidedByComputingShortcuts++;
											termination = computeWithShortcuts(goal);

											// print current choice
											FiloLogger.log(Level.FINE, "Solver : Current choice: ");
											Choice.printChoice(atomManager);
											Choice.printBinaryChoice(atomManager);
											Choice.printFixedChoice(atomManager);

											if (termination) {

												FiloLogger.log(Level.FINE, "Solver : SUCCESS!!!");
												solved = true;
												break;
											} else if (shmanager.getChange() == 0) {
												FiloLogger.log(Level.FINE, "Solver : NO UNIFIER FOR THIS CHOICE");
											}
										} else if (goal == null)
											numberOfDecidedDuringPreprocessing++;
									}
								}
							}

							if (check == -1) {
								numberOfDecidedDuringPreprocessing++;
								FiloLogger.log(Level.FINE, "Solver : Choices checked: " + Choice.choiceCounter);
								FiloLogger.log(Level.FINE, "Solver : Consistent choices: " + Choice.consChoiceCounter);
								FiloLogger.log(Level.FINE, "Solver : Goals checked: " + GoalProvider.goalcounter);
								FiloLogger.log(Level.FINE, "Solver : The problem is not unifiable");
								result = "The problem is not unifiable. (Choices exhausted) FILO failed for constant "
										+ renderer.getShortForm(atomManager.printConceptName(constant));
								solved = false;
							} else if(check == -2) {
									FiloLogger.log(Level.FINE, "Process terminated by user");
									result = "Process terminated by user";
									solved = false;
								
							} else if (termination) {
								FiloLogger.log(Level.FINE, "Solver : Choices checked: " + Choice.choiceCounter);
								FiloLogger.log(Level.FINE, "Solver : Consistent choices: " + Choice.consChoiceCounter);
								FiloLogger.log(Level.FINE, "Solver : Goals checked: " + GoalProvider.goalcounter);
								FiloLogger.log(Level.FINE, "Solver : The problem is  unifiable for constant "
										+ renderer.getShortForm(atomManager.printConceptName(constant)));
							}

						}

						////////////////////// Merging solution for a given constant////////////////

						if (solved) {

							if (shmanager != null) {
								solutionA.addToSolution(shmanager, atomManager);
							}
							if (solution == null)
								solution = solutionA;
							else {
								solutionA.merge(solution);
								solution = solutionA;
							}

						}
					}

					if(SharedData.getRunFlag()) {
					if (!solved) {
						FiloLogger.log(Level.INFO, "Solver : Filo failed for constant "
								+ renderer.getShortForm(atomManager.printConceptName(constant)));
						result = "The problem is not unifiable. FILO failed for constant "
								+ renderer.getShortForm(atomManager.printConceptName(constant));
						break;
					} else {
						result = "The problem is unifiable";
					}

				}}

			}

			if (solved) {
				solution.finalize(atomManager);
				result = result + solution.print(atomManager);
			}
			long endTime = System.nanoTime();

			solvingTime = solvingTime + (endTime - startTime);

			printStats();
		}

		if (!SharedData.getRunFlag()) {
			result = "Process terminated by user";
			resetStatistics();
			FiloLogger.log(Level.INFO, result);
		}

		return solved;
	}

	private boolean processFirstChoice(GoalProvider goalprovider, GenericGoal gengoal, Solution solutionA) {

		ImplicitSolver.resetImplicitSolver();
		Goal goal = goalprovider.setupGoal(gengoal, atomManager);
		FiloLogger.log(Level.FINE, "Printing choice after the goal is set");
		Choice.printChoice(atomManager);
		Choice.printBinaryChoice(atomManager);
		Choice.printFixedChoice(atomManager);

		if (goal != null && GoalProvider.getSuccess()) {
			numberOfDecidedDuringPreprocessing++;
			FiloLogger.log(Level.FINE,
					"Solver : IMMEDIATE SUCCESS!\nAll goal subsumptions has been solved by Implicit Solver.\nThe goal is unifiable");

			solved = true;
		} else if (goal != null && !GoalProvider.getSuccess()) {
			numberOfDecidedByComputingShortcuts++;
			solved = computeWithShortcuts(goal);

		} else if (goal == null) {
			numberOfDecidedDuringPreprocessing++;
			FiloLogger.log(Level.FINE,"The goal is null, rejected by Implicit Solver");
			solved = false;

		}

		if (solved) {
			solutionA.addToSolution(atomManager);
			if (solution == null)
				solution = solutionA;
			else {
				solutionA.merge(solution);
				solution = solutionA;
			}
		}
		return solved;

	}

	private boolean computeWithShortcuts(Goal goal) {
		boolean termination = false;
		shmanager = new ShortcutsManager(atomManager, goal);

		do {

			termination = shmanager.NextShortcuts(goal.getFlatSubsumptions(), atomManager);
		} while (shmanager.getChange() > 0 && !termination && SharedData.getRunFlag());
		return termination;
	}

	private static OWLOntology loadOntology(String filename, OWLOntologyManager manager) {
		try {
			if (filename.isEmpty()) {
				FiloLogger.log(Level.FINE, "Solver : Ontology is null");
				return manager.createOntology();
			}

			InputStream input = new FileInputStream(new File(filename));
			return manager.loadOntologyFromOntologyDocument(input);
		} catch (FileNotFoundException e) {
			System.err.println("Solver : Could not find file '" + filename + "'.");
			return null;
		} catch (OWLOntologyCreationException e) {
			System.err.println("Solver : Could not create ontology from file '" + filename + "'.");
			return null;
		}
	}

	public void printStats() {

		FiloLogger.log(Level.INFO, "Number of constants: " + numberOfConstants);
		FiloLogger.log(Level.INFO, "Number of user variables: " + numberOfUserVariables);
		FiloLogger.log(Level.INFO,
				"The maximal number of variables during computation: " + maxNumberOfComputationVariables);
		FiloLogger.log(Level.INFO,
				"Number of attempts decided by preprocessing: " + numberOfDecidedDuringPreprocessing);
		FiloLogger.log(Level.INFO,
				"Number of attempts decided by computing shortcuts: " + numberOfDecidedByComputingShortcuts);
		FiloLogger.log(Level.INFO, "Solving time: " + solvingTime + " ns");
		// if(numberOfDecidedDuringPreprocessing == 0 &&
		// numberOfDecidedByComputingShortcuts == 0)
		// FiloLogger.log(Level.INFO, "There was no consistent choices for the
		// variables" );

	}

}
