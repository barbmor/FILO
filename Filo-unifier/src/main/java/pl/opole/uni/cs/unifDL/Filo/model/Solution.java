package pl.opole.uni.cs.unifDL.Filo.model;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLXMLOntologyFormat;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.util.SimpleIRIMapper;

import pl.opole.uni.cs.unifDL.Filo.controller.AtomManager;
import pl.opole.uni.cs.unifDL.Filo.controller.Choice;
import pl.opole.uni.cs.unifDL.Filo.controller.FiloLogger;
import pl.opole.uni.cs.unifDL.Filo.controller.ShortcutsManager;
import pl.opole.uni.cs.unifDL.Filo.renderer.OWLRenderer;
import pl.opole.uni.cs.unifDL.Filo.renderer.ShortFormProvider;
import pl.opole.uni.cs.unifDL.Filo.view.Renderer;

/**
 * Computing solution from choice or from shortcuts
 * 
 * @author Barbara Morawska
 * 
 */
public class Solution {

	private Map<Integer, Set<Integer>> solution;
	private Set<Definition> definitions;

	public Solution(AtomManager atmanager) {

		OWLOntologyManager ontmanager = OWLManager.createOWLOntologyManager();
		Renderer renderer = new Renderer(atmanager, new ShortFormProvider(ontmanager));

		solution = new HashMap<>();
		for (Integer var : atmanager.getVariables()) {
			FiloLogger.log(Level.FINE,
					"Solution: Initializing set for var " + renderer.getShortForm(atmanager.printConceptName(var)));
			solution.put(var, new HashSet<>());
		}
	}

	public void addToSolution(ShortcutsManager shmanager, AtomManager atmanager) {

		if (shmanager.getInitial() != null) {

			Shortcut shortcut = shmanager.getInitial();
			Integer P = Goal.A;
			substitute(shortcut, P, atmanager);
			for (Integer role : atmanager.getRoleIds()) {

				if (shortcut.getResolved().containsKey(role)) {
					FiloLogger.log(Level.FINE, "Solution: Checking for shortcuts for role " + role);
					if (shortcut.getResolved().get(role) == null)
						FiloLogger.log(Level.FINE, "Solution: This shortcut is not resolved");
					else
						FiloLogger.log(Level.FINE, "Solution: This shortcut is resolved");

					for (Integer shId : shortcut.getResolved().get(role)) {
						FiloLogger.log(Level.FINE, "Shortcutid is " + shId);
						Shortcut sh = shmanager.getShortcutIndices().get(shId);
						Integer newparticle = atmanager.createValueRestriction(atmanager.getRoleName(role), P);
						addToSolution(sh, shmanager, newparticle, atmanager);

					}
				}
			}
		}
	}

	private void addToSolution(Shortcut shortcut, ShortcutsManager shmanager, Integer p, AtomManager atmanager) {
		substitute(shortcut, p, atmanager);
		for (Integer role : atmanager.getRoleIds()) {

			if (shortcut.getResolved().containsKey(role)) {
				for (Integer shId : shortcut.getResolved().get(role)) {
					Shortcut sh = shmanager.getShortcutIndices().get(shId);
					sh.print(atmanager);
					Integer newparticle = atmanager.createValueRestriction(atmanager.getRoleName(role), p);
					addToSolution(sh, shmanager, newparticle, atmanager);
				}
			}
		}
	}

	public void addToSolution(AtomManager atmanager) {
		FiloLogger.log(Level.FINE, "Solution: Add to Solution for implicit solver case ");
		Choice.printFixedChoice(atmanager);
		for (Integer var : atmanager.getVariables()) {
			FiloLogger.log(Level.FINE, "Solution: Choice for var: " + var + " is " + Choice.getChoice(var));
			if (Choice.getChoice(var) == Choice.CONSTANT) {
				substitute(var, Goal.A);
				FiloLogger.log(Level.FINE, "Solution: Substitute for var " + var + " constant " + Goal.A);
				Integer P = Goal.A;
				if (atmanager.getDecompositionVariables().contains(var)) {

					Integer parent = atmanager.getDecompositionVariable(var).getParent();
					Integer role = atmanager.getDecompositionVariable(var).getRole();
					P = atmanager.createValueRestriction(atmanager.getRoleName(role), P);
					addToSolution(atmanager, parent, P);
				}
			} else {
				FiloLogger.log(Level.FINE, "Solution: Choice for var " + var + " is not constant");
			}
		}

	}

