package ch.hsr.ifs.templator.plugin.view.components;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import ch.hsr.ifs.templator.plugin.TemplatorPlugin;
import ch.hsr.ifs.templator.plugin.logger.TemplatorException;
import ch.hsr.ifs.templator.plugin.view.interfaces.IAsyncLoadCallback;

public class AsyncEntryLoader {

	private Composite parent;
	private String title;
	private IAsyncLoadCallback callback;

	private LoadingBar loadingBar;

	public AsyncEntryLoader(Composite parent, String title, IAsyncLoadCallback callback) {
		this.parent = parent;
		this.title = title;
		this.callback = callback;

		loadingBar = new LoadingBar(parent, SWT.NONE, title);
		parent.setSize(loadingBar.getSize());
	}

	public void start() {
		final Job job = new Job(title + ": Preparing ViewEntry...") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					callback.loadOperation(loadingBar);
				} catch (TemplatorException e) {
					return new Status(IStatus.ERROR, TemplatorPlugin.PLUGIN_ID, e.getMessage(), e);
				}
				return Status.OK_STATUS;
			}
		};
		job.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(final IJobChangeEvent event) {
				if (parent.isDisposed()) {
					return;
				}
				parent.getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						if (event.getResult().isOK()) {
							loadingBar.dispose();
							callback.loadComplete();
						} else {
							callback.loadException(event.getResult().getException());
						}
					}
				});
			}
		});
		job.schedule();
	}
}
