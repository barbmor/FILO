package pl.opole.uni.cs.unifDL.Filo.controller;

import java.util.Set;
import java.util.logging.Level;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import pl.opole.uni.cs.unifDL.Filo.model.Definition;
import pl.opole.uni.cs.unifDL.Filo.model.FiloModel;
import pl.opole.uni.cs.unifDL.Filo.model.Model;
import pl.opole.uni.cs.unifDL.Filo.renderer.ShortFormProvider;
import pl.opole.uni.cs.unifDL.Filo.renderer.StringRenderer;

/**
 * An object of this class connects the graphical user interface with the
 * unification algorithm. In UEL this class is UelModel.
 * 
 * @author Michał Henne
 * @author Sławomir Kost
 * @author Dariusz Marzec
 * @author Barbara Morawska
 */
public class ModelProvider {
	private boolean allUnifiersFound;
	private AtomManager atomManager;
	private int currentUnifierIndex;
	private FiloModel model;
	private ShortFormProvider provider;

	/**
	 * Constructs a new Filo model.
	 * 
	 * @param provider the OntologyProvider that should be used to load ontologies
	 *                 and short forms
	 */
	public ModelProvider(ShortFormProvider provider) {
		this.provider = provider;
	}

	private void cacheShortForms() {
		for (Integer atomId : atomManager.getConstants()) {
			provider.getShortForm(atomManager.printConceptName(atomId));
		}
		for (Integer atomId : atomManager.getVariables()) {
			provider.getShortForm(atomManager.printConceptName(atomId));
		}
		for (Integer roleId : atomManager.getRoleIds()) {
			provider.getShortForm(atomManager.getRoleName(roleId));
		}
	}

	public Integer getAtomId(String name) {
		return atomManager.createConceptName(name);
	}

	/**
	 * Return the model used for the unification algorithm.
	 * 
	 * @return the model
	 */
	public Model getModel() {
		return model;
	}

	/**
	 * Construct a renderer for output of unifiers etc. as strings. The actual
	 * format is specified in the method 'StringRenderer.createInstance'.
	 * 
	 * @param background the background definitions used for formatting unifiers
	 * @return a new StringRenderer object
	 */
	public StringRenderer getStringRenderer(Set<Definition> background) {
		return StringRenderer.createInstance(atomManager, provider, background);
	}

	/**
	 * Prints the model using the default StringRenderer. boolean devOption: if
	 * true, does not eliminate the system variables in the model
	 * 
	 * @return a string representation of the unification model
	 */
	public String printModel(boolean devOption) {

		String output = getStringRenderer(model.getDefinitions()).renderModel(model, devOption);

		return output;

	}

	/**
	 * Resets the internal cache of the short form provider.
	 */
	public void resetShortFormCache() {
		provider.resetCache();
	}

	/**
	 * Initializes the unification model with the given ontologies.
	 * 
	 * @param positiveProblem     the positive part of the unification problem
	 * @param negativeProblem     the negative part of the unification problem
	 * @param owlThingAlias       (optional) an alias for owl:Thing, e.g., 'SNOMED
	 *                            CT Concept'
	 * @param resetShortFormCache reset short form cache
	 */
	public void setupModel(OWLOntology positiveProblem, OWLClass owlThingAlias, boolean resetShortFormCache) {
		if (positiveProblem == null) {
			FiloLogger.log(Level.INFO, "Ontology is null");
		}
		setupModel(positiveProblem, positiveProblem.getAxioms(AxiomType.SUBCLASS_OF),
				positiveProblem.getAxioms(AxiomType.EQUIVALENT_CLASSES), owlThingAlias, resetShortFormCache);
	}

	/**
	 * Initializes the unification goal with the given ontologies and axioms.
	 * 
	 * @param subsumptions        the goal subsumptions, as OWLSubClassOfAxioms
	 * @param equations           the goal equations, as binary
	 *                            OWLEquivalentClassesAxioms
	 * @param owlThingAlias       (optional) an alias for owl:Thing, e.g., 'SNOMED
	 *                            CT Concept'
	 * @param resetShortFormCache reset short form cache
	 */
	public void setupModel(OWLOntology ontology, Set<OWLSubClassOfAxiom> subsumptions,
			Set<OWLEquivalentClassesAxiom> equations, OWLClass owlThingAlias, boolean resetShortFormCache) {

		currentUnifierIndex = -1;
		allUnifiersFound = false;
		atomManager = new AtomManagerImpl();

		if (resetShortFormCache) {
			resetShortFormCache();
		}

		OWLClass top = (owlThingAlias != null) ? owlThingAlias : OWLManager.getOWLDataFactory().getOWLThing();
		model = new FiloModel(atomManager, new OntologyReader(atomManager, ontology, top));

		model.addPositiveAxioms(subsumptions);
		model.addPositiveAxioms(equations);

		OWLDataFactory factory = OWLManager.getOWLDataFactory();

		model.disposeOntology();
	}

}