package cz.cuni.mff.jpddl;

import cz.cuni.mff.jpddl.store.ICloneable;
import cz.cuni.mff.jpddl.utils.StateCompact;

public abstract class PDDLEffector implements ICloneable {

	@Override
	public abstract PDDLEffector clone();
	
	/**
	 * Returns the instance back to the pool. Call this method before you discard last pointers to the effector.
	 */
	public abstract void recycle();
	
	/**
	 * Returns PDDL name of the effector.
	 * @return
	 */
	public abstract String getName();
	
	/**
	 * Returns PDDL representation of the effector instance.
	 * @return
	 */
	public abstract String toEffector();
	
	/**
	 * Instantiates a new PDDLEffector; a factory method.
	 * @return
	 */
	public abstract PDDLEffector create();
	
	/**
	 * Rewrite arguments of this effector into 'assignInto'. 'assignInto' must be of the same type!
	 * @param assignInto
	 */
	public abstract void rewrite(PDDLEffector assignInto);
	
	/**
	 * Convert String arguments into {@link PDDLType}s and assign then into this effector.
	 * @param args
	 */
	public abstract void assign(String[] args);
	
	/**
	 * Nullify all effectors arguments.
	 */
	public abstract void reset();
	
	/**
	 * Is action applicable in the given 'state'?
	 * @param state
	 * @return
	 */
	public abstract boolean isApplicable(PDDLState state);
	
	/**
	 * Is action applicable in the: state / minusState ?
	 * @param state
	 * @param minusState
	 * @return
	 */
	public abstract boolean isApplicable(PDDLState state, PDDLState minusState);
	
	/**
	 * Apply effects in the given 'state' without checking preconditions.
	 * @param state
	 */
	public abstract void apply(PDDLState state);
	
	/**
	 * Reverse effects in the given 'state' without any checks assuming effects can be reverted.
	 * @param state
	 */
	public abstract void reverse(PDDLState state);
	
	/**
	 * Adds POSITIVE effects of the action into the 'compact' state.
	 * @param compact
	 */
	public abstract void addAdds(StateCompact compact);
	
	/**
	 * Removes POSITIVE effects of the action from the 'compact' state.
	 * @param compact
	 */
	public abstract void removeAdds(StateCompact compact);
	
	/**
	 * Adds NEGATIVE effects of the action into the 'compact' state.
	 * @param compact
	 */
	public abstract void addDeletes(StateCompact compact);
	
	/**
	 * Removes NEGATIVE effects of the action from the 'compact' state.
	 * @param compact
	 */
	public abstract void removeDeletes(StateCompact compact);
	
	/**
	 * Enumerator of the effectors that conform to this instance; note that effector and callback must 
	 * be of/work with the very same class as the PDDLEffector you are calling this method on.
	 * @param state
	 * @param effector
	 * @param callback
	 */
	public abstract void unify(PDDLState state, PDDLEffector effector, IPDDLUnification callback);

}
