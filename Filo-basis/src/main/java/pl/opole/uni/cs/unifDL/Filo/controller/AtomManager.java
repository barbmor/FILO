package pl.opole.uni.cs.unifDL.Filo.controller;

import java.util.Set;
import pl.opole.uni.cs.unifDL.Filo.model.Atom;
import pl.opole.uni.cs.unifDL.Filo.model.ConceptName;
import pl.opole.uni.cs.unifDL.Filo.model.DecompositionVariable;
import pl.opole.uni.cs.unifDL.Filo.model.ValueRestriction;
import pl.opole.uni.cs.unifDL.Filo.renderer.ShortFormProvider;

/**
 * An atom manager manages identifiers for atoms (concept names and value
 * restrictions) and also identifiers and names for concepts and roles.
 * 
 * This interface is modified from UEL interface AtomManager
 * 
 * @author Michał Henne
 * @author Sławomir Kost
 * @author Dariusz Marzec
 * @author Barbara Morawska
 * 
 */
public interface AtomManager {

	String UNDEF_SUFFIX = "_UNDEF";

	Integer createConceptName(String conceptName);

	Integer createValueRestriction(String roleName, Integer child);

	Integer createUndefConceptName(Integer originId);

	Atom getAtom(Integer atomId);

	Integer getChild(Integer atomId);

	ConceptName getConceptName(Integer atomId);

	Set<Integer> getConstants();

	Set<Integer> getDefinitionVariables();

	ValueRestriction getValueRestriction(Integer atomId);

	Set<Integer> getValueRestrictions();

	Set<Integer> getFlatteningVariables();

	Integer getIndex(Atom atom);

	Set<Integer> getUserVariables();

	Set<Integer> getVariables();

	void makeConstant(Integer atomId);

	void makeDefinitionVariable(Integer atomId);

	void makeFlatteningVariable(Integer atomId);

	void makeUserVariable(Integer atomId);

	String printConceptName(Integer atomId);

	String printRoleName(Integer atomId);

	Set<Integer> getRoleIds();

	String getRoleName(Integer roleId);

	int size();

	Integer createExistentialRestriction(String roleName, Integer fillerId);
	
	Set<Integer> getDecompositionVariables();
	
	void makeDecompositionVariable(Integer atomId);

	void makeConstantDecompositionVariable(Integer atomId);
	
	void resetDecompositionVariables();
	
	Integer createDecompositionVariable(Integer parentId, Integer roleId, ShortFormProvider renderer);
	
	DecompositionVariable getDecompositionVariable(Integer atomId);

	Integer createConstantDecompositionVariable(Integer atomId, Integer rolename, ShortFormProvider renderer);

	Integer getConstant();

	Set<Integer> getConstantDecompositionVariables();
	
	
}
