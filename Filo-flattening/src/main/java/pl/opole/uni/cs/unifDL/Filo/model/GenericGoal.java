package pl.opole.uni.cs.unifDL.Filo.model;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import pl.opole.uni.cs.unifDL.Filo.controller.AtomManager;
import pl.opole.uni.cs.unifDL.Filo.controller.FiloLogger;
import pl.opole.uni.cs.unifDL.Filo.controller.ModelReader;
import pl.opole.uni.cs.unifDL.Filo.renderer.ShortFormProvider;
import pl.opole.uni.cs.unifDL.Filo.view.Renderer;

/**
 * Goal is the unification problem after flattening II
 * 
 * @author Barbara Morawska
 * 
 */

public class GenericGoal {
	// FiloLogger.log(Level.FINE, FiloLogger.class.getName());
	public  Integer A;
	
	private  AtomManager manager;
	//private  PartialSolution solution;
	public Set<GoalSubsumption> flatsubsumptions = new HashSet<>();
	private Set<GoalSubsumption> increasingsubsumptions = new HashSet<>();
	//private Set<GoalSubsumption> startsubsumptions = new HashSet<>();
	
	//private Set<Subsumption> solvedsubsumptions = new HashSet<>();
	//private Choice choice;
	
	public GenericGoal( AtomManager manager, Integer name) {
		A = name;
		this.manager = manager;
		//choice = new Choice(manager);
	}

	
	public Set<GoalSubsumption> getFlatSubsumptions() {
		return flatsubsumptions;
	}
	
	public Set<GoalSubsumption> getIncreasingSubsumptions() {
		return increasingsubsumptions;
	}
	
	public void print(AtomManager atomManager)  {
		 FiloLogger.log(Level.FINE, GenericGoal.class.getName());
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		//ShortFormProvider provider = new ShortFormProvider(manager);
		Renderer renderer = new Renderer(atomManager, new ShortFormProvider(manager));
		// System.out.println();
		FiloLogger.log(Level.FINE,"Printing Generic Goal\n"); 
		  
		FiloLogger.log(Level.FINE,"Flat subsumptions:");
		  
		FiloLogger.log(Level.FINE,renderer.translateSubsumptions(this.getFlatSubsumptions()).toString());
		  
		FiloLogger.log(Level.FINE,"Increasing subsumptions:");
		  
		FiloLogger.log(Level.FINE,renderer.translateSubsumptions(this.getIncreasingSubsumptions()).toString());
		 
	}
	
	

	
	////////////////The following method is flattening subsumptions and adding them to the generic goal
	/////////////////it should also detect if a flat subsumption is already solved and add it to solved, not to flat subsumptions/////////////
	
	public void addSubsumption(Subsumption subsumption) {
		/*if(ImplicitSolver.firstCheck(subsumption, manager)) {
			solvedsubsumptions.add(ImplicitSolver.makeSolvedSubsumption(subsumption));	
		}else {*/
		// Renderer printrenderer = new Renderer(manager, new ShortFormProvider(OWLManager.createOWLOntologyManager()));
			/*
			 * System.out.println("Flattening subsumption: "); System.out.print(
			 * printrenderer.translateSubsumptions( (Set<? extends Subsumption>)
			 * Collections.singleton(subsumption))); System.out.println();
			 */	  
		Collection<GoalSubsumption> flattenedSubsumptions =   ModelReader.mainflattening(A, subsumption);
		
		/*
		 * System.out.println("Goal: Printing flattened subsumptions for control");
		 * 
		 * System.out.print( printrenderer.translateSubsumptions( (Set<? extends
		 * Subsumption>) flattenedSubsumptions)); System.out.println();
		 */
		
		for(GoalSubsumption newSubsumption : flattenedSubsumptions) {
		//flatten and add increasing or start when needed
			if (newSubsumption instanceof IncreasingSubsumption) {
				increasingsubsumptions.add(newSubsumption);
			}
			/*
			 * else if (newSubsumption instanceof StartSubsumption) {
			 * startsubsumptions.add(newSubsumption); }
			 */
			else if (newSubsumption instanceof FlatSubsumption) {
					flatsubsumptions.add(newSubsumption);
			}
			else throw new IllegalArgumentException("Argument does not represent a goal subsumption.");
		}
	}
/////////////////////////////////////////////////////////////////
	
	public void stats(AtomManager atomManager) {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		Renderer renderer = new Renderer(atomManager, new ShortFormProvider(manager));
		System.out.println();
		System.out.println("Generic Goal stats:\n ");
		int finalNumberOfAtoms = atomManager.size();
		int finalNumberOfConstants = atomManager.getConstants().size();
		int finalNumberOfVariables = atomManager.getVariables().size();
		int finalNumberOfUserVariables = atomManager.getUserVariables().size();
		  
		int finalNumberOfSubsumptions = this.getFlatSubsumptions().size() +
				this.getIncreasingSubsumptions().size();
		  
		  System.out.println("Final number of atoms: " + finalNumberOfAtoms);
		  System.out.println("Final number of constants: " + finalNumberOfConstants);
		  System.out.println("Final number of variables: " + finalNumberOfVariables);
		  System.out.println("Final number of user variables: " + finalNumberOfUserVariables);
		  System.out.println("Final number of subsumptions: " + finalNumberOfSubsumptions);
		  

		  System.out.println();
		  System.out.print("Constants: ");	
		  for(Integer constant: atomManager.getConstants()) {
			  System.out.print(" " + renderer.getShortForm(atomManager.printConceptName(constant))+ ", ");
		  }
		  System.out.println();
		  System.out.print("Variables: ");	
		  for(Integer constant: atomManager.getVariables()) {
			  System.out.print(" " + renderer.getShortForm(atomManager.printConceptName(constant))+ ", ");
		  }
		  System.out.println();
	}
	/*
	 * private boolean isSolved(Subsumption subsumption) { Set<Integer> right =
	 * subsumption.getRight(); for(Integer atomId : right) {
	 * if(manager.getAtom(atomId).isVariable()&& Choice.getChoice(atomId) ==
	 * Choice.TOP){ return true; } } return false; }
	 */
}
