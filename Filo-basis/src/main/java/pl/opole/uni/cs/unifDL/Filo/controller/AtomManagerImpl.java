package pl.opole.uni.cs.unifDL.Filo.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import pl.opole.uni.cs.unifDL.Filo.model.Atom;
import pl.opole.uni.cs.unifDL.Filo.model.ConceptName;
import pl.opole.uni.cs.unifDL.Filo.model.ConstantDecompositionVariable;
import pl.opole.uni.cs.unifDL.Filo.model.DecompositionVariable;
import pl.opole.uni.cs.unifDL.Filo.model.ValueRestriction;
import pl.opole.uni.cs.unifDL.Filo.model.Variable;
import pl.opole.uni.cs.unifDL.Filo.renderer.ShortFormProvider;

/**
 * This class is modified from UEL class AtomManagerImpl
 * 
 * @author Michał Henne
 * @author Sławomir Kost
 * @author Dariusz Marzec
 * @author Barbara Morawska
 * 
 */

public class AtomManagerImpl implements AtomManager {

	/**
	 * 
	 * atoms are here representing value restrictions in form of flat particles
	 * 
	 * if existential restrictions are read, they are treated as value restrictions
	 * later
	 * 
	 */

	final IndexedSet<Atom> atoms = new IndexedSetImpl<>();
	final Map<Integer, Integer> childMap = new HashMap<>();
	final IndexedSet<String> conceptNames = new IndexedSetImpl<>();
	final Set<Integer> constants = new HashSet<>();
	private final Set<Integer> definitionVariables = new HashSet<>();
	private final Set<Integer> flatteningVariables = new HashSet<>();
	final IndexedSet<String> roleNames = new IndexedSetImpl<>();
	private final Set<Integer> userVariables = new HashSet<>();
	final Set<Integer> variables = new HashSet<>();
	private Set<Integer> decompositionVariables = new HashSet<>();
	private Set<Integer> constantdecompositionVariables = new HashSet<>();

	public AtomManagerImpl() {
	}

	@Override
	public Integer createConceptName(String conceptName) {
		Integer index = conceptNames.getIndex(conceptName);
		Integer atomId = -1;
		Integer conceptNameId = conceptNames.addAndGetIndex(conceptName);
		if (index == -1) {
			ConceptName concept = new ConceptName(conceptNameId);
			atomId = atoms.addAndGetIndex(concept);
			if (conceptName.matches(".*_var$")) {
				concept.makeVariable();
				variables.add(atomId);
				userVariables.add(atomId);
			}
			if (!variables.contains(atomId)) {
				constants.add(atomId);
			}
		} else {
			for (Integer id : atoms.getIndices()) {
				Atom concept = atoms.get(id);
				if (concept.isConceptName() && ((ConceptName) concept).getConceptNameId().equals(conceptNameId)) {
					atomId = id;
					break;
				}
			}
		}

		return atomId;
	}

	@Override
	public Integer createValueRestriction(String roleName, Integer childId) {	
		Integer roleId = roleNames.addAndGetIndex(roleName);
		Integer atomId = atoms.addAndGetIndex(new ValueRestriction(roleId, childId));
		childMap.put(atomId, childId);
		return atomId;
	}

	@Override
	public Integer createUndefConceptName(Integer originId) {
		String newName = conceptNames.get(getConceptName(originId).getConceptNameId()) + UNDEF_SUFFIX;
		return createConceptName(newName);
	}

	@Override
	public Atom getAtom(Integer atomId) {
		return atoms.get(atomId);
	}

	@Override
	public Integer getChild(Integer atomId) {
		return childMap.get(atomId);
	}

	@Override
	public ConceptName getConceptName(Integer atomId) {
		Atom atom = atoms.get(atomId);

		if ((atom == null) || !atom.isConceptName()) {
			throw new IllegalArgumentException("Argument does not represent a concept name.");
		}
		return (ConceptName) atom;
	}

	@Override
	public Set<Integer> getConstants() {
		return Collections.unmodifiableSet(constants);
	}

	@Override
	public Set<Integer> getDefinitionVariables() {
		return Collections.unmodifiableSet(definitionVariables);
	}

	@Override
	public ValueRestriction getValueRestriction(Integer atomId) {
		Atom atom = atoms.get(atomId);
		if ((atom == null) || !atom.isValueRestriction()) {
			throw new IllegalArgumentException("Argument does not represent an Value restriction.");
		}
		return (ValueRestriction) atom;
	}

	@Override
	public Set<Integer> getValueRestrictions() {
		return Collections.unmodifiableSet(childMap.keySet());
	}

	@Override
	public Set<Integer> getFlatteningVariables() {
		return Collections.unmodifiableSet(flatteningVariables);
	}

	@Override
	public Integer getIndex(Atom atom) {
		return atoms.getIndex(atom);
	}

	@Override
	public Set<Integer> getRoleIds() {
		return Collections.unmodifiableSet(roleNames.getIndices());
	}

	@Override
	public String getRoleName(Integer roleId) {
		return roleNames.get(roleId);
	}

	@Override
	public Set<Integer> getUserVariables() {
		return Collections.unmodifiableSet(userVariables);
	}

	@Override
	public Set<Integer> getVariables() {
		return Collections.unmodifiableSet(variables);
	}

