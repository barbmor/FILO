package pl.opole.uni.cs.unifDL.Filo.renderer;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectProperty;

import pl.opole.uni.cs.unifDL.Filo.controller.AtomManager;
import pl.opole.uni.cs.unifDL.Filo.model.Axiom;
import pl.opole.uni.cs.unifDL.Filo.model.Definition;

/**
 * This class is modified from UEL class OWLRenderer
 * 
 * @author Michał Henne
 * @author Sławomir Kost
 * @author Dariusz Marzec
 * @author Barbara Morawska
 * 
 */
public class OWLRenderer extends Renderer<OWLClassExpression, Set<OWLAxiom>> {

	private final OWLDataFactory dataFactory = OWLManager.getOWLDataFactory();
	private Set<OWLAxiom> axioms;
	private OWLClassExpression expr;

	public OWLRenderer(AtomManager atomManager, Set<Definition> background) {
		super(atomManager, null, background);
	}

	public OWLRenderer(AtomManager atomManager, ShortFormProvider provider) {
		super(atomManager, provider);
		initialize();
	}

	@Override
	public Set<OWLAxiom> finalizeAxioms() {
		return axioms;
	}

	@Override
	protected OWLClassExpression finalizeExpression() {
		return expr;
	}

	@Override
	protected void initialize() {
		axioms = new HashSet<>();
		expr = null;
	}

	public Set<OWLAxiom> translateAxiom(Axiom axiom) {
		OWLClassExpression left = translateConjunction(axiom.getLeft());
		OWLClassExpression right = translateConjunction(axiom.getRight());

		OWLAxiom newAxiom = dataFactory.getOWLEquivalentClassesAxiom(left, right);
		axioms.add(newAxiom);
		return axioms;
	}

	private OWLClassExpression translateConjunction(Set<Integer> atomIds) {
		if (atomIds.isEmpty()) {
			return translateTop();
		} else if (atomIds.size() == 1) {
			return translateAtom(atomIds.iterator().next());
		} else {
			return translateTrueConjunction(atomIds);
		}
	}

	protected OWLClassExpression translateExistentialRestriction(String roleName, Integer childId) {
		OWLClassExpression child = translateChild(childId);
		OWLObjectProperty property = dataFactory.getOWLObjectProperty(IRI.create(roleName));
		expr = dataFactory.getOWLObjectSomeValuesFrom(property, child);
		return expr;
	}

	private OWLClassExpression translateChild(Integer childId) {
		// TODO Auto-generated method stub
		return translateAtom(childId);
	}

	@Override
	protected OWLClassExpression translateName(Integer atomId) {
		expr = dataFactory.getOWLClass(IRI.create(renderNameWithoutAdditions(atomId)));
		return expr;
	}

	@Override
	protected OWLClassExpression translateTop() {
		expr = dataFactory.getOWLThing();
		return expr;
	}

	protected OWLClassExpression translateTrueConjunction(Set<Integer> atomIds) {
		Set<OWLClassExpression> classExpressions = atomIds.stream().map(atomId -> translateAtom(atomId))
				.collect(Collectors.toSet());
		expr = dataFactory.getOWLObjectIntersectionOf(classExpressions);
		return expr;
	}

	@Override
	protected Set<OWLAxiom> translateAxiom(Axiom axiom, boolean devOption) {
		// TODO Auto-generated method stub
		return null;
	}

	private OWLClassExpression translateAtom(Integer atomId) {
		if (atomManager.getValueRestrictions().contains(atomId)) {
			Integer childId = atomManager.getChild(atomId);
			String roleName = renderRoleWithoutAdditions(atomManager.getValueRestriction(atomId).getRoleId());
			return translateValueRestriction(roleName, childId);
		} else {
			return translateName(atomId);
		}

	}

	protected OWLClassExpression translateValueRestriction(String roleName, Integer childId) {
		OWLClassExpression child = translateChild(childId);
		OWLObjectProperty property = dataFactory.getOWLObjectProperty(IRI.create(roleName));
		expr = dataFactory.getOWLObjectAllValuesFrom(property, child);
		return expr;
	}

	@Override
	protected OWLClassExpression translateTrueConjunction(Set<Integer> atomIds, boolean devOption) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected OWLClassExpression translateValueRestriction(String roleName, Integer childId, boolean devOption) {
		// TODO Auto-generated method stub
		return null;
	}

}
