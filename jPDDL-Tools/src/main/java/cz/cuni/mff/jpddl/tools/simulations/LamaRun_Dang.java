package cz.cuni.mff.jpddl.tools.simulations;

import cz.cuni.mff.jpddl.*;
import cz.cuni.mff.jpddl.tools.planners.Lama;
import cz.cuni.mff.jpddl.tools.search.bench.Timed;
import cz.cuni.mff.jpddl.tools.utils.CSV;
import cz.cuni.mff.jpddl.tools.validators.IPlanValidator;
import cz.cuni.mff.jpddl.tools.validators.PlanChecker;
import cz.cuni.mff.jpddl.utils.IEventSelector;
import cz.cuni.mff.jpddl.utils.StateCompact;

import java.io.File;
import java.util.*;

public class LamaRun_Dang {
	
	public static enum LamaRunResult {
		/**
		 * We got to the goal state.
		 */
		GOAL_ACHIEVED,
		
		/**
		 * Maximum number of iterations were reached.
		 */
		MAX_ITERATIONS_REACHED,
		
		/**
		 * We got to the dead end.
		 */
		DEAD_END
		
	}

	private boolean event_applied = false;

	private List<PDDLEffector> events = new ArrayList<PDDLEffector>();
	
	private int action = 0;
	
	private long planningMillis = 0;

	private void simulateEvent(PDDLProblem problem, Random random, IEventSelector eventSelector) {
		// COLLECT APPLICABLE EVENTS
		event_applied = false;
		events.clear();
		events.add(null); // add NO-EVENT
		problem.getApplicables().collectApplicableEvents(problem.getDomain(), problem.getState(), events);
		
		Collections.shuffle(events); // randomize

		List<PDDLEffector> selected_events = eventSelector.select(problem, events);

		for (PDDLEffector ev : selected_events) {
			ev.apply(problem.getState());
			event_applied = true;
			System.out.println("    +-- EVENT [" + action + ".]: " + ev.toEffector());
		}

		if (!event_applied)
			System.out.println("    +-- EVENT [" + action + ".]: no-event");
	}
	
	public void run(String id, PDDLProblem problem, PlanChecker planChecker, IPlanValidator validator, int maxIterations, long randomSeed, File csvOutputFile, IEventSelector event_selector) {
		StateCompact initialState = problem.getState().getDynamic().clone();
		
		Random random = new Random(randomSeed);
		
		Timed allTime = new Timed();
		Timed planningTime = new Timed();
		Timed validationTime = new Timed();
				
		planningMillis = 0;
		long validatingMillis = 0;
		
		allTime.start();
		
		System.out.println();
		System.out.println("---------");
		System.out.println("LAMA RUN: " + id);
		System.out.println("---------");
		System.out.println("  +-- Validator: " + validator.getDescription());
		
		// RESET
		events.clear();
		action = 0;
		
		// INIT LAMA
		File domainFile = problem.getDomain().getDomainPureFile();		
		File flatDomainFile = problem.getDomain().getDomainFlatFile();		
		Lama lama = new Lama();
		
		// INTERNAL VARS
		int iteration = 0;
		
		// RESULT
		LamaRunResult result = LamaRunResult.GOAL_ACHIEVED;

		System.out.println("  +-- Planning...");
		File problemFile = new File("problem.pddl");
		problem.createProblemFile(problemFile, problem.getState());
		planningTime.start();
		List<PDDLStringInstance> lamaPlan = lama.plan(domainFile, problemFile);
		planningTime.end();
		planningMillis += planningTime.durationMillis;
		problemFile.delete();
		System.out.println(problem.dang());
		// ALGORITHM 3
		while (true) {

			System.out.println(problem.dang());

			if (problem.getGoal().isAchieved(problem.getState())) {
				System.out.println("  +-- GOAL ACHIEVED!");
				result = LamaRunResult.GOAL_ACHIEVED;
				break;
			}
			
			if (iteration >= maxIterations) {
				result = LamaRunResult.MAX_ITERATIONS_REACHED;
				break;
			}
			
			iteration++;
			System.out.println("ITERATION " + iteration);

			if (problem.getDeadEnd().isDeadEnd(problem.getState())) {
				System.out.println("  +-- GOT INTO THE DEAD END STATE!");
				result = LamaRunResult.DEAD_END;
				break;
			} else {
				System.out.println("  +-- Not in dead-end state.");								
			}

			int dang = problem.dang();
			if (dang < 3) {
				System.out.println("  +-- Current state is dangerous, dang: " + dang);
				System.out.println("  +-- Planning to safety... " + problem.getClosestSafeState());
				problemFile = new File("problem.pddl");
				problem.createProblemFile(problemFile, problem.getState(), problem.getClosestSafeState());
				planningTime.start();
				lamaPlan = lama.plan(domainFile, problemFile);
				planningTime.end();
				planningMillis += planningTime.durationMillis;
				problemFile.delete();
				if (lamaPlan == null)
					System.out.println("  +-- LAMA FAILED TO FIND THE PLAN!");
			}

			// PLAN
			if (lamaPlan == null || lamaPlan.isEmpty()) {
				System.out.println("  +-- Planning...");
				problemFile = new File("problem.pddl");
				problem.createProblemFile(problemFile, problem.getState());
				planningTime.start();
				lamaPlan = lama.plan(domainFile, problemFile);
				planningTime.end();
				planningMillis += planningTime.durationMillis;
				problemFile.delete();
			}
			if (lamaPlan == null) {
				System.out.println("  +-- LAMA FAILED TO FIND THE PLAN!");
				simulateEvent(problem, random, event_selector);
			} else {
				// just obtained a new plan, no events happened yet
				event_applied = false;
				// TRANSLATE PLAN
				PDDLEffector[] plan = problem.getDomain().toEffectors(lamaPlan.toArray(new PDDLStringInstance[0]));			
				System.out.println("  +-- Plan has " + plan.length + " steps");

				if (!plan[0].isApplicable(problem.getState())) {
					System.out.println("    +-- Action: " + plan[0].toEffector() + " is NOT APPLICABLE, re-planning!");
					iteration--; 	 // An event made the first action non-applicable -> replan
					lamaPlan = null; // (and do not increase iteration number, we made no steps)
				}
				else {
					int toExecuteActions = 1;

					System.out.println("  +-- Executing plan, actions[0-" + (toExecuteActions - 1) + "]");
					for (int i = 0; i < toExecuteActions; ++i) {
						if (!plan[i].isApplicable(problem.getState())) {
							System.out.println("    +-- Action[" + i + "/" + (++action) + "]: " + plan[i].toEffector() + " is NOT APPLICABLE, terminating the plan execution!");
							break;
						}
						System.out.println("    +-- ACTION[" + (++action) + ".]: " + plan[i].toEffector());
						plan[i].apply(problem.getState());
						lamaPlan.remove(0);
						if (problem.getGoal().isAchieved(problem.getState())) {
							System.out.println("    +-- GOAL ACHIEVED!");
							result = LamaRunResult.GOAL_ACHIEVED;
							break;
						}
						simulateEvent(problem, random, event_selector);
					}
				}
			}
		}
		
		System.out.println("LamaRun Finished");
		System.out.println("  +-- result " + result);
		System.out.println("  +-- iterations " + iteration);
		System.out.println("  +-- total actions " + action);
		allTime.end();
		
		long simulationMillis = allTime.durationMillis - planningMillis - validatingMillis;
		
		System.out.println("  +-- total time   " + Timed.getTimeString(allTime.durationMillis));
		System.out.println("    +-- planning   " + Timed.getTimeString(planningMillis));
		System.out.println("    +-- validation " + Timed.getTimeString(validatingMillis));
		System.out.println("    +-- simulation " + Timed.getTimeString(simulationMillis));
		
		outputToCSV(csvOutputFile, id, problem, validator, result, iteration, allTime.durationMillis, planningMillis, validatingMillis, simulationMillis, randomSeed, maxIterations);
		
		problem.getState().setDynamic(initialState);
	}

