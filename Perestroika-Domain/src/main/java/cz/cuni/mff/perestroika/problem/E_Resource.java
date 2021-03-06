package cz.cuni.mff.perestroika.problem;

import cz.cuni.mff.jpddl.PDDLEnum;
import cz.cuni.mff.perestroika.domain.types.T_Resource;

public final class E_Resource extends PDDLEnum<T_Resource> {
	
	public static final E_Resource THIS = new E_Resource();
	
	private E_Resource() {
	}
	
	@Override
	public String getName() {
		return "resource";
	}
	
	@Override
	public boolean isFinalType() {
		return true;
	}
	
	public void register(int index, T_Resource e) {
		super.register(index, e);
	}
	
}
