package pl.opole.uni.cs.unifDL.Filo.model;

import java.util.Set;

import pl.opole.uni.cs.unifDL.Filo.controller.AtomManager;

/**
 * This class is modified from UEL Goal
 * 
 * An object implementing this interface is an input for the Filo system.
 * 
 * @author Michał Henne
 * @author Sławomir Kost
 * @author Dariusz Marzec
 * @author Barbara Morawska
 */
public interface Model {

	/**
	 * Returns the atom manager.
	 * 
	 * @return the atom manager
	 */
	AtomManager getAtomManager();

	/**
	 * Returns the set of flattened definitions.
	 * 
	 * @return the set of definitions
	 */
	Set<Definition> getDefinitions();

	/**
	 * Returns the set of flattened goal equations.
	 * 
	 * @return the set of goal equations
	 */
	Set<Equation> getEquations();

	/**
	 * Returns the set of all flattened equations (definitions and goal).
	 * 
	 * @return the set of equations
	 */
	Set<Subsumption> getSubsumptions();

}
