package pl.opole.uni.cs.unifDL.Filo.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import pl.opole.uni.cs.unifDL.Filo.model.Atom;
import pl.opole.uni.cs.unifDL.Filo.model.GoalSubsumption;
import pl.opole.uni.cs.unifDL.Filo.model.Variable;
import pl.opole.uni.cs.unifDL.Filo.renderer.ShortFormProvider;
import pl.opole.uni.cs.unifDL.Filo.view.Renderer;

public class Choice {

	// change to a binary number of length variables.size
	// cannot change because have to access variable choice by its atomId
	// 0 = top
	// 1 = constant
	// 2 - no bottom, no constant
	// lex order on 0<1<2.
	// For example for 3 variables: 000, 001, 002, 010,011, 012, 020, 021, 022, 100,
	// 101, 102, 110, 111, 112, 120, 121, 122, 200, 201, 202, 220, 221, 222
	// In fact we use reversed lex order: 000, 100, 200, ...

	public static final int TOP = 0;
	public static final int CONSTANT = 1;
	public static final int NOTHING = 2;
	public static int consChoiceCounter = 0;
	public static int choiceCounter = 0;
	public static Map<Integer, Integer> choiceTable = new HashMap<>();
	static Map<Integer, Integer> fixedChoiceTable = new HashMap<>();
	// binary choice for some variables
	public static Map<Integer, Integer> binaryChoiceTable = new HashMap<>();
	public static HashSet<Integer> binaryChoiceExcludesNothing = new HashSet<>();
	public static HashSet<Integer> binaryChoiceExcludesConstant = new HashSet<>();
	public static HashSet<Integer> binaryChoiceExcludesTop = new HashSet<>();

	// Initial Choice is always consistent
	public Choice(AtomManager manager) {
		choiceCounter = 0;
		consChoiceCounter = 0;
		choiceTable = new HashMap<>();
		fixedChoiceTable = new HashMap<>();
		binaryChoiceTable = new HashMap<>();
		binaryChoiceExcludesNothing = new HashSet<>();
		binaryChoiceExcludesConstant = new HashSet<>();
		binaryChoiceExcludesTop = new HashSet<>();

		for (Integer id : manager.getVariables()) {
			choiceTable.put(id, TOP);
		}

	}

	public static Integer getChoice(Integer variable) {
		if (choiceTable.containsKey(variable))
			return choiceTable.get(variable);
		else if (fixedChoiceTable.containsKey(variable))
			return fixedChoiceTable.get(variable);
		else if (binaryChoiceTable.containsKey(variable) && binaryChoiceExcludesNothing.contains(variable))
			return binaryChoiceTable.get(variable); // we are done with NOTHING excluded
		else if (binaryChoiceTable.containsKey(variable) && binaryChoiceExcludesConstant.contains(variable))
			return (2 * binaryChoiceTable.get(variable)); // in binaryChoiceTable: 0=TOP, 1=NOTHING
		else if (binaryChoiceTable.containsKey(variable) && binaryChoiceExcludesTop.contains(variable))
			return (1 + binaryChoiceTable.get(variable)); // in binaryChoiceTable: 0=CONSTANT, 1=NOTHING
		else {
			FiloLogger.log(Level.WARNING, "Choice not defined for the variable " + variable);

			return null;
		}
	}

	public static void newChoice(Integer variable) {
		choiceTable.put(variable, TOP);
	}

	public static void setChoice(Integer variable, Integer val) {
		if (val == TOP || val == CONSTANT || val == NOTHING)
			choiceTable.put(variable, val);
	}

	public static void fixChoice(Integer variable, Integer val) {
		if (choiceTable.containsKey(variable)) {
			if (val == TOP || val == CONSTANT || val == NOTHING) {
				fixedChoiceTable.put(variable, val);
				choiceTable.remove(variable);
			}
		} else if (binaryChoiceTable.containsKey(variable)) {
			if (val == TOP || val == CONSTANT || val == NOTHING) {
				fixedChoiceTable.put(variable, val);
				binaryChoiceTable.remove(variable);
				if (binaryChoiceExcludesNothing.contains(variable))
					binaryChoiceExcludesNothing.remove(variable);
				else if (binaryChoiceExcludesConstant.contains(variable))
					binaryChoiceExcludesConstant.remove(variable);
				else if (binaryChoiceExcludesTop.contains(variable))
					binaryChoiceExcludesTop.remove(variable);
			}
		}
	}

