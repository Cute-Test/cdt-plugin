package ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.mappings;

import java.util.HashSet;
import java.util.Set;

import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.Context.ContextState;

public class Mapping {
	private Function inFunction;
	private Function outFunction;
	private ArgumentMapping argumentMapping;
	private Set<ContextState> contextStates;
	
	public Mapping(Function inFunction, Function outFunction, ArgumentMapping argumentMapping, ContextState... contextStates) {
		this.inFunction = inFunction;
		this.outFunction = outFunction;
		this.argumentMapping = argumentMapping;
		this.contextStates = new HashSet<ContextState>();
		for(ContextState contextState : contextStates) {
			this.contextStates.add(contextState);
		}
	}
	
	public Function getInFunction() { return inFunction; }
	public Function getOutFunction() { return outFunction; }
	public ArgumentMapping getArgumentMapping() { return argumentMapping; }
	public boolean isApplicableForContextState(ContextState contextState) {
		return contextStates.contains(contextState);
	}
	
	public boolean canHandleOffsets() {
		return contextStates.contains(ContextState.CStringModified) || contextStates.contains(ContextState.CStringAlias);
	}
}
