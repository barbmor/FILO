package pl.opole.uni.cs.unifDL.Filo.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import pl.opole.uni.cs.unifDL.Filo.model.Definition;

/**
 * An object of this class is a Filo ontology that pulls in definitions from
 * background OWL ontologies as needed. All definitions produced by this class
 * will be flat. In UEL this class is UelOntology.
 *
 * @author Michał Henne
 * @author Sławomir Kost
 * @author Dariusz Marzec
 * @author Barbara Morawska
 */
public class OntologyReader {
	//static Logger logger = Logger.getLogger(OntologyReader.class.getName());

	private static final String flatteningVariablePrefix = "var";
	private int flatteningVariableIndex = 0;

	private final Set<Integer> visited = new HashSet<>();
	private final Map<Integer, OWLClass> nameMap = new HashMap<>();
	private final AtomManager atomManager;
	private final OWLOntology ontology;
	private final OWLClass top;

	public OntologyReader(AtomManager atomManager, OWLOntology ontology, OWLClass top) {
		this.atomManager = atomManager;
		this.ontology = ontology;
		this.top = top;
	}

	private Integer createFreshFlatteningDefinition(Set<Integer> atomIds, Set<Definition> newDefinitions) {
		Integer varId = createFreshFlatteningVariable();
		newDefinitions.add(new Definition(varId, atomIds, false));
		return varId;
	}

	private Integer createFreshFlatteningVariable() {
		String str = flatteningVariablePrefix + flatteningVariableIndex;
		flatteningVariableIndex++;
		Integer varId = atomManager.createConceptName(str);
		atomManager.makeFlatteningVariable(varId);
		return varId;
	}

	private Set<Integer> flattenClass(OWLClass cls) {
	    OWLDataFactory dataFactory = OWLManager.getOWLDataFactory();
	    //if (cls.equals(dataFactory.getOWLThing())) {
	    if (cls.isOWLThing()) {
	        return Collections.emptySet();
	    }
	    
	    Integer atomId = atomManager.createConceptName(cls.toStringID());
	    return Collections.singleton(atomId);
	}
	
	

	public Set<Integer> flattenClassExpression(OWLClassExpression expression, Set<Definition> newDefinitions) {
		if (expression instanceof OWLClass) {
			return flattenClass((OWLClass) expression);
		}
		if (expression instanceof OWLObjectIntersectionOf) {
			return flattenConjunction((OWLObjectIntersectionOf) expression, newDefinitions);
		}
		if (expression instanceof OWLObjectAllValuesFrom) {
			return flattenValueRestriction((OWLObjectAllValuesFrom) expression, newDefinitions);
		}
		if (expression instanceof OWLObjectSomeValuesFrom) {
			return flattenExistentialRestriction((OWLObjectSomeValuesFrom) expression, newDefinitions);
		}
		throw new RuntimeException("Unsupported class expression: " + expression);
	}

	private Set<Integer> flattenConjunction(OWLObjectIntersectionOf conjunction, Set<Definition> newDefinitions) {
		Set<Integer> atomIds = new HashSet<>();
		for (OWLClassExpression operand : conjunction.getOperands()) {
			atomIds.addAll(flattenClassExpression(operand, newDefinitions));
		}
		return atomIds;
	}

	private Set<Integer> flattenExistentialRestriction(OWLObjectSomeValuesFrom existentialRestriction,
			Set<Definition> newDefinitions) {
		OWLObjectPropertyExpression propertyExpr = existentialRestriction.getProperty();
		if (propertyExpr.isAnonymous()) {
			throw new RuntimeException("Unsupported object property expression: " + propertyExpr);
		}

		String roleName = propertyExpr.getNamedProperty().toStringID();
		Set<Integer> fillerIds = flattenClassExpression(existentialRestriction.getFiller(), newDefinitions);
		Integer fillerId = null;

		if (fillerIds.size() == 0) {
			// the empty conjunction is top
			fillerId = atomManager.createConceptName(top.toStringID());
		} else if (fillerIds.size() == 1) {
			fillerId = fillerIds.iterator().next();
		}

		if ((fillerId == null) || !atomManager.getAtom(fillerId).isConceptName()) {
			// if we have more than one atom id in 'fillerIds' or the only atom
			// id is not a concept name, then we need to introduce a new
			// definition in order to obtain a flat atom
			fillerId = createFreshFlatteningDefinition(fillerIds, newDefinitions);
		}

		Integer atomId = atomManager.createExistentialRestriction(roleName, fillerId);
		return Collections.singleton(atomId);
	}

	private Set<Integer> flattenValueRestriction(OWLObjectAllValuesFrom valueRestriction,
			Set<Definition> newDefinitions) {

		OWLObjectPropertyExpression propertyExpr = valueRestriction.getProperty();
		if (propertyExpr.isAnonymous()) {
			throw new RuntimeException("Unsupported object property expression: " + propertyExpr);
		}

		String roleName = propertyExpr.getNamedProperty().toStringID();

		if (valueRestriction.getFiller().isOWLThing()) {
			return Collections.emptySet();
		}
		else {
			Set<Integer> fillerIds = flattenClassExpression(valueRestriction.getFiller(), newDefinitions);
			Iterator<Integer> iterator = fillerIds.iterator();
			Integer fillerId = null;
	
			Set<Integer> identifiers = new HashSet<Integer>();
			if (fillerIds.size() == 0) {
				// the empty conjunction is top
				fillerId = atomManager.createConceptName(top.toStringID());
				Integer particleId = atomManager.createValueRestriction(roleName, fillerId);
				identifiers.add(particleId);
			} else if (fillerIds.size() == 1) {
				// Now filler is a concept or a value restriction
				fillerId = iterator.next();
	
				if (atomManager.getAtom(fillerId).isValueRestriction()) {
					Set<Integer> definiens = new HashSet<Integer>();
					definiens.add(fillerId);
					fillerId = createFreshFlatteningDefinition(definiens, newDefinitions);
				}
				;
	
				// Here value restriction is created with filler either a variable or a concept
				// name
				// bottom of the recursion (one of possible bottoms, the other one is a
				// conjunction
				// of names or variables)
				Integer particleId = atomManager.createValueRestriction(roleName, fillerId);
				identifiers.add(particleId);
			} else if (fillerIds.size() > 1) {
				while (iterator.hasNext()) {
					fillerId = iterator.next();
					if (atomManager.getAtom(fillerId).isValueRestriction()) {
						Set<Integer> definiens = new HashSet<Integer>();
						definiens.add(fillerId);
						fillerId = createFreshFlatteningDefinition(definiens, newDefinitions);
					}
					Integer particleId = atomManager.createValueRestriction(roleName, fillerId);
					identifiers.add(particleId);
				}
	
			}
	
			return Collections.unmodifiableSet(identifiers);
		}
	}

}
