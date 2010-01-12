package org.ginkgo.gcov.builder;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.ginkgo.gcov.parser.IParser;
import org.ginkgo.gcov.parser.LineCoverageParser;
import org.xml.sax.SAXException;

public class LineCoverageBuilder extends Builder {
	private IParser gcovParser;
	public static final String BUILDER_ID = "org.ginkgo.gcov.markerBuilder";

	public void checkXML(IResource resource) {
		if (resource instanceof IFile && (resource.getName().endsWith(".cpp")|| resource.getName().endsWith(".h"))) {
			IFile file = (IFile) resource;
			LineCoverageParser.deleteMarkers(file);
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
		gcovParser = new LineCoverageParser(); 
	}
	return gcovParser;
	}
	
	public void cleanBuild(){
		
	}
}
