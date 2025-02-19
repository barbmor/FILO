package pl.opole.uni.cs.unifDL.Filo.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import pl.opole.uni.cs.unifDL.Filo.controller.AtomManager;
import pl.opole.uni.cs.unifDL.Filo.controller.FiloLogger;
import pl.opole.uni.cs.unifDL.Filo.controller.ShortcutsManager;
import pl.opole.uni.cs.unifDL.Filo.renderer.ShortFormProvider;
import pl.opole.uni.cs.unifDL.Filo.view.Renderer;

/**
 * Shortcuts are just sets of variables that "satisfy" the flat subsumptions
 * 
 * @author Barbara Morawska
 * 
 */
public class Shortcut {

	private Set<Integer> variables;

	private Map<Integer, Set<Integer>> resolved;

	public Shortcut(Set<Integer> vars, AtomManager manager) {
		variables = vars;
		resolved = new HashMap<>();
		for (Integer var : variables) {
			if (manager.getDecompositionVariables().contains(var)
					&& !manager.getConstantDecompositionVariables().contains(var)) {
				Integer role = manager.getDecompositionVariable(var).getRole();
				resolved.put(role, new HashSet<>());
			}
		}
	}

	public void add(Integer id, AtomManager manager) {
		variables.add(id);
		if (manager.getDecompositionVariables().contains(id)
				&& !manager.getConstantDecompositionVariables().contains(id)) {
			Integer role = manager.getDecompositionVariable(id).getRole();
			if (!resolved.containsKey(role))
				resolved.put(role, new HashSet<>());
		}
	}

	public Set<Integer> getVariables() {
		return variables;
	}

	public Map<Integer, Set<Integer>> getResolved() {
		return resolved;
	}

	public boolean satisfies(Set<FlatSubsumption> flatsubsumptions) {

		boolean answer = true;
		for (FlatSubsumption sub : flatsubsumptions) {
			if (answer) {
				for (Integer id : sub.getRight()) {
					if (answer) {
						if (variables.contains(id)) {
							answer = false;
							for (Integer leftid : sub.getLeft()) {
								if (variables.contains(leftid)) {
									answer = true;
									break;
								}
							}
						}
					} else {
						break;
					}
				}
			} else
				break;
		}

		if (answer == false)
			FiloLogger.log(Level.FINE, "Shortcut : This shortcut does not satisfy flat subsumptions");
		return answer;
	}

	public void print(AtomManager manager) {
		FiloLogger.log(Level.FINE, Shortcut.class.getName());
		OWLOntologyManager ontmanager = OWLManager.createOWLOntologyManager();
		Renderer renderer = new Renderer(manager, new ShortFormProvider(ontmanager));
		String information = "[";
		for (Integer id : variables) {
			information = information + renderer.getShortForm(manager.printConceptName(id)) + " , ";
		}
		information = information + "]";
		FiloLogger.log(Level.FINE, "Shortcut : " + information);
	}

	public void printfull(AtomManager manager) {
		FiloLogger.log(Level.FINE, Shortcut.class.getName());
		OWLOntologyManager ontmanager = OWLManager.createOWLOntologyManager();
		Renderer renderer = new Renderer(manager, new ShortFormProvider(ontmanager));

		if (!this.isEmpty()) {
			String information = "\n variables: [";
			for (Integer id : variables) {
				information = information + renderer.getShortForm(manager.printConceptName(id)) + " , ";
			}
			information = information + "]";
			String info = "\n resolvedIDs: [";
			for (Integer id : manager.getRoleIds()) {
				if (resolved.containsKey(id)) {
					info = info + "for role " + id + ": ";

					for (Integer shid : resolved.get(id)) {
						info = info + shid;
					}
					info = info + "]\n";
				}
			}
			FiloLogger.log(Level.FINE, "Shortcut : " + information + info);
		} else
			FiloLogger.log(Level.FINE, "This shortcut is empty");

	}

	public boolean isEmpty() {
		return variables.isEmpty();
	}

	@Override
	public boolean equals(Object sh) {
		if (sh instanceof Shortcut && this.getVariables().equals(((Shortcut) sh).getVariables()))
			return true;
		else
			return false;
	}

	public boolean isResolved(ShortcutsManager shmanager, AtomManager manager) {

		for (Integer variable : variables) {
			// if this is a decomposition variable,
			if (manager.getDecompositionVariables().contains(variable)
					&& !manager.getConstantDecompositionVariables().contains(variable)) {
				FiloLogger.log(Level.FINE, "Shortcut: Shortcut contains dec var " + variable);
				boolean found = false;
				for (Shortcut shortcut : shmanager.getShortcuts()) {
					Integer role = manager.getDecompositionVariable(variable).getRole();
					if (this.isResolved(shortcut, manager, role)) {
						// check if all variables in the old shortcut have children in the given one
						found = true;
						Integer id = shmanager.getShortcutIndices().getIndex(shortcut);
						FiloLogger.log(Level.FINE, "Shortcut: Adding shortcut id to resolved: " + id);
						resolved.get(role).add(id);
						break;
					}
				}
				// if does not exist, return false
				if (!found)
					return false;
			}
		}
		return true;
	}

	private boolean isResolved(Shortcut shortcut, AtomManager manager, Integer role) {
		OWLOntologyManager ontmanager = OWLManager.createOWLOntologyManager();
		Renderer renderer = new Renderer(manager, new ShortFormProvider(ontmanager));
		FiloLogger.log(Level.FINE, "Shortcut: Checking if the shortcut is resolved by ");
		shortcut.print(manager);
		for (Integer variable : shortcut.variables) {
			for (Integer child : manager.getDecompositionVariables()) {
				if (!manager.getConstantDecompositionVariables().contains(child)) {
					if (manager.getDecompositionVariable(child).getParent() == variable
							&& manager.getDecompositionVariable(child).getRole() == role) {
						if (!this.variables.contains(child)) {
							FiloLogger.log(Level.FINE, "Shortcut: The shortcut does not contain dec variable : "
									+ renderer.getShortForm(manager.printConceptName(child)));

							return false;
						}
					}
				}
			}
		}

		return true;
	}

	@Override
	public int hashCode() {
		return this.variables.hashCode();
	}

}