	private void addToSolution(AtomManager atmanager, Integer var, Integer p) {
		substitute(var, p);
		if (atmanager.getDecompositionVariables().contains(var)) {

			Integer parent = atmanager.getDecompositionVariable(var).getParent();
			Integer role = atmanager.getDecompositionVariable(var).getRole();
			p = atmanager.createValueRestriction(atmanager.getRoleName(role), p);
			addToSolution(atmanager, parent, p);
		}
	}

	private void substitute(Integer var, Integer a) {
		FiloLogger.log(Level.FINE, "Solution: Adding to var " + var + ": " + a);
		this.solution.get(var).add(a);

	}

	private void substitute(Shortcut shortcut, Integer p, AtomManager atmanager) {
		for (Integer var : shortcut.getVariables()) {
			if (var != Goal.A) {
				this.solution.get(var).add(p);
			}
		}
	}

	public void finalize(AtomManager atmanager) {
		FiloLogger.log(Level.FINE, "Solution: In finilizing function");
		definitions = new HashSet<>();
		for (Integer var : atmanager.getVariables()) {
			FiloLogger.log(Level.FINE, "Solution: Adding to var " + var + solution.get(var).toString());
			definitions.add(new Definition(var, solution.get(var), false));
		}
	}

	public Set<Definition> getDefinitions() {
		return definitions;
	}

	public Map<Integer, Set<Integer>> getSolutionMap() {
		return solution;
	}

	public void merge(Solution solution1) {

		if (solution1 == null) {
			solution1 = this;
		} else {
			FiloLogger.log(Level.FINE, "Previous solution is not null");
			for (Integer variable : solution1.getSolutionMap().keySet()) {
				FiloLogger.log(Level.FINE, "updating solution for var " + variable);
				if (this.solution.containsKey(variable)) {
					FiloLogger.log(Level.FINE, "This variable is in the new solution too ");
					this.getSolutionMap().get(variable).addAll(solution1.getSolutionMap().get(variable));

				} else {
					FiloLogger.log(Level.FINE, "This variable is NOT in the new solution ");
					this.solution.put(variable, solution1.getSolutionMap().get(variable));
				}
			}
		}
	}

	public String print(AtomManager atmanager) {
		String result;
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		Renderer renderer = new Renderer(atmanager, new ShortFormProvider(manager));
		FiloLogger.log(Level.INFO, "Solution: Printing solution:");

		FiloLogger.log(Level.INFO, renderer.translateDefinitions(this.getDefinitions()).toString());
		result = renderer.translateDefinitions(this.getDefinitions(), atmanager).toString();
		if (!result.isEmpty() && !result.isBlank()) {
			result = "\nSolution:\n " + result;
			return result;
		} else {
			return "\nThere are no user variables. The problem is ground.";
		}
	}

	public OWLOntology toOntology(AtomManager atmanager)
			throws OWLOntologyCreationException, IOException, OWLOntologyStorageException {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		ShortFormProvider provider = new ShortFormProvider(manager);
		IRI ontology_iri = IRI.create("http://unifdl.cs.uni.opole.pl/ontologies/solution.owl#");
		File output = new File("solution.owl");
		IRI documentIRI = IRI.create(output);
		OWLOntology ontology = manager.createOntology(ontology_iri);
		OWLRenderer owlrenderer = new OWLRenderer(atmanager, provider);
		for (Definition def : definitions) {
			if (atmanager.getUserVariables().contains(def.getDefiniendum())) {
				owlrenderer.translateAxiom(def);
			}
		}

		ontology.addAxioms(owlrenderer.finalizeAxioms());
		manager.saveOntology(ontology, documentIRI);

		return ontology;
	}
}
