package pl.opole.uni.cs.unifDL.Filo.model;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;

import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import pl.opole.uni.cs.unifDL.Filo.controller.AtomManager;
import pl.opole.uni.cs.unifDL.Filo.controller.OntologyReader;


/**
 * This class is modified UEL UelGoalModel
 * 
 * @author Michał Henne
 * @author Sławomir Kost
 * @author Dariusz Marzec
 * @author Barbara Morawska
 */
public class FiloModel implements Model {

	static Logger logger = Logger.getLogger(FiloModel.class.getName());
	
	private final Set<Definition> definitions = new HashSet<>();
	private final Set<Equation> equations = new HashSet<>();
	private final Set<Subsumption> subsumptions = new HashSet<>();
	private final AtomManager atomManager;
	private OntologyReader ontology;

	public FiloModel(AtomManager manager, OntologyReader ontology) {
		this.atomManager = manager;
		this.ontology = ontology;
	}

	public void addPositiveAxioms(Set<? extends OWLAxiom> axioms) {
		for (OWLAxiom axiom : axioms) {
			if (axiom.isOfType(AxiomType.EQUIVALENT_CLASSES)) {
				addEquation((OWLEquivalentClassesAxiom) axiom);
			} else if (axiom.isOfType(AxiomType.SUBCLASS_OF)) {
				addSubsumption((OWLSubClassOfAxiom) axiom);
			} else {
				throw new RuntimeException("Unsupported axiom type: " + axiom);
			}
		}
	}

	public void addEquation(OWLEquivalentClassesAxiom axiom) {
		equations.add(createAxiom(Equation.class, axiom));
	}

	public void addSubsumption(OWLSubClassOfAxiom axiom) {
		subsumptions.add(createAxiom(Subsumption.class, axiom));
	}

	private <T extends Axiom> T createAxiom(Class<T> type, OWLEquivalentClassesAxiom axiom) {
		Iterator<OWLClassExpression> it = axiom.getClassExpressions().iterator();
		return createAxiom(type, it.next(), it.next());
	}

	private <T extends Axiom> T createAxiom(Class<T> type, OWLSubClassOfAxiom axiom) {	
		return createAxiom(type, axiom.getSubClass(), axiom.getSuperClass());
	}

	private <T extends Axiom> T createAxiom(Class<T> type, OWLClassExpression left, OWLClassExpression right) {
		Set<Definition> newDefinitions = new HashSet<>();
		Set<Integer> leftIds = ontology.flattenClassExpression(left, newDefinitions);
		Set<Integer> rightIds = ontology.flattenClassExpression(right, newDefinitions);
		T newAxiom;
		try {
			newAxiom = type.getConstructor(Set.class, Set.class).newInstance(leftIds, rightIds);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
		processDefinitions(newDefinitions);
		return newAxiom;
	}

	public void disposeOntology() {
		ontology = null;
	}

	@Override
	public AtomManager getAtomManager() {
		return atomManager;
	}

	@Override
	public Set<Definition> getDefinitions() {
		return definitions;
	}

	@Override
	public Set<Equation> getEquations() {
		return equations;
	}

	@Override
	public Set<Subsumption> getSubsumptions() {
		return subsumptions;
	}

	private void processDefinitions(Set<Definition> newDefinitions) {	
		
		for (Definition newDefinition : newDefinitions) {
			// only full definitions are allowed
			if (newDefinition.isPrimitive()) {
				definitions.add(processPrimitiveDefinition(newDefinition));
			} else {
				definitions.add(newDefinition);
			}
		}
	}

	private Definition processPrimitiveDefinition(Definition def) {
		logger.info("This should not be accessed now");
		Integer defId = def.getDefiniendum();
		Integer undefId = atomManager.createUndefConceptName(defId);
		Set<Integer> newRightIds = new HashSet<>(def.getRight());
		newRightIds.add(undefId);
		return new Definition(defId, newRightIds, false);
	}

}
