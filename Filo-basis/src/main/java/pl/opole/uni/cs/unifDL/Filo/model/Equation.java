package pl.opole.uni.cs.unifDL.Filo.model;

import java.util.Set;

/**
 * This class is borrowed from UEL class Equation
 */
public class Equation extends Axiom {
	public Equation(Set<Integer> left, Set<Integer> right) {
		super(left, right);
	}

	@Override
	public String getConnective() {
		return "≡";
	}
}