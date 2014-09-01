package ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.mappings;

public class Mapping {
	private FunctionDescription inFunction;
	private FunctionDescription outFunction;
	private boolean offsetAllowed;
	private ArgumentMapping argumentMapping;
	
	public Mapping(FunctionDescription inFunction, FunctionDescription outFunction, boolean offsetAllowed, ArgumentMapping argumentMapping) {
		this.inFunction = inFunction;
		this.outFunction = outFunction;
		this.offsetAllowed = offsetAllowed;
		this.argumentMapping = argumentMapping;
	}
	
	public FunctionDescription getInFunction() { return inFunction; }
	public FunctionDescription getOutFunction() { return outFunction; }
	public boolean isOffsetAllowed() { return offsetAllowed; }
	public ArgumentMapping getArgumentMapping() { return argumentMapping; }
}
