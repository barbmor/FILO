package pl.opole.uni.cs.unifDL.Filo.model;

import java.util.Iterator;
import pl.opole.uni.cs.unifDL.Filo.controller.AtomManager;

/**
 * This class is modified from UEL class ConceptName
 * 
 * @author Michał Henne
 * @author Sławomir Kost
 * @author Dariusz Marzec
 * @author Barbara Morawska
 *
 */
public class ConceptName implements Atom {

	private final Integer conceptNameId;
	private String conceptName;
	private boolean isVariable = false;

	public Integer getConceptNameId() {
		return conceptNameId;
	}

	public ConceptName(Integer conceptNameId) {
		this.conceptNameId = conceptNameId;
	}

	@Override
	public boolean isConstant() {
		return !isVariable();
	}

	public ConceptName getConceptName() {
		return this;
	}

	String getConceptNameName() {
		return conceptName;
	}

	@Override
	public boolean isConceptName() {
		return true;
	}

	@Override
	public boolean isValueRestriction() {
		return false;
	}

	@Override
	public boolean isGround() {
		return isConstant();
	}

	@Override
	public boolean isVariable() {
		return isVariable;
	}

	public void makeConstant() {
		isVariable = false;
	}

	public void makeVariable() {
		isVariable = true;

	}

	@Override
	public boolean equals(Object name) {

		return this.getConceptNameId() == ((ConceptName) name).getConceptNameId();
	}

	public boolean fullyDecomposed(AtomManager manager) {
		if (this.isVariable) {
			boolean found = true;
			Iterator<Integer> roleIt = manager.getRoleIds().iterator();

			while (found && roleIt.hasNext()) {
				Integer role = roleIt.next();

				found = false;
				for (Integer child : manager.getDecompositionVariables()) {

					if (manager.getDecompositionVariable(child).getRole() == role
							&& manager.getDecompositionVariable(child).getParent() == this.getConceptNameId()) {
						found = true;
					}
				}
			}
			if (found) {
				found = false;
				for (Integer child : manager.getConstantDecompositionVariables()) {
					if (manager.getDecompositionVariable(child).getParent() == this.getConceptNameId())
						found = true;
					break;
				}
			}

			return found;

		} else
			throw new UnsupportedOperationException();
	}

}
