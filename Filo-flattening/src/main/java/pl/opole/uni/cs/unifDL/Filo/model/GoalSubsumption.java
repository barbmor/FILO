package pl.opole.uni.cs.unifDL.Filo.model;

import java.util.Set;

import pl.opole.uni.cs.unifDL.Filo.controller.AtomManager;
import pl.opole.uni.cs.unifDL.Filo.controller.Choice;

public  class GoalSubsumption extends Subsumption{

	
	public GoalSubsumption(Set<Integer> left, Set<Integer> right) {
		super(left, right);
		
	}
	/*
	 * boolean isFlat(); boolean isIncreasing(); boolean isStart(); boolean
	 * isSolved(AtomManager manager,Choice choice);
	 */

	public void deleteLeftAtom(Integer id) {
		this.left.remove(id);
		
	}

	public boolean leftIsConstantEquiv(AtomManager manager) {
		boolean value = true;
		boolean found = false;
		if(left.isEmpty())
			value = false;
		for(Integer id : left) {
			if(manager.getAtom(id).isVariable() && Choice.getChoice(id) !=Choice.TOP)
				value = false;
			else if( id==Goal.A ) {
					found = true;
				}
			}
			
			return value & found;
		}

	public boolean leftIsTopEquiv(AtomManager manager) {
		boolean value = true;
		if(!left.isEmpty()) {
		for(Integer id : left) {
			if(!manager.getAtom(id).isVariable() || Choice.getChoice(id)!=Choice.TOP) {
				value = false;
			}
		}}
		return value;
	}
	
	
}
