package pl.opole.uni.cs.unifDL.Filo.controller;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import com.google.common.collect.Iterables;

import pl.opole.uni.cs.unifDL.Filo.model.*;
import pl.opole.uni.cs.unifDL.Filo.renderer.ShortFormProvider;
import pl.opole.uni.cs.unifDL.Filo.view.Renderer;

/**
 * A model reader contains all the tools needed for normalizing and flattening
 * subsumptions for the unificator. Contains only static methods.
 * 
 * The flattening performed is non-deterministic and can lead to immediate
 * solution: success or failure.
 * 
 * Unificator can be forced to backtrack in the case of failure.
 * 
 * @author Barbara Morawska
 * @author Dariusz Marzec
 */

public class ModelReader {
	static Logger logger = Logger.getLogger(ModelReader.class.getName());
	static Model filomodel;
	static AtomManager atomManager;
	static ShortFormProvider renderer;

	// method normalizing all subsumptions in filomodel
	public static Set<Subsumption> normalize() {
		Set<Subsumption> newsubsumptions = new HashSet<>();

		for (Equation equation : filomodel.getEquations()) {
			newsubsumptions.addAll(ModelReader.normalize(equation));
		}
		for (Definition def : filomodel.getDefinitions()) {
			newsubsumptions.addAll(ModelReader.normalize(def));
		}
		for (Subsumption subsumption : filomodel.getSubsumptions()) {
			newsubsumptions.addAll(ModelReader.normalize(subsumption));
		}

		return newsubsumptions;
	}

	public static Collection<? extends Subsumption> normalize(Equation eq) {
		Set<Integer> leftside = eq.getLeft();
		Set<Integer> rightside = eq.getRight();
		Set<Subsumption> newsubsumptions = new HashSet<>();
		Subsumption sub1 = new Subsumption(leftside, rightside);
		Subsumption sub2 = new Subsumption(rightside, leftside);
		newsubsumptions.addAll(normalize(sub1));
		newsubsumptions.addAll(normalize(sub2));

		return newsubsumptions;

	}

	public static Collection<? extends Subsumption> normalize(Definition def) {
		Set<Integer> leftside = def.getLeft();
		Set<Integer> rightside = def.getRight();
		Set<Subsumption> newsubsumptions = new HashSet<>();

		Equation eq = new Equation(leftside, rightside);
		newsubsumptions.addAll(normalize(eq));

		return newsubsumptions;
	}

	public static Collection<? extends Subsumption> normalize(Subsumption sub) {
		Set<Integer> leftside = sub.getLeft();
		Set<Integer> rightside = sub.getRight();
		Set<Subsumption> newsubsumptions = new HashSet<>();
		if (rightside.size() > 1) {
			for (Integer atomId : rightside) {
				Set<Integer> newrightside = new HashSet<>();
				newrightside.add(atomId);
				Subsumption newsubsumption = new Subsumption(leftside, newrightside);
				newsubsumptions.add(newsubsumption);
			}
		} else if (rightside.size() == 1) {
			newsubsumptions.add(sub);

		} else {

			// ToDo: throw some error here
		}

		return newsubsumptions;
	}

///////////////////////////Flattening methods/////////////////////////////////	
	// Have to do double pass flattening because of constants

	public static boolean isRightVarLeftFlat(Subsumption sub) {
		Set<Integer> left = sub.getLeft();
		Set<Integer> right = sub.getRight();
		boolean answer = true;
		for (Integer id : right) {
			if (atomManager.getAtom(id).isConstant() || atomManager.getAtom(id).isValueRestriction()) {
				System.out.println("Expected variable on the right side, but got something else");
				answer = true;
				break;
			}
		}

		for (Integer id : left) {
			if (atomManager.getAtom(id).isConstant()) {
				answer = false;
				break;
			}
		}

		return answer;
	}

