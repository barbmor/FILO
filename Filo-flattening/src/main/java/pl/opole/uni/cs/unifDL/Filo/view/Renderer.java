package pl.opole.uni.cs.unifDL.Filo.view;

import java.util.Set;
import java.util.logging.Logger;

import pl.opole.uni.cs.unifDL.Filo.controller.AtomManager;
import pl.opole.uni.cs.unifDL.Filo.model.Definition;
import pl.opole.uni.cs.unifDL.Filo.model.GoalSubsumption;
import pl.opole.uni.cs.unifDL.Filo.model.Subsumption;
import pl.opole.uni.cs.unifDL.Filo.renderer.RendererKeywords;
import pl.opole.uni.cs.unifDL.Filo.renderer.ShortFormProvider;


 public class Renderer {
	 static Logger logger = Logger.getLogger(Renderer.class.getName());
	 
	 protected StringBuilder sb;
	 private final AtomManager atomManager;
	private final ShortFormProvider provider;
	
	public Renderer( AtomManager atomManager, ShortFormProvider prov) {
		
		this.atomManager =  atomManager;
		this.provider = prov;
		sb = new StringBuilder();
	}
	

	public StringBuilder translateSubsumptions(Set<? extends Subsumption> set) {
		
		sb = new StringBuilder();
		for (Subsumption subsumption : set) {
			translateSubsumption(subsumption);
		}
		return sb;

	}


	public void translateSubsumption(Subsumption subsumption) {
		
			
			sb.append(translateConjunction(subsumption.getLeft()));
			
			sb.append(" ");
			sb.append(subsumption.getConnective());
			sb.append(" ");
			
			sb.append(translateConjunction(subsumption.getRight()));
			
			sb.append(System.lineSeparator());
			sb.append(System.lineSeparator());
		
	
	}


	private String translateConjunction(Set<Integer> atomIds) {
		if (atomIds.isEmpty()) {
			return translateTop();
		} else if (atomIds.size() == 1) {
			return translateAtom(atomIds.iterator().next());
		} else {
			return translateTrueConjunction(atomIds);
		}
		
	}


	private String translateTrueConjunction(Set<Integer> atomIds) {
		sb.append(RendererKeywords.open);

		for (Integer atomId : atomIds) {
			translateAtom(atomId);
			sb.append(RendererKeywords.space);
			sb.append(RendererKeywords.and);
			sb.append(RendererKeywords.space);
		}

		sb.setLength(sb.length() - 2 * RendererKeywords.space.length() - RendererKeywords.and.length());
		sb.append(RendererKeywords.close);
		return "";
	}


	public String translateAtom(Integer atomId) {
		if (atomManager.getValueRestrictions().contains(atomId)) {
			Integer childId = atomManager.getChild(atomId);
			String roleName = renderRole(atomManager.getValueRestriction(atomId).getRoleId());
			return translateValueRestriction(roleName, childId);
		} else {
			return translateName(atomId);
		}
	}


	private String renderRole(Integer roleId) {
		return getShortForm(atomManager.getRoleName(roleId));
	}


	public String getShortForm(String id) {
		if (provider == null) {
			return id;
		}

		String label = id;
		if (id.endsWith(atomManager.UNDEF_SUFFIX)) {
			label = id.substring(0, id.length() - atomManager.UNDEF_SUFFIX.length());
		}
		String str = provider.getShortForm(label);
		if (str != null) {
			label = str;
		}
		if (id.endsWith(atomManager.UNDEF_SUFFIX)) {
			label += atomManager.UNDEF_SUFFIX;
		}

		return "<" + label + ">";
	}


	
		public String translateName(Integer atomId) {
			sb.append(renderName(atomId));
			return "";
		}
	


	public String renderName(Integer atomId) {
		return getShortForm(atomManager.printConceptName(atomId));
	}


	protected String translateValueRestriction(String roleName, Integer childId) {
		sb.append(RendererKeywords.open);
		sb.append(roleName);
		sb.append(RendererKeywords.space);
		sb.append(RendererKeywords.only);
		sb.append(RendererKeywords.space);
			translateAtom(childId);
		sb.append(RendererKeywords.close);
		return "";
	}



	protected String translateTop() {
		sb.append(RendererKeywords.owlThing);
		return "";
	}


	public StringBuilder translateDefinitions(Set<Definition> definitions) {

		sb = new StringBuilder();
		for (Definition def : definitions) {
			translateDefinition(def);
		}
		return sb;
	}

	public StringBuilder translateDefinitions(Set<Definition> definitions, AtomManager atmanager) {
		sb = new StringBuilder();
		for(Definition def : definitions) {
			if(atmanager.getUserVariables().contains(def.getDefiniendum())) {
				translateDefinition(def);
			}
			
		}
		return sb;
	}

	private  void translateDefinition(Definition def) {
		
		sb.append(translateConjunction(def.getLeft()));
		
		sb.append(" ");
		sb.append(def.getConnective());
		sb.append(" ");
		
		sb.append(translateConjunction(def.getRight()));
		
		sb.append(System.lineSeparator());
		sb.append(System.lineSeparator());
	}

	}
