package ch.hsr.ifs.cutelauncher.model;

import java.util.Vector;

public interface ITestComposite {

	public abstract int getError();

	public abstract int getFailure();

	public abstract int getSuccess();

	public abstract int getTotalTests();

	public abstract int getRun();
	
	public abstract Vector<? extends TestElement> getElements();
	
	public void addTestElement(TestElement element);
	
	public boolean hasErrorOrFailure();
	
	public void addListener(ITestCompositeListener listener);
	
	public void removeListener(ITestCompositeListener listener);

}