	@Override
	public void makeConstant(Integer atomId) {
		getConceptName(atomId).makeConstant();
		constants.add(atomId);
		variables.remove(atomId);
		userVariables.remove(atomId);
		definitionVariables.remove(atomId);
		flatteningVariables.remove(atomId);
	}

	@Override
	public void makeDefinitionVariable(Integer atomId) {
		getConceptName(atomId).makeVariable();
		constants.remove(atomId);
		variables.add(atomId);
		userVariables.remove(atomId);
		definitionVariables.add(atomId);
		flatteningVariables.remove(atomId);
	}

	@Override
	public void makeFlatteningVariable(Integer atomId) {
		getConceptName(atomId).makeVariable();
		constants.remove(atomId);
		variables.add(atomId);
		userVariables.remove(atomId);
		definitionVariables.remove(atomId);
		flatteningVariables.add(atomId);
	}

	@Override
	public void makeUserVariable(Integer atomId) {
		getConceptName(atomId).makeVariable();
		constants.remove(atomId);
		variables.add(atomId);
		userVariables.add(atomId);
		definitionVariables.remove(atomId);
		flatteningVariables.remove(atomId);
	}

	@Override
	public String printConceptName(Integer atomId) {
		return conceptNames.get(getConceptName(atomId).getConceptNameId());
	}

	@Override
	public String printRoleName(Integer atomId) {
		return roleNames.get(getValueRestriction(atomId).getRoleId());
	}

	@Override
	public int size() {
		return atoms.size();
	}

	@Override
	public Integer createExistentialRestriction(String roleName, Integer fillerId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Integer> getDecompositionVariables() {
		return Collections.unmodifiableSet(decompositionVariables);
	}

	@Override
	public void makeDecompositionVariable(Integer atomId) {
		constants.remove(atomId);
		variables.add(atomId);
		userVariables.remove(atomId);
		definitionVariables.remove(atomId);
		flatteningVariables.remove(atomId);
		decompositionVariables.add(atomId);
	}

	@Override
	public void makeConstantDecompositionVariable(Integer atomId) {
		constants.remove(atomId);
		variables.add(atomId);
		userVariables.remove(atomId);
		definitionVariables.remove(atomId);
		flatteningVariables.remove(atomId);
		decompositionVariables.add(atomId);
		constantdecompositionVariables.add(atomId);

	}

	@Override
	public void resetDecompositionVariables() {
		for (Integer atomId : getDecompositionVariables()) {
			variables.remove(atomId);
			userVariables.remove(atomId);
			definitionVariables.remove(atomId);
			flatteningVariables.remove(atomId);
			constantdecompositionVariables.remove(atomId);
		}
		decompositionVariables = new HashSet<>();
	}

	@Override
	public Integer createDecompositionVariable(Integer parentId, Integer roleId, ShortFormProvider renderer) {
		String newName = conceptNames.get(getConceptName(parentId).getConceptNameId()).toString() + "_"
				+ renderer.getShortForm(roleNames.get(roleId));
		Integer index = conceptNames.getIndex(newName);
		Integer atomId = -1;
		Integer conceptNameId = conceptNames.addAndGetIndex(newName);
		if (index == -1) {

			DecompositionVariable dec = new DecompositionVariable(parentId, roleId, conceptNameId);
			atomId = atoms.addAndGetIndex(dec);
			getConceptName(atomId).makeVariable();
			Atom parent = getAtom(parentId);
		} else {
			for (Integer id : atoms.getIndices()) {
				Atom concept = atoms.get(id);
				if (concept.isConceptName() && ((ConceptName) concept).getConceptNameId() == conceptNameId) {
					atomId = id;
					break;
				}
			}
		}
		return atomId;
	}

	@Override
	public Integer createConstantDecompositionVariable(Integer parentId, Integer constant, ShortFormProvider renderer) {
		String newName = conceptNames.get(getConceptName(parentId).getConceptNameId()).toString() + "_"
				+ renderer.getShortForm(conceptNames.get(getConceptName(constant).getConceptNameId()).toString());
		Integer index = conceptNames.getIndex(newName);
		Integer atomId = -1;
		Integer conceptNameId = conceptNames.addAndGetIndex(newName);
		if (index == -1) {

			DecompositionVariable dec = new DecompositionVariable(parentId, constant, conceptNameId);

			atomId = atoms.addAndGetIndex(dec);

			makeConstantDecompositionVariable(atomId);
			Atom parent = getAtom(parentId);
		} else {
			for (Integer id : atoms.getIndices()) {
				Atom concept = atoms.get(id);
				if (concept.isConceptName() && ((ConceptName) concept).getConceptNameId().equals(conceptNameId)) {
					atomId = id;
					break;
				}
			}
		}
		return atomId;
	}

	@Override
	public DecompositionVariable getDecompositionVariable(Integer atomId) {
		Atom atom = atoms.get(atomId);
		if ((atom == null) || !atom.isVariable()) {
			throw new IllegalArgumentException("Argument does not represent a Variable.");
		}
		if (atom instanceof DecompositionVariable)
			return (DecompositionVariable) atom;
		else
			throw new IllegalArgumentException("Argument does not represent a Decomposition Variable.");
	}

	@Override
	public Integer getConstant() {

		Iterator<Integer> it = constants.iterator();
		if (it.hasNext())
			return it.next();
		else
			return null;
	}

	public Set<Integer> getConstantDecompositionVariables() {
		return constantdecompositionVariables;
	}

}
