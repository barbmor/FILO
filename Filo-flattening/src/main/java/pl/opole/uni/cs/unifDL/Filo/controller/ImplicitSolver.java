package pl.opole.uni.cs.unifDL.Filo.controller;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import pl.opole.uni.cs.unifDL.Filo.model.FlatSubsumption;
import pl.opole.uni.cs.unifDL.Filo.model.GenericGoal;
import pl.opole.uni.cs.unifDL.Filo.model.Goal;
import pl.opole.uni.cs.unifDL.Filo.model.GoalSubsumption;
import pl.opole.uni.cs.unifDL.Filo.model.IncreasingSubsumption;
import pl.opole.uni.cs.unifDL.Filo.model.SolvedSubsumption;
import pl.opole.uni.cs.unifDL.Filo.model.StartSubsumption;
import pl.opole.uni.cs.unifDL.Filo.model.Subsumption;
import pl.opole.uni.cs.unifDL.Filo.renderer.ShortFormProvider;
import pl.opole.uni.cs.unifDL.Filo.view.Renderer;

/**
 * This class contains eager rules to be applied to flat subsumptions
 *
 * @author Barbara Morawska
 *
 *
 */

public class ImplicitSolver {
	public static boolean answer = true;
	public static boolean termination = false;

	public static boolean getAnswer() {
		return answer;
	}

	public static void defineStartSubsumptions(Goal goal, AtomManager manager) {
		Integer constant = goal.A;
		for (Integer id : manager.getVariables()) {
			if (Choice.getChoice(id) == Choice.CONSTANT) {
				goal.getStartSubsumptions()
						.add(new StartSubsumption(Collections.singleton(id), Collections.singleton(constant)));
			}
		}
	}

	public static SolvedSubsumption makeSolvedSubsumption(Subsumption sub) {
		return new SolvedSubsumption(sub.getLeft(), sub.getRight());
	}

	// foreign constant for generic goal
	public static void foreignConstantCheck(GenericGoal goal, AtomManager manager) {
		Set<GoalSubsumption> todelete = new HashSet<>();
		Integer constant = goal.A;
		for (GoalSubsumption sub : goal.getFlatSubsumptions()) {
			Set<Integer> left = sub.getLeft();
			Set<Integer> right = sub.getRight();
			boolean control = true;
			for (Integer id : right) {
				if (manager.getAtom(id).isConstant() && id != constant) {
					todelete.add(sub);
					control = false;
					break;
				}
			}
			if (control == true) {
				Set<Integer> idsToDelete = new HashSet<>();
				for (Integer id : left) {
					if (manager.getAtom(id).isConstant() && id != constant) {
						idsToDelete.add(id);
					}
				}
				for (Integer id : idsToDelete) {
					left.remove(id);
				}
			}
		}

		for (GoalSubsumption sub : todelete) {
			goal.flatsubsumptions.remove(sub);
		}

	}

	// foreign constant for goal -- to remove
	public static void foreignConstantCheck(Goal goal, AtomManager manager) {
		Set<GoalSubsumption> todelete = new HashSet<>();
		Integer constant = Goal.A;
		for (GoalSubsumption sub : goal.getFlatSubsumptions()) {
			Set<Integer> left = sub.getLeft();
			Set<Integer> right = sub.getRight();
			boolean control = true;
			for (Integer id : right) {
				if (manager.getAtom(id).isConstant() && id != constant) {
					todelete.add(sub);
					control = false;
					break;
				}
			}
			if (control == true) {
				Set<Integer> idsToDelete = new HashSet<>();
				for (Integer id : left) {
					if (manager.getAtom(id).isConstant() && id != constant) {
						idsToDelete.add(id);
					}
				}
				for (Integer id : idsToDelete) {
					left.remove(id);
				}
			}
		}

		for (GoalSubsumption sub : todelete) {
			// logger.info("Deleting subsumption with foreign constant on the right");
			goal.deleteFlatSubsumption(sub);
			goal.getSolvedSubsumptions().add(sub);
		}
	}

	// the first rule
	// Not exactly, because it does not check for a value restriction with a top
	// variable is on the right side of subsumption
	// If this is corrected we can get more cases at once
	public static void firstCheck(Goal goal, AtomManager manager) {
		Set<GoalSubsumption> todelete = new HashSet<>();
		for (GoalSubsumption sub : goal.getFlatSubsumptions()) {
			Set<Integer> right = sub.getRight();
			for (Integer atomId : right) {
				if (manager.getAtom(atomId).isVariable() && Choice.getChoice(atomId) == Choice.TOP) {
					todelete.add(sub);
				}
			}
		}

		for (GoalSubsumption sub : todelete) {
			// logger.info("Adding subsumption to solved subsumptions");
			goal.deleteFlatSubsumption(sub);
			goal.getSolvedSubsumptions().add(sub);
		}
	}

	// the second rule
	public static void secondCheck(Goal goal, AtomManager manager) {
		Set<GoalSubsumption> todeleteTOP = new HashSet<>();
		for (GoalSubsumption sub : goal.getFlatSubsumptions()) {

			for (Integer id : sub.getLeft()) {
				if (manager.getAtom(id).isVariable() && Choice.getChoice(id) == Choice.TOP) {
					todeleteTOP.add(sub);
				}
			}
		}
		if (todeleteTOP.size() > 0) {
			for (GoalSubsumption sub : todeleteTOP) {
				goal.getFlatSubsumptions().remove(sub);
				Set<Integer> todeleteID = new HashSet<>();
				for (Integer id : sub.getLeft()) {
					if (manager.getAtom(id).isVariable() && Choice.getChoice(id) == Choice.TOP) {
						todeleteID.add(id);
					}
				}
				sub.getLeft().removeAll(todeleteID);
				goal.getFlatSubsumptions().add((FlatSubsumption) sub);
			}
		}
	}

