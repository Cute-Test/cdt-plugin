package ch.hsr.ifs.templator.plugin.viewdata;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.parser.util.StringUtil;
import org.eclipse.cdt.internal.core.dom.rewrite.astwriter.ASTWriter;
import org.eclipse.jface.text.IRegion;

import ch.hsr.ifs.templator.plugin.asttools.data.AbstractResolvedNameInfo;
import ch.hsr.ifs.templator.plugin.asttools.data.AbstractTemplateInstance;
import ch.hsr.ifs.templator.plugin.asttools.data.ResolvedName;
import ch.hsr.ifs.templator.plugin.asttools.data.SubNameErrorCollection;
import ch.hsr.ifs.templator.plugin.asttools.formatting.ASTTemplateFormatter;
import ch.hsr.ifs.templator.plugin.asttools.templatearguments.TemplateArgumentMap;
import ch.hsr.ifs.templator.plugin.logger.TemplatorException;
import ch.hsr.ifs.templator.plugin.util.ILoadingProgress;

public class ViewData {

	private AbstractResolvedNameInfo resolvedNameInfo;
	private ResolvedName resolvedName;
	private Map<Integer, IRegion> subSegments;
	private String dataText;
	private ASTWriter writer = new ASTWriter();

	public ViewData(ResolvedName resolvedName) {
		this.resolvedName = resolvedName;
		resolvedNameInfo = resolvedName.getInfo();
	}

	public void prepareForView(ILoadingProgress loadingProgress) throws TemplatorException {

		// searching for sub templates
		resolvedNameInfo.searchSubNames(loadingProgress);

		// formatting the copied AST
		ASTTemplateFormatter.format(resolvedNameInfo);

		// write the create the data text and generate clickable regions
		loadingProgress.setStatus("Rewriting Code...");
		ASTWriterRegionFinder regionFindWriter = new ASTWriterRegionFinder(resolvedNameInfo);

		// populate fields for data access later
		this.dataText = regionFindWriter.getSourceString();
		this.subSegments = regionFindWriter.getFoundRegions();
	}

	public String getTitle() {
		return writer.write(resolvedName.getOriginalName());
	}

	public String getDataText() {
		return dataText;
	}

	public List<String> getDescription() {
		List<String> descriptionStrings = new ArrayList<>();
		if (resolvedNameInfo instanceof AbstractTemplateInstance) {
			ICPPASTTemplateParameter[] templateParameters = ((AbstractTemplateInstance) resolvedNameInfo)
					.getTemplateParameters();

			for (ICPPASTTemplateParameter templateParameter : templateParameters) {
				if (!templateParameter.isParameterPack()) {
					ICPPTemplateArgument argument = resolvedNameInfo.getArgument(templateParameter);
					String argumentString = TemplateArgumentMap.getArgumentString(argument);
					descriptionStrings.add(writer.write(templateParameter) + " = " + argumentString);
				} else {
					ICPPTemplateArgument[] packExpansion = resolvedNameInfo.getTemplateArgumentMap()
							.getPackExpansion(templateParameter);

					String[] argumentStrings = new String[packExpansion.length];
					for (int i = 0; i < packExpansion.length; i++) {
						argumentStrings[i] = TemplateArgumentMap.getArgumentString(packExpansion[i]);
					}
					String allArguments = StringUtil.join(argumentStrings, ", ");
					descriptionStrings.add(writer.write(templateParameter) + " = " + allArguments);
				}
			}
		}

		return descriptionStrings;
	}

	public Map<Integer, IRegion> getSubSegments() {
		return subSegments;
	}

	public ViewData getSubNameData(int subNameIndex) {
		ResolvedName subInstance = resolvedNameInfo.getSubNames().get(subNameIndex);
		return new ViewData(subInstance);
	}

	public SubNameErrorCollection getSubNameErrors() {
		return resolvedNameInfo.getSubNameErrors();
	}

	public void navigateToName() {
		resolvedNameInfo.navigateTo();
	}

	public void navigateToSubName(int subNameIndex) {
		AbstractResolvedNameInfo subNameInfo = resolvedNameInfo.getSubNames().get(subNameIndex).getInfo();
		subNameInfo.navigateTo();
	}
}
