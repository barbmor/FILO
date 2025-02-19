package pl.opole.uni.cs.unifDL.Filo.model;

import java.util.Set;

import pl.opole.uni.cs.unifDL.Filo.controller.AtomManager;
import pl.opole.uni.cs.unifDL.Filo.controller.Choice;

public class StartSubsumption extends  GoalSubsumption {

	public StartSubsumption(Set<Integer> left, Set<Integer> right) {
		super(left, right);
		
	}

	
	public boolean isFlat() {
		return false;
	}

	
	public boolean isIncreasing() {
		return false;
	}

	
	public boolean isStart() {
		return true;
	}

	
	public boolean isSolved(AtomManager manager, Choice choice) {
		// TODO Auto-generated method stub
		return false;
	}

	

}