	// main flattening method
	public static Set<GoalSubsumption> mainflattening(Integer A, Subsumption sub) {
		FiloLogger.log(Level.FINE, "ModelReader: mainflattening method");

		for (Integer atomRight : sub.getRight()) {
			if (atomManager.getAtom(atomRight).isValueRestriction()) {
				Integer roleId = atomManager.getValueRestriction(atomRight).getRoleId();
				return (Set<GoalSubsumption>) flatten(A, sub, roleId);
			}
			// Flattening case 2: constant on the right (This is not needed, since Implicite
			// Rules will
			// catch it
			else if (atomManager.getAtom(atomRight).isConstant()) {
				//TODO: change this...
				return (Collections.singleton(flattenConstantRight(A, sub)));
				//return (Collections.singleton(new FlatSubsumption(sub.getLeft(), sub.getRight())));
			}
			// Flattening case 3: variable on the right
			else if (atomManager.getAtom(atomRight).isVariable()) {
				FiloLogger.log(Level.FINE,
						"ModelReader : in mainflattening method calling fullvariableflatten on subsumption that may be flat");
				return (Set<GoalSubsumption>) fullvariableflatten(sub, A);
			}
		}
		System.out.println("Unexpected right side of a subsmption");
		return null;
	}

	private static GoalSubsumption flattenConstantRight(Integer A, Subsumption sub) {
		
		Set<Integer> newleftside = new HashSet<>();
		Set<Integer> newrightside = new HashSet<>();
		newrightside.addAll(sub.getRight());

		for (Integer id : sub.getLeft()) {
			if (id == A || atomManager.getAtom(id).isVariable())
				newleftside.add(id);
		}

		return new FlatSubsumption(newleftside, newrightside);
	}

	public static Collection<GoalSubsumption> flatten(Integer A, Subsumption sub, Integer rolename) {
		Set<Integer> leftside = sub.getLeft();
		Set<Integer> rightside = sub.getRight();
		Set<Integer> newleftside = new HashSet<>();
		Set<Integer> newrightside = new HashSet<>();
		Set<GoalSubsumption> newsubsumptions = new HashSet<>();
		boolean foreignconstant = false;
		// right side
		for (Integer atomId : rightside) {
			if (atomManager.getValueRestriction(atomId).getRoleId() == rolename) {
				Integer child = atomManager.getValueRestriction(atomId).getConceptName();
				if (atomManager.getAtom(child).isConstant() && child != A) {
					foreignconstant = true;
				} else {
					newrightside.add(atomManager.getValueRestriction(atomId).getConceptName());
				}
			} else {
				FiloLogger.log(Level.FINE,"Wrong role name on the right of subsumption");
				return null;
			}
		}

		// left side
		if (!foreignconstant) {
			for (Integer atomId : leftside) {
				if (atomManager.getAtom(atomId).isValueRestriction()) {
					if (atomManager.getValueRestriction(atomId).getRoleId() == rolename) {
						Integer child = atomManager.getValueRestriction(atomId).getConceptName();
						if (!atomManager.getAtom(child).isConstant() || child == A) {
							newleftside.add(atomManager.getValueRestriction(atomId).getConceptName());
						}
					}
				} else if (atomManager.getAtom(atomId).isVariable()) {
					// decomposition variable
					Integer parentId = atomId;
					Integer decVarId = atomManager.createDecompositionVariable(atomId, rolename, renderer);
					atomManager.makeDecompositionVariable(decVarId);
					newleftside.add(decVarId);
					Set<Integer> incrLeft = new HashSet<Integer>();
					incrLeft.add(parentId);
					Set<Integer> incrRight = new HashSet<Integer>();
					incrRight.add(atomManager.createValueRestriction(atomManager.getRoleName(rolename), decVarId));
					newsubsumptions.add(new IncreasingSubsumption(incrLeft, incrRight));
				}
			}
			FlatSubsumption newSub = new FlatSubsumption(newleftside, newrightside);
			newsubsumptions.add(newSub);
		} else {
			// Here adding subsumption which is perhaps not flat, but have top on the right
			// side
			FlatSubsumption newSub = new FlatSubsumption(leftside, newrightside);
			newsubsumptions.add(newSub);
		}
		return newsubsumptions;
	}

	private static GoalSubsumption secondflattening(Integer A, GoalSubsumption newSub) {

		return (GoalSubsumption) fullvariableflatten(newSub, A);
	}

