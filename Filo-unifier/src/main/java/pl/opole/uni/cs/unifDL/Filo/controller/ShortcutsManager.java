package pl.opole.uni.cs.unifDL.Filo.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import pl.opole.uni.cs.unifDL.Filo.model.FlatSubsumption;
import pl.opole.uni.cs.unifDL.Filo.model.Goal;
import pl.opole.uni.cs.unifDL.Filo.model.Shortcut;
import pl.opole.uni.cs.unifDL.Filo.renderer.ShortFormProvider;
import pl.opole.uni.cs.unifDL.Filo.view.Renderer;

/**
 * Work in progress... Shortcuts Manager keeps all shortcuts Computes shortcuts
 * in a brute force way, without computing possibilities first
 * 
 * @author Barbara Morawska
 * 
 */
public class ShortcutsManager {

	private Set<Shortcut> shortcuts;
	private Set<Shortcut> rejected;

	private IndexedSet<Shortcut> shortcutIndices;
	private Map<Integer, Set<Integer>> varShortcutsMap;
	private Set<Integer> possiblyGoodVar;

	// The good variables are those for which we have already found shortcuts
	private Set<Integer> goodvar;
	private Shortcut initial;

	private int height = 0;
	private int change = 0;

	public ShortcutsManager(AtomManager manager, Goal goal) {

		FiloLogger.log(Level.FINE, ShortcutsManager.class.getName());

		// initialize tables:
		shortcuts = new HashSet<>();
		rejected = new HashSet<>();
		shortcutIndices = new IndexedSetImpl<Shortcut>();

		// initialize varShortcutsMap
		varShortcutsMap = new HashMap<>();
		for (Integer var : manager.getVariables()) {
			varShortcutsMap.put(var, new HashSet<>());
		}

		Set<Integer> inivars = new HashSet<>();
		for (Integer id : manager.getVariables()) {
			if (Choice.getChoice(id) == Choice.CONSTANT) {
				inivars.add(id);
			}
			inivars.add(goal.A);
		}
		initial = new Shortcut(inivars, manager);

		if (!initial.satisfies(goal.getFlatSubsumptions())) {
			FiloLogger.log(Level.FINE, "ShortcutsManager : ERROR: initial  is not a shortcut!!!");
		} else {
			FiloLogger.log(Level.FINE, "ShortcutsManager : Initial for this choice is ");
			printVariables(initial.getVariables(), manager);
		}

		possiblyGoodVar = new HashSet<>();
		goodvar = new HashSet<>();
		for (Integer id : manager.getVariables()) {

			if (Choice.getChoice(id) != Choice.TOP && (!manager.getDecompositionVariables().contains(id)
					|| manager.getConstantDecompositionVariables().contains(id))) {
				possiblyGoodVar.add(id);
			}
		}
		possiblyGoodVar.add(Goal.A);

		FiloLogger.log(Level.FINE, "ShortcutsManager : The possibly good variables are: ");
		printVariables(possiblyGoodVar, manager);

	}

	public int getChange() {
		return change;
	}

	public Shortcut getInitial() {
		return initial;
	}

	public boolean NextShortcuts(Set<FlatSubsumption> flatsubsumptions, AtomManager manager) {
		int oldnbr = shortcuts.size();
		FiloLogger.log(Level.FINE, "ShortcutsManager : Shortcuts size: " + shortcuts.size());
		change = 0;
		Set<Shortcut> newshortcuts = new HashSet<>();
		long length = possiblyGoodVar.size();
		FiloLogger.log(Level.FINE, "ShortcutsManager : There are " + possiblyGoodVar.size() + " possibly good vars");
		int[] array = new int[possiblyGoodVar.size()];
		Arrays.fill(array, 0);
		// for every subset of possibly good variables
		while (nextSubset(array) != null) {
			Iterator<Integer> iter = possiblyGoodVar.iterator();
			Shortcut newshortcut = new Shortcut(new HashSet<>(), manager);
			for (int j = 0; j < length; j++) {
				if (array[j] == 1) {
					Integer in = iter.next();
					newshortcut.add(in, manager);
				} else {
					iter.next();
				}
			}
			FiloLogger.log(Level.FINE, "Possible new shortcut: ");
			newshortcut.print(manager);
			if (!shortcuts.contains(newshortcut) && !rejected.contains(newshortcut)) {
				if (!newshortcut.isEmpty() && newshortcut.satisfies(flatsubsumptions)) {
					FiloLogger.log(Level.FINE, "ShortcutManager: This shortcut satisfies flat subsumptions");

					if (newshortcut.isResolved(this, manager)) {
						FiloLogger.log(Level.FINE, "ShortcutManager: This shortcut is resolved");
						newshortcut.printfull(manager);

						if (!newshortcut.getVariables().contains(Goal.A)) {

							FiloLogger.log(Level.FINE, "ShortcutManager: This shortcut does not contain a constant");
							Integer newshortcutId = shortcutIndices.getIndex(newshortcut);

							newshortcutId = shortcutIndices.addAndGetIndex(newshortcut);
							FiloLogger.log(Level.FINE,
									"ShortcutManager: Adding new shortcut with id: " + newshortcutId);
							newshortcuts.add(newshortcut);
							for (Integer var : newshortcut.getVariables()) {
								Set<Integer> listOfShortcuts = varShortcutsMap.get(var);
								listOfShortcuts.add(newshortcutId);
							}
						} else if (newshortcut.equals(initial)) {
							FiloLogger.log(Level.FINE, "ShortcutManager: SUCCESS (initial is found");
							newshortcut.printfull(manager);
							initial = newshortcut;
							return true;
						} else {
							FiloLogger.log(Level.FINE,
									"This shortcut  contains constant and is not initial. Added to rejected.");
							rejected.add(newshortcut);
						}

					} else
						FiloLogger.log(Level.FINE, "This shortcut is not resolved");

				} else {
					FiloLogger.log(Level.FINE,
							"This shortcut is rejected: empty or does not satisfy flat subsumptions");
					rejected.add(newshortcut);
				}
			} else {
				FiloLogger.log(Level.FINE, "This shortcut has already been checked: in shortcuts or in rejected");
			}
		}

		printShortcuts(manager, newshortcuts);
		shortcuts.addAll(newshortcuts);
		int newnbr = shortcuts.size();
		if (newnbr > oldnbr) {
			FiloLogger.log(Level.FINE, "ShortcutsManager : Oldnumber of shortcuts is smaller than the new number "
					+ oldnbr + " < " + newnbr);
			change++;
			height++;

			return update(manager);
		} else
			return false;

	}

