


package pl.opole.uni.cs.unifDL.Filo.controller;

import java.util.Set;
import java.util.logging.Level;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import pl.opole.uni.cs.unifDL.Filo.model.GenericGoal;
import pl.opole.uni.cs.unifDL.Filo.model.Goal;
import pl.opole.uni.cs.unifDL.Filo.model.Model;
import pl.opole.uni.cs.unifDL.Filo.model.Subsumption;
import pl.opole.uni.cs.unifDL.Filo.renderer.ShortFormProvider;

/**
 * Goal provider sets up a goal for the unifier
 * 
 * @author Barbara Morawska
 * @author Dariusz Marzec
 */

public  class GoalProvider {
	
/*
 * The boolean variable success is true if 
 * there are no constants (the goal is unifiable with the empty substitution,
 * or if the unifiability is detected by ImplicitSolver while
 * the goal is created.
 * 
 */
	private static boolean success = false;
	Model model;
	public static int goalcounter = 0;


	public GoalProvider(Model filoModel, AtomManager manager) {
		model = filoModel;
		if(manager.getConstants().isEmpty()) {
			success = true;
		}else success = false;
	}

	public static boolean getSuccess() {
		return success;
	}

	public GenericGoal setupGenericGoal( AtomManager oldmanager, Integer constant) {
		oldmanager.resetDecompositionVariables();
		Integer A = constant;
		GenericGoal goal = new GenericGoal(oldmanager, A);
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		ShortFormProvider renderer = new ShortFormProvider(manager);
		ModelReader.setModel(model);
		ModelReader.setAtomManager(oldmanager);
		ModelReader.setRenderer(renderer);
		Set<Subsumption> newsubsumptions = (Set<Subsumption>) ModelReader.normalize();

		for(Subsumption normalizedSub : newsubsumptions) {
			goal.addSubsumption(normalizedSub);
		}
		ImplicitSolver.foreignConstantCheck(goal, oldmanager);

		return goal;
	}

	public Goal setupGoal(GenericGoal base,  AtomManager manager)  {
	
		goalcounter++;
		success = false;

		Integer constant = base.A;
		
		Goal goal = new Goal(base, constant, manager);
		
		ImplicitSolver.criticalChecks(goal, manager);
		
		if(ImplicitSolver.answer) {
			FiloLogger.log(Level.FINE,"GoalProvider : The goal nr " + goalcounter + " is setup for the next step");
			Choice.printChoice(manager);
			ImplicitSolver.nonfailingChecks(goal, manager);
			success = ImplicitSolver.termination;
			FiloLogger.log(Level.FINE, "ImplicitSolver: termination = " + success);
			ImplicitSolver.defineStartSubsumptions(goal, manager);
			return goal;
		}else {
			Choice.printChoice(manager);
			FiloLogger.log(Level.FINE,"GoalProvider : The goal is not unifiable for this choice. Update the choice and trying again.");
			return null;
		}
	}
}
