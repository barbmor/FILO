package pl.opole.uni.cs.unifDL.Filo.renderer;

import java.util.Set;

import pl.opole.uni.cs.unifDL.Filo.controller.AtomManager;
import pl.opole.uni.cs.unifDL.Filo.model.Axiom;
import pl.opole.uni.cs.unifDL.Filo.model.Definition;

/**
 * This class is modified from UEL class StringRenderer
 * 
 * @author Michał Henne
 * @author Sławomir Kost
 * @author Dariusz Marzec
 * @author Barbara Morawska
 *
 */
public abstract class StringRenderer extends Renderer<String, String> {

	public static StringRenderer createInstance(AtomManager atomManager, ShortFormProvider provider,
			Set<Definition> background) {
		return new ManchesterRenderer(atomManager, provider, background);
	}

	protected StringBuilder sb;

	protected StringRenderer(AtomManager atomManager, ShortFormProvider provider, Set<Definition> background) {
		super(atomManager, provider, background);
	}

	@Override
	protected String finalizeAxioms() {
		return sb.toString();
	}

	@Override
	protected String finalizeExpression() {
		// TODO remove '<' and '>' around result?
		return sb.toString();
	}

	@Override
	protected void initialize() {
		sb = new StringBuilder();
	}

	@Override
	protected String translateAxiom(Axiom axiom, boolean devOption) {

		translateConjunction(axiom.getLeft(), devOption);

		sb.append(" ");
		sb.append(axiom.getConnective());
		sb.append(" ");

		translateConjunction(axiom.getRight(), devOption);

		sb.append(System.lineSeparator());
		sb.append(System.lineSeparator());
		return "";
	}

	@Override
	protected String translateName(Integer atomId) {
		sb.append(renderName(atomId));
		return "";
	}

}