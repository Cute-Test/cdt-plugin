package org.ginkgo.gcov.builder;


import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.ginkgo.gcov.parser.CoverageSummaryParser;
import org.ginkgo.gcov.parser.IParser;
import org.xml.sax.SAXException;

public class CoverageSummaryBuilder extends Builder{
	private IParser gcovParser;
	public static final String BUILDER_ID = "org.ginkgo.gcov.sampleBuilder";
	
	public void checkXML(IResource resource) {
		if (resource instanceof IFile && resource.getName().endsWith(".gcda")) {
			IFile file = (IFile) resource;
//			XMLErrorHandler reporter = new XMLErrorHandler(file);
			try {
				getParser().parse(file);
			} catch (Exception e1) {
			}
		}
	}
	IParser getParser() throws ParserConfigurationException,
	SAXException {
	if (gcovParser == null){
		gcovParser = new CoverageSummaryParser(); 
	}
	return gcovParser;
	}
	
	

	public void cleanBuild(){
		CoverageSummaryParser.deleteSummary(this.getProject());
	}
}
