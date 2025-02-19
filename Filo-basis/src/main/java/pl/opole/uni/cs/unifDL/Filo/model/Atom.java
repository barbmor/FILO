package pl.opole.uni.cs.unifDL.Filo.model;

import java.util.List;

import pl.opole.uni.cs.unifDL.Filo.controller.AtomManager;

/**
 * This interface is modified from UEL interface Atom
 *  
 * Represents a flat FL0-atom, which can be a concept name or an value
 * restriction over a concept name (possibly the 'top concept name').
 * 
 * @author Michał Henne
 * @author Sławomir Kost
 * @author Dariusz Marzec
 * @author Barbara Morawska
 */
public interface Atom {

	/**
	 * Tells whether this flat atom is a concept name.
	 * 
	 * @return <code>true</code> if and only if this atom has an associated role
	 *         name
	 */
	boolean isConceptName();

	/**
	 * Tells whether this flat atom is a constant.
	 * 
	 * @return <code>true</code> if and only if this atom is not an existential
	 *         restriction and is ground
	 */
	boolean isConstant();

	/**
	 * Tells whether this flat atom is a value restriction.
	 * 
	 * @return <code>true</code> if and only if this atom has an associated role
	 *         name
	 */
	boolean isValueRestriction();

	/**
	 * Check whether this flat atom is ground.
	 * 
	 * @return <code>true</code> if and only if the concept name is not a variable
	 */
	boolean isGround();

	/**
	 * Check whether this flat atom is a variable.
	 * 
	 * @return <code>true</code> if and only if this atom is not an value
	 *         restriction and is not ground
	 */
	boolean isVariable();
}
