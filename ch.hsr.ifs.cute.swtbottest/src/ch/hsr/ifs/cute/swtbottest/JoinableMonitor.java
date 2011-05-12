package ch.hsr.ifs.cute.swtbottest;

import org.eclipse.core.runtime.NullProgressMonitor;

final public class JoinableMonitor extends NullProgressMonitor {
	public boolean isDone = false;

	@Override
	public synchronized void done() {
		isDone = true;
		notifyAll();
	}

	public synchronized void join() {
		while(!isDone) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}