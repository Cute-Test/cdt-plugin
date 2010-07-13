package ch.hsr.ifs.cute.ui;


public interface IIncludeStrategyProvider {

	public abstract GetOptionsStrategy getStrategy(int optionType);

}