	public static void setBinaryChoice(Integer variable, Integer val, Integer exclude) {
		if (choiceTable.containsKey(variable)) {
			if (val == TOP || val == CONSTANT || val == NOTHING) {
				binaryChoiceTable.put(variable, val);
				choiceTable.remove(variable);
			}
			if (exclude == CONSTANT) {
				binaryChoiceExcludesConstant.add(variable);
			} else if (exclude == TOP) {
				binaryChoiceExcludesTop.add(variable);
			} else if (exclude == NOTHING)
				binaryChoiceExcludesNothing.add(variable);
		}
	}

	public static String printChoice() {
		return choiceTable.toString();
	}

	public static String printFixedChoice() {
		return fixedChoiceTable.toString();
	}

	public static String printBinaryChoice() {
		return binaryChoiceTable.toString();
	}

	public static void printBinaryChoiceVariables(AtomManager manager) throws SecurityException, IOException {

		OWLOntologyManager ontmanager = OWLManager.createOWLOntologyManager();
		Renderer renderer = new Renderer(manager, new ShortFormProvider(ontmanager));
		String FINErmation = "Variables with no NOTHING: [ ";
		for (Integer id : binaryChoiceExcludesNothing) {
			FINErmation = FINErmation + renderer.getShortForm(manager.printConceptName(id)) + ",";
		}
		FINErmation += " ]\n";
		FINErmation += "Variables with no CONSTANT: [ ";
		for (Integer id : binaryChoiceExcludesConstant) {
			FINErmation = FINErmation + renderer.getShortForm(manager.printConceptName(id)) + ",";
		}
		FINErmation += " ]\n";
		FINErmation += " Variables with no TOP: [";
		for (Integer id : binaryChoiceExcludesTop) {
			FINErmation = FINErmation + renderer.getShortForm(manager.printConceptName(id)) + ",";
		}
		FINErmation += " ]\n";
		FiloLogger.log(Level.FINE, "Choice : " + FINErmation);

	}

	public static void printChoice(AtomManager manager) {
		OWLOntologyManager ontmanager = OWLManager.createOWLOntologyManager();
		Renderer renderer = new Renderer(manager, new ShortFormProvider(ontmanager));
		String FINErmation = "";
		for (Integer id : choiceTable.keySet()) {
			FINErmation = FINErmation + "[" + renderer.getShortForm(manager.printConceptName(id)) + " = "
					+ choiceTable.get(id) + "]";
		}
		FiloLogger.log(Level.FINE, "Choice : " + FINErmation);

	}

	public static void printFixedChoice(AtomManager manager) {
		OWLOntologyManager ontmanager = OWLManager.createOWLOntologyManager();
		Renderer renderer = new Renderer(manager, new ShortFormProvider(ontmanager));
		String FINErmation = "";
		for (Integer id : fixedChoiceTable.keySet()) {
			FINErmation = FINErmation + "[" + renderer.getShortForm(manager.printConceptName(id)) + " = "
					+ fixedChoiceTable.get(id) + "]";
		}
		FiloLogger.log(Level.FINE, "Fixed choice : " + FINErmation);

	}

