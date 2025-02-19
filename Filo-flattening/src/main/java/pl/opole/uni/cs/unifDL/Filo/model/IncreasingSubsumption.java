package pl.opole.uni.cs.unifDL.Filo.model;

import java.util.Set;

import pl.opole.uni.cs.unifDL.Filo.controller.AtomManager;
import pl.opole.uni.cs.unifDL.Filo.controller.Choice;

 public class IncreasingSubsumption extends  GoalSubsumption {

	public IncreasingSubsumption(Set<Integer> left, Set<Integer> right) {
		super(left, right);
		
	}

	
	public boolean isFlat() {
		return false;
	}

	
	public boolean isIncreasing() {
		
		return true;
	}

	
	public boolean isStart() {
		return false;
	}

	
	public boolean isSolved(AtomManager manager, Choice choice) {
		// TODO Auto-generated method stub
		return false;
	}

	

}