	public static Set<GoalSubsumption> fullvariableflatten(Subsumption sub, Integer constant) {

		//Set<Integer> leftside = sub.getLeft();
		Set<Integer> rightside = sub.getRight();
		//Set<Integer> newleftside = new HashSet<>();
		//Set<Integer> newrightside = new HashSet<>();

		Set<GoalSubsumption> newsubsumptions = new HashSet<>();
		// right side
		for (Integer atomId : rightside) {

			if (atomManager.getAtom(atomId).isVariable()) {
				FiloLogger.log(Level.FINE, "ModelReader : Full variable flattening on variable "
						+ renderer.getShortForm(atomManager.printConceptName(atomId)));
				for (Integer roleid : atomManager.getRoleIds()) {
					newsubsumptions.addAll(variableflatten(sub, roleid, constant));
				}

				newsubsumptions.add(simpleConstantflatten(sub, constant));

			} else {
				FiloLogger.log(Level.WARNING,
						"ModelReader : (fullvariableflatten) Expected variable on the right side of subsumption, but instead got something else.");
				return null;
			}
		}
		return newsubsumptions;
	}

	private static Collection<? extends GoalSubsumption> variableflatten(Subsumption sub, Integer roleid, Integer A) {
		Set<Integer> right = sub.getRight();
		Set<Integer> left = sub.getLeft();
		Set<Integer> newright = new HashSet<>();
		Set<Integer> newleft = new HashSet<>();
		Set<GoalSubsumption> newsubsumptions = new HashSet<>();
		for (Integer id : right) {
			if (atomManager.getAtom(id).isVariable()) {
				Integer parentId = id;
				Integer decVarId = atomManager.createDecompositionVariable(id, roleid, renderer);
				atomManager.makeDecompositionVariable(decVarId);
				newright.add(decVarId); // add to right side

				// increasing subsumption
				// Add only if there is no one already
				Set<Integer> incrLeft = new HashSet<Integer>();
				incrLeft.add(parentId);
				Set<Integer> incrRight = new HashSet<Integer>();
				incrRight.add(atomManager.createValueRestriction(atomManager.getRoleName(roleid), decVarId));
				newsubsumptions.add(new IncreasingSubsumption(incrLeft, incrRight));
			} else {
				FiloLogger.log(Level.FINE,
						"ModelReader: Expected variable on the right side, instead got something else");
				return null;
			}
		}

		for (Integer id : left) {
			if (atomManager.getAtom(id).isVariable()) {
				Integer parentId = id;
				Integer decVarId = atomManager.createDecompositionVariable(id, roleid, renderer);
				atomManager.makeDecompositionVariable(decVarId);
				newleft.add(decVarId); // add to right side

				// increasing subsumption
				// Add only if there is no one already
				Set<Integer> incrLeft = new HashSet<Integer>();
				incrLeft.add(parentId);
				Set<Integer> incrRight = new HashSet<Integer>();
				incrRight.add(atomManager.createValueRestriction(atomManager.getRoleName(roleid), decVarId));
				newsubsumptions.add(new IncreasingSubsumption(incrLeft, incrRight));
			} else if (atomManager.getAtom(id).isValueRestriction()
					&& atomManager.getValueRestriction(id).getRoleId() == roleid) {
				Integer newId = atomManager.getValueRestriction(id).getConceptName();
				if (!atomManager.getAtom(newId).isConstant() || newId == A)
					newleft.add(newId);
			}

		}
		FlatSubsumption newsub = new FlatSubsumption(newleft, newright);
		newsubsumptions.add(newsub);
		return newsubsumptions;
	}

	public static GoalSubsumption simpleConstantflatten(Subsumption sub, Integer constant) {
		Set<Integer> newleftside = new HashSet<>();
		Set<Integer> newrightside = new HashSet<>();

		for (Integer id : sub.getRight()) {
			if (atomManager.getAtom(id).isVariable()) {
				Integer DecVar = atomManager.createConstantDecompositionVariable(id, constant, renderer);

				newrightside.add(DecVar);
			} else {
				FiloLogger.log(Level.FINE,
						"ModelReader: Expected variable on the right of subsumption, but got something else");
				return null;
			}
		}

		for (Integer id : sub.getLeft()) {
			if (id == constant || atomManager.getAtom(id).isVariable())
				newleftside.add(id);
		}

		return new FlatSubsumption(newleftside, newrightside);
	}

/////////////setters/getters/////////////////////

	static void setModel(Model oldfilomodel) {
		filomodel = oldfilomodel;
	}

	static Model getModel() {
		return filomodel;
	}

	static void setAtomManager(AtomManager oldAtomManager) {
		atomManager = oldAtomManager;
	}

	public static AtomManager getAtomManager() {
		return atomManager;
	}

	static void setRenderer(ShortFormProvider formProvider) {
		renderer = formProvider;
	}

	public static ShortFormProvider getRenderer() {
		return renderer;
	}
}
