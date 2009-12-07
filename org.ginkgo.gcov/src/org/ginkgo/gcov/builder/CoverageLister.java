package org.ginkgo.gcov.builder;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.ginkgo.gcov.model.CoverageData;

public class CoverageLister implements ICoverageListener {
	IResource sourceFile = null;
	
	private class CoverageListerResourceVisitor implements IResourceVisitor{
		private String sourceFileName;

		private CoverageListerResourceVisitor(String FileName){			
			sourceFileName = FileName;
		}
		public boolean visit(IResource resource) throws CoreException {
			if(resource instanceof IFile)
			{
				if(resource.getName().equals(sourceFileName)){
					sourceFile = resource;
					return false;
				}
			}
			return true;
		}
	}
	
	
	/* (�� Javadoc)
	 * coverageSummaryListener
	 * @see org.ginkgo.gcov.builder.ICoverageListener#addData(org.eclipse.core.resources.IProject, org.ginkgo.gcov.builder.CoverageData)
	 */
	public void addCoverageData(IProject project, CoverageData cov)
			throws CoreException {
		if(cov.getElementType().equals("File")){
			
			sourceFile = null;
			
			try
			{
				project.accept(new CoverageListerResourceVisitor(cov.getElementName()));
			}
			catch (CoreException ce)
			{
			}
			
			
			if(sourceFile != null)
			{
				setCoverage(project, sourceFile.getFullPath().toOSString(), cov.getPersent(), cov.getTotalLine());
			}
		}else if(cov.getElementType().equals("Function")){
			project.setPersistentProperty(new QualifiedName(cov.getElementName(),"persent"), cov.getPersent());//(elementName + "persent", persent);
			project.setPersistentProperty(new QualifiedName(cov.getElementName(),"totalLine"), cov.getTotalLine());
		}
	}
	
	private void setCoverage(IProject project, String sPath, String sPersent, String sTotalLine) throws CoreException
	{
		

		float fileCoverage = 0;
		int fileTotalLine = 0;

		int dirTotalLine = 0;
		float dirCoverage = 0;
	
		
		
		project.setPersistentProperty(new QualifiedName(sPath,"persent"), sPersent);
		project.setPersistentProperty(new QualifiedName(sPath,"totalLine"), sTotalLine);
	
		try{
			fileTotalLine = Integer.parseInt(project.getPersistentProperty(new QualifiedName(sPath,"totalLine")));
			fileCoverage = Float.parseFloat(project.getPersistentProperty(new QualifiedName(sPath,"persent")));
		}
		catch (NumberFormatException e){
		}
		String separator = System.getProperty("file.separator");
		
		sPath = sPath.substring(0, sPath.lastIndexOf(separator) ); // under Linux it would be '/' this needs to be operating system independent;
		
		/* Update the percent for sub directories. */
		while (!sPath.equals(""))
		{
			try{
				dirTotalLine = Integer.parseInt(project.getPersistentProperty(new QualifiedName(sPath,"totalLine")));
				dirCoverage = Float.parseFloat(project.getPersistentProperty(new QualifiedName(sPath,"persent")));
				
				dirCoverage = ((dirTotalLine * dirCoverage) + (fileTotalLine * fileCoverage)) / (dirTotalLine + fileTotalLine);
				

				project.setPersistentProperty(new QualifiedName(sPath,"persent"), Float.toString(dirCoverage));
				project.setPersistentProperty(new QualifiedName(sPath,"totalLine"), Integer.toString(dirTotalLine + fileTotalLine));
			}
			catch (NumberFormatException e){
				project.setPersistentProperty(new QualifiedName(sPath,"persent"), sPersent);
				project.setPersistentProperty(new QualifiedName(sPath,"totalLine"), sTotalLine);
			}
			

			
			sPath = sPath.substring(0, sPath.lastIndexOf(separator)); // under Linux it would be '/'; solved
		}
		
		
//		totalLine = Integer.parseInt(p.getPersistentProperty(new QualifiedName(n,"totalLine")));
//		coverage = Float.parseFloat(p.getPersistentProperty(new QualifiedName(n,"persent")));
//		coverLine = (int) (totalLine * coverage /100);
	}
}
