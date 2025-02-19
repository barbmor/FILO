package pl.opole.uni.cs.unifDL.Filo.renderer;

import java.util.Set;
import java.util.logging.Logger;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;

import pl.opole.uni.cs.unifDL.Filo.controller.AtomManager;
import pl.opole.uni.cs.unifDL.Filo.controller.ModelProvider;
import pl.opole.uni.cs.unifDL.Filo.model.Axiom;
import pl.opole.uni.cs.unifDL.Filo.model.Definition;
import pl.opole.uni.cs.unifDL.Filo.model.Model;

/**
 * This class is modified from UEL class Renderer
 * 
 * @author Michał Henne
 * @author Sławomir Kost
 * @author Dariusz Marzec
 * @author Barbara Morawska
 * 
 */
public abstract class Renderer<ExpressionType, AxiomsType> {
	protected final AtomManager atomManager;
	private final ShortFormProvider provider;
	private final Set<Definition> background;

	protected Renderer(AtomManager atomManager, ShortFormProvider provider, Set<Definition> background) {
		this.atomManager = atomManager;
		this.provider = provider;
		this.background = background;
	}

	protected Renderer(AtomManager atomManager, ShortFormProvider provider) {
		this.atomManager = atomManager;
		this.provider = provider;
		this.background = null;
	}

	protected abstract AxiomsType finalizeAxioms();

	protected abstract ExpressionType finalizeExpression();

	private Set<Integer> getDefinition(Integer atomId) {
		for (Definition definition : background) {
			if (definition.getDefiniendum().equals(atomId)) {
				return definition.getRight();
			}
		}
		throw new IllegalArgumentException("Atom has no definition.");
	}

	public String getShortForm(String id) {
		if (provider == null) {
			return id;
		}

		String label = id;
		if (id.endsWith(AtomManager.UNDEF_SUFFIX)) {
			label = id.substring(0, id.length() - AtomManager.UNDEF_SUFFIX.length());
		}
		String str = provider.getShortForm(label);
		if (str != null) {
			label = str;
		}
		if (id.endsWith(AtomManager.UNDEF_SUFFIX)) {
			label += AtomManager.UNDEF_SUFFIX;
		}

		return "<" + label + ">";
	}

	public String getShortFormWithoutAdditions(String id) {
		if (provider == null) {
			return id;
		}

		String label = id;
		if (id.endsWith(AtomManager.UNDEF_SUFFIX)) {
			label = id.substring(0, id.length() - AtomManager.UNDEF_SUFFIX.length());
		}
		String str = provider.getShortForm(label);
		if (str != null) {
			label = str;
		}
		if (id.endsWith(AtomManager.UNDEF_SUFFIX)) {
			label += AtomManager.UNDEF_SUFFIX;
		}
		return label;
	}

	protected abstract void initialize();

	public ExpressionType renderAtom(Integer atomId, boolean devOption) {
		initialize();
		translateAtom(atomId, devOption);
		return finalizeExpression();
	}

	public AxiomsType renderAxioms(Set<? extends Axiom> axioms, boolean devOption) {
		initialize();
		translateAxioms(axioms, devOption);
		return finalizeAxioms();
	}

	public ExpressionType renderConjunction(Set<Integer> atomIds, boolean devOption) {
		initialize();
		translateConjunction(atomIds, devOption);
		return finalizeExpression();
	}

	public AxiomsType renderModel(Model input, boolean devOption) {
		initialize();

		if (devOption) {
			translateAxioms(input.getDefinitions(), devOption);
		}
		translateAxioms(input.getEquations(), devOption);
		translateAxioms(input.getSubsumptions(), devOption);

		return finalizeAxioms();
	}

	public String renderName(Integer atomId) {
		return getShortForm(atomManager.printConceptName(atomId));
	}

	public String renderNameWithoutAdditions(Integer atomId) {
		return getShortFormWithoutAdditions(atomManager.printConceptName(atomId));
	}

	public String renderRole(Integer roleId) {
		return getShortForm(atomManager.getRoleName(roleId));
	}

	public String renderRoleWithoutAdditions(Integer roleId) {
		return getShortFormWithoutAdditions(atomManager.getRoleName(roleId));
	}

	public ExpressionType renderTop() {
		initialize();
		translateTop();
		return finalizeExpression();
	}

	protected ExpressionType translateAtom(Integer atomId, boolean devOption) {
		if (atomManager.getValueRestrictions().contains(atomId)) {
			Integer childId = atomManager.getChild(atomId);
			String roleName = renderRole(atomManager.getValueRestriction(atomId).getRoleId());
			return translateValueRestriction(roleName, childId, devOption);
		} else {
			return translateName(atomId);
		}
	}

	protected abstract AxiomsType translateAxiom(Axiom axiom, boolean devOption);

	public AxiomsType translateAxioms(Set<? extends Axiom> axioms, boolean devOption) {
		AxiomsType ret = null;

		for (Axiom axiom : axioms) {
			ret = translateAxiom(axiom, devOption);
		}
		return ret;
	}

	protected ExpressionType translateChild(Integer childId, boolean devOption) {
		if (atomManager.getFlatteningVariables().contains(childId)) {
			return translateConjunction(getDefinition(childId), devOption);
		} else {
			return translateName(childId);
		}
	}

	protected ExpressionType translateConjunction(Set<Integer> atomIds, boolean devOption) {
		if (atomIds.isEmpty()) {
			return translateTop();
		} else if (atomIds.size() == 1) {
			return translateAtom(atomIds.iterator().next(), devOption);
		} else {
			return translateTrueConjunction(atomIds, devOption);
		}
	}

	protected abstract ExpressionType translateValueRestriction(String roleName, Integer childId, boolean devOption);

	protected abstract ExpressionType translateName(Integer atomId);

	protected abstract ExpressionType translateTop();

	protected abstract ExpressionType translateTrueConjunction(Set<Integer> atomIds, boolean devOption);

	protected Set<OWLAxiom> translateAxiom(Axiom axiom) {
		// TODO Auto-generated method stub
		return null;
	}

	protected OWLClassExpression translateExistentialRestriction(String roleName, Integer childId) {
		// TODO Auto-generated method stub
		return null;
	}

	protected OWLClassExpression translateTrueConjunction(Set<Integer> atomIds) {
		// TODO Auto-generated method stub
		return null;
	}

}
