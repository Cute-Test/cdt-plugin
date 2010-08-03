package ch.hsr.ifs.cute.ui;


/**
 * @since 4.0
 */
public interface IIncludeStrategyProvider {

	public abstract GetOptionsStrategy getStrategy(int optionType);

}