	public static void printBinaryChoice(AtomManager manager) {
		OWLOntologyManager ontmanager = OWLManager.createOWLOntologyManager();
		Renderer renderer = new Renderer(manager, new ShortFormProvider(ontmanager));
		String FINErmation = "";
		for (Integer id : binaryChoiceTable.keySet()) {
			if (binaryChoiceExcludesConstant.contains(id))
				FINErmation = FINErmation + "[" + renderer.getShortForm(manager.printConceptName(id)) + " = "
						+ (binaryChoiceTable.get(id) * 2) + "]";
			else if (binaryChoiceExcludesTop.contains(id))
				FINErmation = FINErmation + "[" + renderer.getShortForm(manager.printConceptName(id)) + " = "
						+ (binaryChoiceTable.get(id) + 1) + "]";
			else if (binaryChoiceExcludesNothing.contains(id))
				FINErmation = FINErmation + "[" + renderer.getShortForm(manager.printConceptName(id)) + " = "
						+ (binaryChoiceTable.get(id)) + "]";
		}
		FiloLogger.log(Level.FINE, "Binary choice : " + FINErmation);

	}

	static boolean isConsistent(AtomManager manager) {

		for (Integer id : manager.getDecompositionVariables()) {

			if (getChoice(id) != TOP && getChoice(manager.getDecompositionVariable(id).getParent()) == TOP) {
				FiloLogger.log(Level.FINE,
						"Choice " + choiceCounter + " Decomposition variable is not top, but parent is top");
				return false;
//			} else if (manager.getConstantDecompositionVariables().contains(id) && getChoice(id) == NOTHING) {
//				FiloLogger.log(Level.FINE, "Choice " + choiceCounter
//						+ " ConstantDecVariable is nothing in choice (should be TOP or CONSTANT)");
//				return false;

			} else if (manager.getConstantDecompositionVariables().contains(id) && getChoice(id) == CONSTANT
					&& getChoice(manager.getDecompositionVariable(id).getParent()) != CONSTANT) {
				FiloLogger.log(Level.FINE,
						"Choice " + choiceCounter + " The choice for ConstantDecVariable does not agree with parent.");
				return false;

			} else if (manager.getConstantDecompositionVariables().contains(id)
					&& getChoice(manager.getDecompositionVariable(id).getParent()) == CONSTANT
					&& getChoice(id) != CONSTANT) {
				FiloLogger.log(Level.FINE,
						"Choice " + choiceCounter + " The choice for ConstantDecVariable does not agree with parent.");
				return false;

			}
		}
		return true;

	}

