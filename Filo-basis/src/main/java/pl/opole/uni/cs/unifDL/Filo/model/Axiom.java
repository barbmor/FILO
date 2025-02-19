package pl.opole.uni.cs.unifDL.Filo.model;

import java.util.Set;

/**
 * This class is modified from UEL class Axiom
 * 
 * @author Michał Henne
 * @author Sławomir Kost
 * @author Dariusz Marzec
 * @author Barbara Morawska
 *
 */
public abstract class Axiom {

	final Set<Integer> left;
	final Set<Integer> right;

	Axiom(Set<Integer> left, Set<Integer> right) {
		this.left = left;
		this.right = right;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if ((o == null) || !o.getClass().equals(this.getClass())) {
			return false;
		}
		Axiom other = (Axiom) o;
		return left.equals(other.left) && right.equals(other.right);
	}

	public abstract String getConnective();

	public Set<Integer> getLeft() {
		return left;
	}

	public Set<Integer> getRight() {
		return right;
	}

	@Override
	public int hashCode() {
		return left.hashCode() + 37 * right.hashCode();
	}

	@Override
	public String toString() {
		return "<" + left.toString() + " " + getConnective() + " " + right.toString() + ">";
	}
}