	// the third rule
	public static void thirdCheck(Goal goal, AtomManager manager) {
		Set<GoalSubsumption> todelete = new HashSet<>();

		for (GoalSubsumption sub : goal.getFlatSubsumptions()) {
			for (Integer id : sub.getRight()) {
				// logger.info("Checking subsumption: " + sub.toString());

				if (sub.getLeft().contains(id)) {
					// logger.info("to delete sub: " + sub.toString());
					todelete.add(sub);
					break;
				}
			}
		}
		goal.getFlatSubsumptions().removeAll(todelete);
		goal.getSolvedSubsumptions().addAll(todelete);
	}

	// the fourth and fifth and seventh rule
	public static void fourthCheck(Goal goal, AtomManager manager) {
		boolean found = true;
		//Integer constant = Goal.A;
		Set<GoalSubsumption> todelete = new HashSet<>();
		for (GoalSubsumption sub : goal.getFlatSubsumptions()) {

			// checking sub
			for (Integer id : sub.getRight()) {
				if (answer && manager.getAtom(id).isConstant()) {
					FiloLogger.log(Level.FINE, "Implicit Solver checking constant on the right");
					found = false;
					if (sub.getLeft().size() > 0) {
						for (Integer idLeft : sub.getLeft()) {
							if (manager.getAtom(idLeft).isConstant() || (manager.getAtom(idLeft).isVariable()
									&& Choice.getChoice(idLeft) == Choice.CONSTANT)) {
								// FOURTH check
								todelete.add(sub);
								found = true;
								break;
							}
						}
					}
					if (!found) {
						answer = false;
						FiloLogger.log(Level.FINE, "Implicit Solver should return with the answer false");
						return;
					}

				} else if (answer && manager.getAtom(id).isVariable() && Choice.getChoice(id) == Choice.CONSTANT) {
					// SEVENTH check

					found = false;
					if (sub.getLeft().size() > 0) {
						for (Integer idLeft : sub.getLeft()) {
							if (manager.getAtom(idLeft).isConstant() || (manager.getAtom(idLeft).isVariable()
									&& Choice.getChoice(idLeft) == Choice.CONSTANT)) {
								found = true;
								break;
							}
						}
					}
					if (!found) {
						answer = false;
						FiloLogger.log(Level.FINE, "Implicit Solver should return with the answer false");
						return;
					}
				}
			}
		}

		// for FOURTH check
		if (answer) {
			goal.getFlatSubsumptions().removeAll(todelete);
			goal.getSolvedSubsumptions().addAll(todelete);
		}
	}

	// the sixth rule (non-critical):
	// delete constant A from the left side if var on the right does not contain A
	public static boolean sixthCheck(Goal goal, AtomManager manager) {
		Integer A = Goal.A;
		for (GoalSubsumption sub : goal.getFlatSubsumptions()) {
			Set<Integer> todelete = new HashSet<>();
			for (Integer id : sub.getRight()) {
				if (manager.getAtom(id).isVariable() && Choice.getChoice(id) != Choice.CONSTANT) {
					for (Integer leftid : sub.getLeft()) {
						if (leftid == A) {
							todelete.add(leftid);
						}
					}
				}

			}

			if (todelete.size() > 0) {
				sub.getLeft().removeAll(todelete);
			}
		}
		return true;
	}

	// the eight rule:
	// If left side is 1 atom or empty, the checks are more strict than in check
	// fourth
	public static void eighthCheck(Goal goal, AtomManager manager) {
		for (GoalSubsumption sub : goal.getFlatSubsumptions()) {
			for (Integer id : sub.getRight()) {
				if (manager.getAtom(id).isVariable()) {
					if (sub.leftIsConstantEquiv(manager)) {
						if (Choice.getChoice(id) == Choice.NOTHING) {
							answer = false;
						} else {
							for (GoalSubsumption isub : goal.getIncreasingSubsumptions()) {
								if (isub.getLeft().contains(id)) {
									// logger.info("Increasing sub found");
									for (Integer rightId : isub.getRight()) {
										Integer childId = manager.getChild(rightId);
										if (Choice.getChoice(childId) != Choice.TOP) {
											answer = false;
											break;
										}
									}
								}
							}
						}
					} else if (sub.leftIsTopEquiv(manager)) {
						if (Choice.getChoice(id) != Choice.TOP) {
							answer = false;
							break;
						}
					}
				}
			}
		}
	}

	public static void resetImplicitSolver() {
		answer = true;
		termination = false;
	}

	// the ninth rule
	public static boolean ninthCheck(Goal goal) {

		if (goal.getFlatSubsumptions().isEmpty()) {
			termination = true;
			FiloLogger.log(Level.INFO, "Implicit Solver: The goal is unifiable SUCCESS");

			return true;
		}
		return false;
	}

	public static boolean allChecks(Goal goal, AtomManager manager) {
		answer = true;
		foreignConstantCheck(goal, manager);
		firstCheck(goal, manager);
		secondCheck(goal, manager);
		thirdCheck(goal, manager);
		fourthCheck(goal, manager);
		sixthCheck(goal, manager);
		eighthCheck(goal, manager);
		ninthCheck(goal);

		return answer;
	}

	public static boolean criticalChecks(Goal goal, AtomManager manager) {
		resetImplicitSolver();
		fourthCheck(goal, manager);
		if (answer)
			eighthCheck(goal, manager);
		return answer;
	}

	public static boolean nonfailingChecks(Goal goal, AtomManager manager) {
		firstCheck(goal, manager);
		secondCheck(goal, manager);
		thirdCheck(goal, manager);
		sixthCheck(goal, manager);
		ninthCheck(goal);
		return answer;
	}
}
