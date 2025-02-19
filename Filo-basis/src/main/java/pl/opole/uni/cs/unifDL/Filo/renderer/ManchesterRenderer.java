package pl.opole.uni.cs.unifDL.Filo.renderer;

import java.util.Set;

import pl.opole.uni.cs.unifDL.Filo.controller.AtomManager;
import pl.opole.uni.cs.unifDL.Filo.model.Axiom;
import pl.opole.uni.cs.unifDL.Filo.model.Definition;
import pl.opole.uni.cs.unifDL.Filo.model.FiloModel;

/**
 * This class is modified from UEL class ManchesterRenderer
 * 
 * @author Michał Henne
 * @author Sławomir Kost
 * @author Dariusz Marzec
 * @author Barbara Morawska
 * 
 */
public class ManchesterRenderer extends StringRenderer {

	protected ManchesterRenderer(AtomManager atomManager, ShortFormProvider provider, Set<Definition> background) {
		super(atomManager, provider, background);
	}

	@Override
	protected String translateValueRestriction(String roleName, Integer childId, boolean devOption) {
		sb.append(RendererKeywords.open);
		sb.append(roleName);
		sb.append(RendererKeywords.space);
		sb.append(RendererKeywords.only);
		sb.append(RendererKeywords.space);
		if (!devOption)
			translateChild(childId, devOption);
		else
			translateName(childId);
		sb.append(RendererKeywords.close);
		return "";
	}

	@Override
	protected String translateTop() {
		sb.append(RendererKeywords.owlThing);
		return "";
	}

	@Override
	protected String translateTrueConjunction(Set<Integer> atomIds, boolean devOption) {
		sb.append(RendererKeywords.open);

		for (Integer atomId : atomIds) {
			translateAtom(atomId, devOption);
			sb.append(RendererKeywords.space);
			sb.append(RendererKeywords.and);
			sb.append(RendererKeywords.space);
		}

		sb.setLength(sb.length() - 2 * RendererKeywords.space.length() - RendererKeywords.and.length());
		sb.append(RendererKeywords.close);
		return "";
	}

}
