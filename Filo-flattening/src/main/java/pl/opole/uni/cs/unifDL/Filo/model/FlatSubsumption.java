package pl.opole.uni.cs.unifDL.Filo.model;

import java.util.Set;

import pl.opole.uni.cs.unifDL.Filo.controller.AtomManager;
import pl.opole.uni.cs.unifDL.Filo.controller.Choice;

public class FlatSubsumption  extends  GoalSubsumption{

	public FlatSubsumption(Set<Integer> left, Set<Integer> right) {
		super(left, right);
	}

	
	public boolean isFlat() {
		return true;
	}

	
	public boolean isIncreasing() {
		return false;
	}

	
	public boolean isStart() {
		return false;
	}

	
	public boolean isSolved(AtomManager manager, Choice choice) {
		Set<Integer> right = this.getRight();
		for(Integer atomId : right) {
			if(manager.getAtom(atomId).isVariable()&& Choice.getChoice(atomId) == Choice.TOP){
				return true;
			}
		}
		return false;
	}


	
}
