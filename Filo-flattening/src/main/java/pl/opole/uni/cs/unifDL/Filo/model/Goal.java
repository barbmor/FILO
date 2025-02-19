package pl.opole.uni.cs.unifDL.Filo.model;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import pl.opole.uni.cs.unifDL.Filo.controller.AtomManager;
import pl.opole.uni.cs.unifDL.Filo.controller.FiloLogger;
import pl.opole.uni.cs.unifDL.Filo.renderer.ShortFormProvider;
import pl.opole.uni.cs.unifDL.Filo.view.Renderer;

/**
 * Goal is the unification problem after flattening II
 * 
 * @author Barbara Morawska
 * @author Dariusz Marzec
 */

public class Goal  {
	Logger logger = Logger.getLogger(Goal.class.getName());
	public static Integer A;

	//This is not the same as on GitHub????
	// private AtomManager manager;
	//private PartialSolution solution;
	
	 private Set<FlatSubsumption> flatsubsumptions; 
	 private Set<GoalSubsumption> increasingsubsumptions; 
	 private Set<GoalSubsumption> startsubsumptions = new HashSet<>();
	 

	private Set<Subsumption> solvedsubsumptions = new HashSet<>();
	//private Choice choice;

	public Goal(GenericGoal base, Integer name, AtomManager manager) {
		//super(manager);
		A = name;
		//this.choice = choice;
		this.setFlatSubsumptions(base.getFlatSubsumptions());
		this.setIncreasingSubsumptions(base.getIncreasingSubsumptions());
	}

	
	  private void setIncreasingSubsumptions(Set<GoalSubsumption> increasing) {
	  this.increasingsubsumptions = new HashSet<>(increasing);
	  
	  }
	 

	public Set<FlatSubsumption> getFlatSubsumptions() {
		return flatsubsumptions;
	}

	public Set<GoalSubsumption> getIncreasingSubsumptions() {
		return increasingsubsumptions;
	}

	public Set<GoalSubsumption> getStartSubsumptions() {
		return startsubsumptions;
	}

	public Set<Subsumption> getSolvedSubsumptions() {
		return solvedsubsumptions;
	}

	public void deleteFlatSubsumption(GoalSubsumption sub) {
		flatsubsumptions.remove(sub);
	}

	
	  private void setFlatSubsumptions(Set<GoalSubsumption> flat) {
		  Set<FlatSubsumption> newflat = new HashSet<>();
		  for(GoalSubsumption sub : flat) {
			  Set<Integer> newleft = new HashSet<>(sub.getLeft());
			  Set<Integer> newright = new HashSet<>(sub.getRight());
			  newflat.add(new FlatSubsumption(newleft, newright));
			  
		  }
	  this.flatsubsumptions = newflat; }
	 
		public void print(AtomManager atomManager) {
			FiloLogger.log(Level.INFO, Goal.class.getName());
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			//ShortFormProvider provider = new ShortFormProvider(manager);
			Renderer renderer = new Renderer(atomManager, new ShortFormProvider(manager));
			 System.out.println();
			 FiloLogger.log(Level.INFO,"Printing  Goal\n"); 
			  
			  
			 FiloLogger.log(Level.INFO,"Flat subsumptions:");
			  
			 FiloLogger.log(Level.INFO,
			  renderer.translateSubsumptions(this.getFlatSubsumptions()).toString());
			  
			 FiloLogger.log(Level.INFO,"Increasing subsumptions:");
			  
			 FiloLogger.log(Level.INFO,
			  renderer.translateSubsumptions(this.getIncreasingSubsumptions()).toString());
			 
			  
			  
			 FiloLogger.log(Level.INFO,"Start subsumptions:");
			  
			  FiloLogger.log(Level.INFO, 
			  renderer.translateSubsumptions(this.getStartSubsumptions()).toString());
			 
			  FiloLogger.log(Level.INFO,"Solved subsumptions:");
			  
			  FiloLogger.log(Level.INFO,
			  renderer.translateSubsumptions(this.getSolvedSubsumptions()).toString());
			 
			  
		}
	
		public void stats(AtomManager atomManager) {
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			Renderer renderer = new Renderer(atomManager, new ShortFormProvider(manager));
			System.out.println();
			System.out.println("Goal stats:\n ");
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