	private PDDLEffector[] improvePlan(PDDLProblem problem, Lama lama, File flatDomainFile, PDDLEffector[] plan, int firstSafeStateIndex, Timed planningTime) {
		// BACKUP STATE
		StateCompact currentState = problem.getState().getDynamic().clone();
		
		// GET TO SAFE STATE
		for (int i = 0; i < firstSafeStateIndex; ++i) {
			plan[i].apply(problem.getState());
		}
		
		// SAVE THE SAFE STATE
		StateCompact targetState = problem.getState().getDynamic().clone();
		
		// RESTORE THE CURRENT STATE
		problem.getState().setDynamic(currentState);
		
		// GENERATE PROBLEM FILE
		File problemFile = new File("problem-flat.pddl");
		problem.createProblemFile(problemFile, problem.getState(), targetState);
		
		// PLAN!
		planningTime.start();
		List<PDDLStringInstance> lamaPlan = lama.plan(flatDomainFile, problemFile);
		planningTime.end();
		planningMillis += planningTime.durationMillis;
		problemFile.delete();
		
		if (lamaPlan == null) {
			System.out.println("    +-- LAMA FAILED TO FIND IMPROVEMENT!");
			return null;
		}
		
		// TRANSLATE PLAN
		PDDLEffector[] improvement = problem.getDomain().toEffectors(lamaPlan.toArray(new PDDLStringInstance[0]));			
		System.out.println("    +-- Improvement has " + plan.length + " steps, merging with original plan");
		
		List<PDDLEffector> result = new ArrayList<PDDLEffector>();
		for (int i = 0; i < improvement.length; ++i) 
			if (improvement[i] != null) result.add(improvement[i]);
		for (int i = firstSafeStateIndex; i < plan.length; ++i)
			result.add(plan[i]);
		
		return result.toArray(new PDDLEffector[0]);
	}

	private void outputToCSV(File csvOutputFile, String id, PDDLProblem problem, IPlanValidator validator, LamaRunResult result, int iterations,
			long durationMillis, long planningMillis, long validatingMillis, long simulationMillis, long randomSeed,
			int maxIterations) {
		
		System.out.println("  +-- appending result into " + csvOutputFile.getAbsolutePath());
		
		Date now = Calendar.getInstance().getTime();
		
		CSV.appendCSVRow(csvOutputFile, 
			new String[] {"date", "id", "problem",            "validator",                 "result", "iterations", "durationMillis", "planningMillis", "validatingMillis", "simulationMillis", "randomSeed", "maxIterations"},
			               now,    id,   problem.getClass(),   validator.getDescription(),  result,   iterations,   durationMillis,   planningMillis,   validatingMillis,   simulationMillis,   randomSeed,   maxIterations			               
		);
		
	}
	
}