	public static void fixChoice(Set<GoalSubsumption> flatsubsumptions, AtomManager manager, Integer constant,
			boolean ruleBased) {

		boolean isChanged = true;

		while (isChanged) {
			isChanged = false;

			// if the left side is TOP, the variable on the right side must be TOP
			for (GoalSubsumption sub : flatsubsumptions) {
				if (sub.getLeft().isEmpty()) {
					for (Integer id : sub.getRight()) {
						if (manager.getVariables().contains(id) && !fixedChoiceTable.containsKey(id)) {
							isChanged = true;
							fixChoice(id, Choice.TOP);
							FiloLogger.log(Level.FINE, "Choice : Fixing choice for a subsumption with empty side left");
						}
					}
				}
			}

			// rules for Choices
			if (ruleBased) {
				// if ParentVariable is fixed and TOP, then all its DecompositionVariabes are
				// fixed and TOP
				for (Integer variable : manager.getVariables()) {
					if (fixedChoiceTable.containsKey(variable) && getChoice(variable) == TOP) {
						for (Integer decVar : manager.getDecompositionVariables()) {
							if (manager.getDecompositionVariable(decVar).getParent() == variable) {
								if (!fixedChoiceTable.containsKey(decVar)) {
									isChanged = true;
									fixChoice(decVar, TOP);
									FiloLogger.log(Level.FINE, "Choice : Fixing choice for all DecVars");
								}
							}
						}
					}
				}

				// if ParentVariable is fixed and not CONSTANT, then all its
				// ConstantDecompositionVariabe is fixed and TOP
				for (Integer variable : manager.getVariables()) {
					if (fixedChoiceTable.containsKey(variable) && getChoice(variable) != CONSTANT) {
						for (Integer decVar : manager.getConstantDecompositionVariables()) {
							if (manager.getDecompositionVariable(decVar).getParent() == variable) {
								if (!fixedChoiceTable.containsKey(decVar)) {
									isChanged = true;
									fixChoice(decVar, TOP);
									FiloLogger.log(Level.FINE, "Choice : Fixing choice for ConstDecVars");
								}
							}
						}
					}
				}

				// if ConstantDecompositionVariable is fixed and TOP, then its ParentVariable
				// does not contain CONSTANT
				for (Integer variable : manager.getConstantDecompositionVariables()) {
					if (fixedChoiceTable.containsKey(variable) && getChoice(variable) == TOP) {
						Integer parent = manager.getDecompositionVariable(variable).getParent();
						if (choiceTable.containsKey(parent)) {
							isChanged = true;
							setBinaryChoice(parent, TOP, CONSTANT);
							FiloLogger.log(Level.FINE, "Choice : Binary choice for the ParentVar of a ConstDecVar");
						}
					}
				}

				// all constant decomposition variables must be TOP or CONSTANT
				for (Integer id2 : manager.getConstantDecompositionVariables()) {
					if (choiceTable.containsKey(id2)) {
						isChanged = true;
						setBinaryChoice(id2, TOP, NOTHING);
						FiloLogger.log(Level.FINE, "Choice : Binary choice for ConstDecVarss");
					}
				}

				// if the left side has only the constant, the variable
				// on the right side must be TOP or CONSTANT
				for (GoalSubsumption sub : flatsubsumptions) {
					if (sub.getLeft().size() == 1) {
						for (Integer id : sub.getLeft()) {
							if (id == constant && sub.getRight().size() == 1) {
								for (Integer idRight : sub.getRight()) {
									if (manager.getVariables().contains(idRight) && choiceTable.containsKey(idRight)) {
										isChanged = true;
										Choice.setBinaryChoice(idRight, TOP, NOTHING);
										FiloLogger.log(Level.FINE, "Choice : Binary choice for right side Var");
									}
								}
							}
						}
					}
				}

				// if the right side has only the constant and the only variable on the left
				// side
				// this variable must be CONSTANT
				for (GoalSubsumption sub : flatsubsumptions) {
					if (sub.getRight().size() == 1) {
						for (Integer id : sub.getRight()) {
							if (id == constant && sub.getLeft().size() == 1) {
								for (Integer idLeft : sub.getLeft()) {
									// if(Choice.binaryChoiceExcludesNothing.contains(idLeft)) {
									if (manager.getVariables().contains(idLeft)
											&& !fixedChoiceTable.containsKey(idLeft)) {
										isChanged = true;
										Choice.fixChoice(idLeft, CONSTANT);
										FiloLogger.log(Level.FINE, "Choice : Fixing choice for the only left side Var");
									}
								}
							}
						}
					}
				}
			}
		}
	}

	public static int nextBinaryChoice(AtomManager manager) {
		// next choice string
		Integer[] keys = new Integer[binaryChoiceTable.size()];
		int counter = 0;
		for (Integer id : manager.getVariables()) {
			if (binaryChoiceTable.keySet().contains(id)) {
				keys[counter] = id;
				counter++;
			}
		}
		int i = binaryChoiceTable.size() - 1;
		while (i >= 0 && binaryChoiceTable.get(keys[i]) == 1)
			i--;
		if (i == -1) {
			for (Integer id : manager.getVariables()) {
				if (binaryChoiceTable.keySet().contains(id)) {
					binaryChoiceTable.put(id, 0);
				}
			}
			return -1;
		} else {
			binaryChoiceTable.put(keys[i], binaryChoiceTable.get(keys[i]) + 1);
			for (int j = i + 1; j <= binaryChoiceTable.size() - 1; j++)
				binaryChoiceTable.put(keys[j], 0);
		}
		return 0;
	}

