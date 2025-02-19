package pl.opole.uni.cs.unifDL.Filo.model;

import java.util.Set;

import pl.opole.uni.cs.unifDL.Filo.controller.AtomManager;
import pl.opole.uni.cs.unifDL.Filo.controller.Choice;

public class SolvedSubsumption  extends  GoalSubsumption{

	public SolvedSubsumption(Set<Integer> left, Set<Integer> right) {
		super(left, right);
	}


	public boolean isSolved() {
		return true;
	}
	public boolean isFlat() {
		return false;
	}

	
	public boolean isIncreasing() {
		return false;
	}

	
	public boolean isStart() {
		return false;
	}



	
	
	
}
