package pl.opole.uni.cs.unifDL.Filo.model;

import java.util.Collections;
import java.util.Set;

/**
 * This class is modified from UEL class Definition
 * 
 * @author Michał Henne
 * @author Sławomir Kost
 * @author Dariusz Marzec
 * @author Barbara Morawska
 *
 */
public class Definition extends Axiom {
	private boolean primitive;

	public Definition(Integer left, Set<Integer> right, boolean primitive) {
		super(Collections.singleton(left), right);
		this.primitive = primitive;
	}

	@Override
	public boolean equals(Object o) {
		return super.equals(o) && primitive == ((Definition) o).primitive;
	}

	@Override
	public String getConnective() {
		return primitive ? "⊑" : "≡";
	}

	public Integer getDefiniendum() {
		return left.iterator().next();
	}

	public boolean isPrimitive() {
		return primitive;
	}
}