	public static int nextChoice(AtomManager manager) {
		// next choice string
		choiceCounter++;
		Integer[] keys = new Integer[choiceTable.size()];
		int counter = 0;
		for (Integer id : manager.getVariables()) {
			if (choiceTable.keySet().contains(id)) {
				keys[counter] = id;
				counter++;
			}
		}
		int i = choiceTable.size() - 1;
		while (i >= 0 && choiceTable.get(keys[i]) == 2)
			i--;
		if (choiceTable.isEmpty() && nextBinaryChoice(manager) == -1) {
			FiloLogger.log(Level.FINE, "All choices exhausted.");
			return -1;
		} else if (!choiceTable.isEmpty() && nextBinaryChoice(manager) == -1) {
			if (i == -1) {
				System.out.println("All choices exhausted.");
				return -1;
			}
			choiceTable.put(keys[i], choiceTable.get(keys[i]) + 1);
			for (int j = i + 1; j <= choiceTable.size() - 1; j++)
				choiceTable.put(keys[j], 0);
		}
		if (!isConsistent(manager)) {
			return 0;
		}
		consChoiceCounter++;
		return 1;
	}

	public static int nextBinaryChoiceReversed(AtomManager manager) {
		// next choice
		if (binaryChoiceTable.isEmpty())
			return -1;

		Integer[] keys = new Integer[binaryChoiceTable.size()];

		int counter = 0;
		for (Integer id : manager.getVariables()) {
			if (binaryChoiceTable.keySet().contains(id)) {
				keys[counter] = id;
				counter++;
			}
		}

		int i = 0;
		while (i < binaryChoiceTable.size() && binaryChoiceTable.get(keys[i]) == 1)
			i++;

		if (i == binaryChoiceTable.size()) {

			for (Integer id : manager.getVariables()) {
				if (binaryChoiceTable.keySet().contains(id)) {
					binaryChoiceTable.put(id, 0);
				}
			}
			return -1;
		} else {
			binaryChoiceTable.put(keys[i], binaryChoiceTable.get(keys[i]) + 1);
			if (i != 0) {
				for (int j = 0; j <= i - 1; j++)
					binaryChoiceTable.put(keys[j], 0);
			}
		}
		return 0;
	}

	public static int nextChoiceReversed(AtomManager manager, boolean runflag) {
		if(runflag) {
		// next choice
		choiceCounter++;
		Integer[] keys = new Integer[choiceTable.size()];

		int counter = 0;
		for (Integer id : manager.getVariables()) {
			if (choiceTable.keySet().contains(id)) {
				keys[counter] = id;
				counter++;
			}
		}

		int i = 0;
		while (i < choiceTable.size() && choiceTable.get(keys[i]) == 2)
			i++;

		if (choiceTable.isEmpty() && nextBinaryChoiceReversed(manager) == -1) {
			FiloLogger.log(Level.FINE, "All choices exhausted.");
			return -1;
		}

		else if (!choiceTable.isEmpty() && nextBinaryChoiceReversed(manager) == -1) {
			if (i == choiceTable.size()) {
				FiloLogger.log(Level.FINE, "All choices exhausted.");
				return -1;
			}
			choiceTable.put(keys[i], choiceTable.get(keys[i]) + 1);
			if (i != 0) {
				for (int j = 0; j <= i - 1; j++)
					choiceTable.put(keys[j], 0);
			}
		}
		if (!isConsistent(manager)) {

			return 0;
		}

		consChoiceCounter++;

		return 1;}else {
			return -2;
		}
	}

	public static void manualChoice(AtomManager manager) {
		Scanner input = new Scanner(System.in);

		for (Integer id : manager.getVariables()) {
			if (choiceTable.containsKey(id)) {
				System.out.println("Enter the value for variable" + id);
				int answer = input.nextInt();
				setChoice(id, answer);
			}
		}
		input.close();

	}

	public static int manualChoice(AtomManager manager, int[] array) {
		FiloLogger.log(Level.FINE, "Choice : Choice is entered as an array of values");
		Iterator<Integer> iter = choiceTable.keySet().iterator();
		for (int i = 0; i < array.length; i++) {
			setChoice(iter.next(), array[i]);
		}
		if (isConsistent(manager)) {
			return 1;
		} else
			return 0;
	}
}