package pl.opole.uni.cs.unifDL.Filo.model;

/**
 * This class is modified from UEL class ExistentialRestriction
 * 
 * Represents a flat FL0-atom consisting of a role name and a concept name.
 * 
 * @author Michał Henne
 * @author Sławomir Kost
 * @author Dariusz Marzec
 * @author Barbara Morawska
 */
public class ValueRestriction implements Atom {

	private final Integer child;
	private final Integer role;

	/**
	 * Construct a new flat value restriction.
	 * 
	 * @param role  the role name identifier
	 * @param child the concept name
	 */
	public ValueRestriction(Integer role, Integer child) {
		this.role = role;
		this.child = child;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof ValueRestriction)) {
			return false;
		}

		ValueRestriction other = (ValueRestriction) obj;
		if (!other.role.equals(role)) {
			return false;
		}
		if (!other.child.equals(child)) {
			return false;
		}
		return true;
	}

	public Integer getConceptName() {
		return child;
	}

	/**
	 * Retrieve the role name of this Value restriction.
	 * 
	 * @return the role name
	 */
	public Integer getRoleId() {
		return role;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + child.hashCode();
		result = prime * result + role;
		return result;
	}

	@Override
	public boolean isConceptName() {
		return false;
	}

	@Override
	public boolean isConstant() {
		return false;
	}

	@Override
	public boolean isValueRestriction() {
		return true;
	}

	@Override
	public boolean isGround() {
		return true; // child.isGround();
	}

	@Override
	public boolean isVariable() {
		return false;
	}

	@Override
	public String toString() {
		return "Forall " + role + " " + child;
	}

}