	private boolean update(AtomManager manager) {
		OWLOntologyManager ontmanager = OWLManager.createOWLOntologyManager();
		Renderer renderer = new Renderer(manager, new ShortFormProvider(ontmanager));
		FiloLogger.log(Level.FINE, "ShortcutsManager : In updating function");

		if (shortcuts.isEmpty()) {
			FiloLogger.log(Level.FINE, "ShortcutsManager : The set of shortcuts is empty");
			System.out.println("ShortcutsManager : There are no shortcuts of height " + (height - 1));
		} else {
			FiloLogger.log(Level.FINE, "ShortcutsManager : The set of shortcuts is not empty");
			FiloLogger.log(Level.FINE, "ShortcutsManager : Size of set of good var: " + goodvar.size());
			// Update of good variables
			for (Integer id : possiblyGoodVar) {
				if (!goodvar.contains(id)) {
					for (Shortcut s : shortcuts) {
						if (s.getVariables().contains(id)) {
							goodvar.add(id);
							break;
						}
					}
				}

			}
			FiloLogger.log(Level.FINE, "ShortcutsManager : Size of set of good var after update: " + goodvar.size());

			if (change > 0) {
				FiloLogger.log(Level.FINE, "ShortcutsManager : Change is true");
				Set<Integer> toadd = new HashSet<>();
				FiloLogger.log(Level.FINE,
						"ShortcutsManager : Size of set of possibly good var: " + possiblyGoodVar.size());
				for (Integer id : goodvar) {
					for (Integer childId : manager.getDecompositionVariables())
						if (manager.getDecompositionVariable(childId).getParent() == id
								&& Choice.getChoice(childId) != Choice.TOP)
							toadd.add(childId);
				}

				possiblyGoodVar.addAll(toadd);
				FiloLogger.log(Level.FINE,
						"ShortcutsManager : Size of set of possibly good var after update: " + possiblyGoodVar.size());
			}
		}
		return false;
	}

	public void printVariables(Set<Integer> variables, AtomManager manager) {
		OWLOntologyManager ontmanager = OWLManager.createOWLOntologyManager();
		Renderer renderer = new Renderer(manager, new ShortFormProvider(ontmanager));

		String FINErmation = "";
		for (Integer id : variables) {
			FINErmation = FINErmation + renderer.getShortForm(manager.printConceptName(id)) + " , ";
		}
		FiloLogger.log(Level.FINE, "ShortcutsManager : variables: " + FINErmation);

	}

	public void printShortcuts(AtomManager manager, Set<Shortcut> sc) {
		FiloLogger.log(Level.FINE, "ShortcutsManager : Printing shortcuts ");
		for (Shortcut sh : sc) {

			sh.print(manager);
		}
	}

	public IndexedSet<Shortcut> getShortcutIndices() {
		return shortcutIndices;
	}

	public Set<Shortcut> getShortcuts() {
		return shortcuts;
	}

	public int getHeight() {
		return height;
	}

	private int[] nextSubset(int[] array) {
		int length = array.length;
		int i = length - 1;
		while (i >= 0 && array[i] == 1) {
			i--;
		}
		if (i == -1) {
			FiloLogger.log(Level.FINE, "ShortcutsManager : No more subsets");
			return null;
		} else {
			array[i]++;
			for (int j = i + 1; j < length; j++) {
				array[j] = 0;
			}
		}
		return array;
	}

	public Set<Integer> getGoodVariables() {

		return goodvar;
	}
}
