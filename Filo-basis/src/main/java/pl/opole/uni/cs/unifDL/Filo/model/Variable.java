package pl.opole.uni.cs.unifDL.Filo.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import org.semanticweb.owlapi.apibinding.OWLManager;

import pl.opole.uni.cs.unifDL.Filo.controller.AtomManager;
import pl.opole.uni.cs.unifDL.Filo.controller.FiloLogger;
import pl.opole.uni.cs.unifDL.Filo.renderer.Renderer;
import pl.opole.uni.cs.unifDL.Filo.renderer.ShortFormProvider;

/** 
 * @author Michał Henne
 * @author Sławomir Kost
 * @author Dariusz Marzec
 * @author Barbara Morawska
 */
public class Variable extends ConceptName {

	private List<Integer> children;

	public Variable(Integer conceptNameId) {
		super(conceptNameId);
		this.makeVariable();
		children = new ArrayList<>();

	}

	public List<Integer> getChildren() {
		return children;
	}

	public boolean fullyDecomposed(AtomManager manager) {

		boolean found = true;
		Iterator<Integer> roleIt = manager.getRoleIds().iterator();

		while (found && roleIt.hasNext()) {
			Integer role = roleIt.next();

			found = false;
			for (Integer child : manager.getDecompositionVariables()) {

				if (manager.getDecompositionVariable(child).getRole() == role
						&& manager.getDecompositionVariable(child).getParent() == this.getConceptNameId()) {
					found = true;
				}
			}
		}
		if (found) {
			found = false;
			for (Integer child : manager.getConstantDecompositionVariables()) {
				if (manager.getDecompositionVariable(child).getParent() == this.getConceptNameId())
					found = true;
				break;
			}
		}

		return found;

	}

	public void addChild(Integer dec) {
		children.add(dec);

	}

}