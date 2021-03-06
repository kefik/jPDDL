package cz.cuni.mff.bw.problem2;

import cz.cuni.mff.bw.domain.Domain;
import cz.cuni.mff.bw.domain.State;
import cz.cuni.mff.bw.problem.BwProblem;
import cz.cuni.mff.jpddl.PDDLDeadEnd;

public final class Problem extends BwProblem {
	
	static {
		// ENSURE STATIC INITIALIZATION OF THE CLASSES
		new E_Blocks();
		new E_Slipperys();
		new E_Stickys();
	}
	
	public Domain domain;
	
	public State state;
	
	public Goal goal;
	
	public DeadEnd deadEnd;
	
	public Problem() {
		domain = new Domain();
		state = new State();
		goal = new Goal();
		deadEnd = new DeadEnd();
		
		state.p_HandEmpty.set(E_Slipperys.h1);
		state.p_HandEmpty.set(E_Slipperys.h2);
		
		state.p_On.set(E_Blocks.b1, E_Blocks.b2);
		state.p_On.set(E_Blocks.b2, E_Blocks.b4);
		state.p_On.set(E_Blocks.b5, E_Blocks.b3);
		state.p_OnTable.set(E_Blocks.b3);
		state.p_OnTable.set(E_Blocks.b4);
		state.p_Clear.set(E_Blocks.b1);
		state.p_Clear.set(E_Blocks.b5);
		state.p_Movable.set(E_Blocks.b1);
		state.p_Movable.set(E_Blocks.b2);
		state.p_Movable.set(E_Blocks.b3);
		state.p_Movable.set(E_Blocks.b4);
		state.p_Movable.set(E_Blocks.b5);
	}
	
	@Override
	public String getName() {
		return "BlocksWorld-Problem";
	}
	
	@Override
	public Domain getDomain() {
		return domain;
	}

	@Override
	public State getState() {
		return state;
	}

	@Override
	public Goal getGoal() {
		return goal;
	}	
	
	@Override
	public PDDLDeadEnd getDeadEnd() {
		return deadEnd;
	}
	
}
