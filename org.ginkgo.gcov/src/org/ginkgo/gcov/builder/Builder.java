package org.ginkgo.gcov.builder;

import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;
import org.ginkgo.gcov.parser.IParser;
import org.ginkgo.gcov.parser.LineCoverageParser;
import org.xml.sax.SAXException;

public abstract class Builder extends IncrementalProjectBuilder {
	class SampleResourceVisitor implements IResourceVisitor {

		public boolean visit(IResource resource) {
			checkXML(resource);
			//return true to continue visiting children.
			return true;
		}
	}
	class SampleResourceCleanVisitor implements IResourceVisitor {
		public boolean visit(IResource resource) {
			
			System.out.println(resource + " " + resource.getName());

			IProject project = resource.getProject();
			try
			{
				//String n = resource.getName();
				String n = resource.getFullPath().toOSString();
				project.setPersistentProperty(new QualifiedName(n,"persent"), null);
				project.setPersistentProperty(new QualifiedName(n,"totalLine"), null);
			}
			catch(CoreException e)
			{

			}

			if (resource instanceof IFile ) {

				String name = resource.getName();
				if(name.endsWith(".c")||name.endsWith(".cpp")){
					//				IFile file = (IFile) resource;
					deleteMarkers((IFile) resource);


				}else if(name.endsWith(".gcda")||name.endsWith(".gcno")||name.endsWith(".gcov")){
					try {
						resource.delete(IResource.FORCE, null);
						//						resource.delete(true, null);
					} catch (CoreException e) {
						// TODO 自動生成された catch ブロック
						e.printStackTrace();
					}
				}
			}
			return true;
		}
	}
	class SampleDeltaVisitor implements IResourceDeltaVisitor {
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse.core.resources.IResourceDelta)
		 */
		public boolean visit(IResourceDelta delta) throws CoreException {
			IResource resource = delta.getResource();
			switch (delta.getKind()) {
			case IResourceDelta.ADDED:
				// handle added resource
				checkXML(resource);
				break;
			case IResourceDelta.REMOVED:
				// handle removed resource
				break;
			case IResourceDelta.CHANGED:
				// handle changed resource
				cleanDerivedResource(resource);
				checkXML(resource);	
				break;
			}
			//return true to continue visiting children.
			return true;
		}


	}
	public Builder() {
		super();
	}
	private void cleanDerivedResource(IResource resource) {
		if (resource instanceof IFile) {
			IFile file = (IFile) resource;
			String name = file.getName();
			if(name.endsWith(".c")||name.endsWith(".cpp")){
//				deleteMarkers(file);
				String body = name.replaceFirst("\\.c$", "");
				IProject project = file.getProject();
//				try {
////					String a = body + ".gcno";
////					IFile b = file.getProject().getFile(body + ".gcno");
//					project.getFile(body + ".gcno").delete(true, null);
//				} catch (CoreException e) {
//					// TODO 自動生成された catch ブロック
//					e.printStackTrace();
//				}
//				try {
////					file.getProject().findMember(body + ".gcda").delete(true, null);
//					project.getFile(body + ".gcda").delete(true, null);
//				} catch (CoreException e) {
//					// TODO 自動生成された catch ブロック
//					e.printStackTrace();
//				}
//				try {
//					project.getFile(name + ".gcov").delete(true, null);
//				} catch (CoreException e) {
//					// TODO 自動生成された catch ブロック
//					e.printStackTrace();
//				}
//				System.out.println(a);
//				file.getName().
			}
		}
		
	}
	private void deleteMarkers(IFile file) {
		try {
			file.deleteMarkers(LineCoverageParser.COVER_MARKER_TYPE, true, IResource.DEPTH_ZERO);
			file.deleteMarkers(LineCoverageParser.UNCOVER_MARKER_TYPE, true, IResource.DEPTH_ZERO);
		} catch (CoreException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.internal.events.InternalBuilder#build(int,
	 *      java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
			throws CoreException {
			cleanBuild();
				if (kind == FULL_BUILD) {
					fullBuild(monitor);
				} else {
					IResourceDelta delta = getDelta(getProject());
					if (delta == null) {
						fullBuild(monitor);
					} else {
						incrementalBuild(delta, monitor);
					}
				}
				return null;
			}
	protected void fullBuild(final IProgressMonitor monitor) throws CoreException {
		try {
			getProject().accept(new SampleResourceVisitor());
		} catch (CoreException e) {
		}
	}

	abstract IParser getParser() throws ParserConfigurationException,
	SAXException;
	abstract void checkXML(IResource resource);
	
	protected void incrementalBuild(IResourceDelta delta, IProgressMonitor monitor)
			throws CoreException {
				// the visitor does the work.
				delta.accept(new SampleDeltaVisitor());
			}

	@Override
	protected void clean(IProgressMonitor monitor) throws CoreException {
		super.clean(monitor);
		getProject().accept(new SampleResourceCleanVisitor());
	}

	abstract public void cleanBuild();
}