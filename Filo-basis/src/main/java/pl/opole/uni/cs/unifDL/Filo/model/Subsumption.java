package pl.opole.uni.cs.unifDL.Filo.model;

import java.util.Set;
import pl.opole.uni.cs.unifDL.Filo.controller.AtomManager;

/**
 * This class is modified from UEL class Subsumption
 * 
 * @author Michał Henne
 * @author Sławomir Kost
 * @author Dariusz Marzec
 * @author Barbara Morawska
 * 
 */
public class Subsumption extends Axiom {
	public Subsumption(Set<Integer> left, Set<Integer> right) {
		super(left, right);
	}

	@Override
	public String getConnective() {
		return "⊑";
	}

	// the method isNotFlat
	// returns -1 if the subsumption is flat, otherwise
	// returns id of the first value restriction occurring in the subsumption

	public Integer isNotFlat(AtomManager manager) {

		Integer result = this.isFlatRight(manager);
		if (result < 0) {
			result = this.isFlatLeft(manager);
		}

		return result;

	}

	public Integer isFlatRight(AtomManager manager) {
		Set<Integer> right = this.getRight();
		Integer result = -1;
		for (Integer id : right) {
			if (manager.getAtom(id).isValueRestriction()) {
				result = id;
				break;
			}
		}
		return result;

	}

	public Integer isFlatLeft(AtomManager manager) {
		Set<Integer> left = this.getLeft();
		Integer result = -1;
		for (Integer id : left) {
			if (manager.getAtom(id).isValueRestriction()) {
				result = id;
				break;
			}
		}
		return result;
	}

}