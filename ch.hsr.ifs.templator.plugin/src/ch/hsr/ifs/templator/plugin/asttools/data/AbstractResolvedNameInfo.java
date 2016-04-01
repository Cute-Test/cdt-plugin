package ch.hsr.ifs.templator.plugin.asttools.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.internal.core.dom.rewrite.astwriter.ASTWriter;

import ch.hsr.ifs.templator.plugin.asttools.ASTAnalyzer;
import ch.hsr.ifs.templator.plugin.asttools.ASTTools;
import ch.hsr.ifs.templator.plugin.asttools.resolving.FindAllNamesVisitor;
import ch.hsr.ifs.templator.plugin.asttools.resolving.PostResolver;
import ch.hsr.ifs.templator.plugin.asttools.resolving.nametype.TypeNameToType;
import ch.hsr.ifs.templator.plugin.asttools.resolving.NameDeduction;
import ch.hsr.ifs.templator.plugin.asttools.templatearguments.TemplateArgumentMap;
import ch.hsr.ifs.templator.plugin.asttools.type.finding.RelevantNameType;
import ch.hsr.ifs.templator.plugin.logger.TemplatorException;
import ch.hsr.ifs.templator.plugin.util.ILoadingProgress;

public abstract class AbstractResolvedNameInfo {

	protected ASTAnalyzer analyzer;

	protected IASTName resolvingName;
	protected IBinding binding;

	protected AbstractResolvedNameInfo parent;
	protected List<ResolvedName> subNames;
	protected Map<RelevantNameType, AbstractResolvedNameInfo> subNameCache;
	protected boolean subNamesSearched = false;
	protected NameTypeKind type;

	protected IASTDeclaration definition;
	protected IASTDeclaration formattedDefinition;
	protected RelevantNameCache relevantNameCache;

	private SubNameErrorCollection subNameErrors;

	protected AbstractResolvedNameInfo(IASTName resolvingName, IBinding binding, NameTypeKind type,
			IASTDeclaration definition, AbstractResolvedNameInfo parent, ASTAnalyzer analyzer) {
		this.resolvingName = resolvingName;
		this.binding = binding;
		this.type = type;
		this.definition = definition;
		this.parent = parent;
		this.analyzer = analyzer;
		subNames = new ArrayList<>();
		subNameCache = new HashMap<>();
		relevantNameCache = new RelevantNameCache();
	}

	protected AbstractResolvedNameInfo(UnresolvedNameInfo unresolvedName, IASTDeclaration definition,
			AbstractResolvedNameInfo parent, ASTAnalyzer analyzer) {
		this(unresolvedName.getResolvingName(), unresolvedName.getBinding(), unresolvedName.getType(), definition,
				parent, analyzer);
	}

	public static AbstractResolvedNameInfo create(IASTName originalName, boolean acceptUnknownBindings,
			ASTAnalyzer analyzer) throws TemplatorException {
		// get the template-id for the original name if necessary, else deduceStatement will return null
		IASTName resolvingOriginalName = ASTTools.extractTemplateInstanceName(originalName);
		UnresolvedNameInfo unresolvedName = NameDeduction.deduceName(resolvingOriginalName, acceptUnknownBindings,
				analyzer, RelevantNameCache.EMPTY_CACHE);
		return create(unresolvedName, null, analyzer);
	}

	public static AbstractResolvedNameInfo create(UnresolvedNameInfo unresolvedName, AbstractResolvedNameInfo parent,
			ASTAnalyzer analyzer) throws TemplatorException {
		return create(unresolvedName, parent, analyzer, true);
	}

	public static AbstractResolvedNameInfo create(UnresolvedNameInfo unresolvedName, AbstractResolvedNameInfo parent,
			ASTAnalyzer analyzer, boolean resolve) throws TemplatorException {
		if (unresolvedName != null) {
			if (resolve) {
				PostResolver.resolveToFinalBinding(unresolvedName, parent, analyzer);
			}
			if (unresolvedName.getType() == NameTypeKind.FUNCTION) {
				return FunctionCall.__create(unresolvedName, parent, analyzer);
			} else {
				return AbstractTemplateInstance.__create(unresolvedName, parent, analyzer);
			}
		}

		return null;
	}

	public void searchSubNames(ILoadingProgress loadingProgress) throws TemplatorException {
		if (subNamesSearched) {
			return;
		}

		subNameErrors = new SubNameErrorCollection();

		List<IASTName> allNames = findAllNames(loadingProgress);

		subNames = _createSubNames(loadingProgress, allNames, subNameErrors);

		subNamesSearched = true;
	}

	private List<IASTName> findAllNames(ILoadingProgress loadingProgress) {
		loadingProgress.setStatus("Listing all Names...");
		FindAllNamesVisitor searchVisitor = new FindAllNamesVisitor();
		definition.accept(searchVisitor);
		loadingProgress.setProgress(0.1);
		return searchVisitor.getAllNames();
	}

	private List<ResolvedName> _createSubNames(ILoadingProgress loadingProgress, List<IASTName> allNames,
			SubNameErrorCollection errors) {

		loadingProgress.setStatus("Deducing Template Relevant Names...");
		double loadingBarIncrement = 0.99 / (allNames.size() + 1);
		double currentLoadingProgress = 0.1;

		List<ResolvedName> resultSubNames = new ArrayList<>();
		for (IASTName name : allNames) {

			UnresolvedNameInfo unresolvedName = deduceSubName(name, errors);
			if (unresolvedName != null) {
				ResolvedName subName = createSubName(unresolvedName, errors);
				if (subName != null && subName.getInfo() != null) {
					resultSubNames.add(subName);
				}
			}
			currentLoadingProgress += loadingBarIncrement;
			loadingProgress.setProgress(currentLoadingProgress);
		}
		return resultSubNames;
	}

	private UnresolvedNameInfo deduceSubName(IASTName name, SubNameErrorCollection errors) {
		try {
			UnresolvedNameInfo unresolvedSubName = NameDeduction.deduceName(name, true, analyzer, relevantNameCache);
			return unresolvedSubName;
		} catch (TemplatorException e) {
			errors.addDeductionError(name, e);
		}
		return null;
	}

	private ResolvedName createSubName(UnresolvedNameInfo unresolvedName, SubNameErrorCollection errors) {
		try {
			// first check cache for same occurrence of variables etc. that have already been resolved
			AbstractResolvedNameInfo resolvedSubName = subNameCache.get(unresolvedName.getNameType());
			if (resolvedSubName == null) {
				resolvedSubName = create(unresolvedName, this, analyzer);
				if (resolvedSubName != null) {
					doPostResolving(resolvedSubName);
					subNameCache.put(unresolvedName.getNameType(), resolvedSubName);
				}
			}
			return new ResolvedName(unresolvedName.getOriginalName(), resolvedSubName);
		} catch (TemplatorException e) {
			errors.addResolvingError(unresolvedName.getOriginalName(), e);
		}
		return null;
	}

	// TODO msyfrig 08.09.2015 - maybe call substatementInfo.doPostResolving if possible
	protected abstract void doPostResolving(AbstractResolvedNameInfo subNameInfo) throws TemplatorException;

	public abstract void navigateTo();

	public abstract TemplateArgumentMap getTemplateArgumentMap();

	public ICPPTemplateArgument getArgument(ICPPTemplateParameter param) {
		ICPPTemplateArgument argument = getTemplateArgumentMap().getArgument(param);
		AbstractResolvedNameInfo parentNameInfo = getParent();
		while (argument == null && parentNameInfo != null) {
			argument = parentNameInfo.getTemplateArgumentMap().getArgument(param);
			parentNameInfo = parentNameInfo.getParent();
		}
		return argument;
	}

	public ICPPTemplateArgument getArgument(ICPPASTTemplateParameter astParam) {
		ICPPTemplateArgument argument = getTemplateArgumentMap().getArgument(astParam);
		AbstractResolvedNameInfo parentNameInfo = getParent();
		while (argument == null && parentNameInfo != null) {
			argument = parentNameInfo.getTemplateArgumentMap().getArgument(astParam);
			parentNameInfo = parentNameInfo.getParent();
		}
		return argument;
	}

	public IASTName getResolvingName() {
		return resolvingName;
	}

	public NameTypeKind getType() {
		return type;
	}

	public AbstractResolvedNameInfo getParent() {
		return parent;
	}

	public IBinding getBinding() {
		return binding;
	}

	public List<ResolvedName> getSubNames() {
		return subNames;
	}

	public IASTDeclaration getFormattedDefinition() {
		return formattedDefinition;
	}

	public void setFormattedDefinition(IASTDeclaration formattedDefinition) {
		this.formattedDefinition = formattedDefinition;
	}

	public IASTDeclaration getDefinition() {
		return definition;
	}

	public ASTAnalyzer getAnalyzer() {
		return analyzer;
	}

	public SubNameErrorCollection getSubNameErrors() {
		return subNameErrors;
	}

	/** For debug purposes only. */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("resolvingName: " + Objects.toString(resolvingName) + " ");
		sb.append(" (");
		sb.append("binding: " + Objects.toString(binding) + ", ");
		sb.append("type: " + Objects.toString(binding) + ", ");

		if (binding instanceof ICPPSpecialization) {
			sb.append("argument map: ");
			sb.append(((ICPPSpecialization) binding).getTemplateParameterMap());
		}
		sb.append(")");

		sb.append("\n\n");
		sb.append("grandparent: " + new ASTWriter().write(resolvingName.getParent().getParent()));

		return sb.toString();
	}

	protected static AbstractResolvedNameInfo createParent(UnresolvedNameInfo unresolvedName,
			AbstractResolvedNameInfo parent, ASTAnalyzer analyzer) throws TemplatorException {
		NameTypeKind type = unresolvedName.getType();
		IASTName resolvingName = unresolvedName.getResolvingName();

		ICPPASTTemplateDeclaration classDeclaration = null;
		if (type == NameTypeKind.METHOD_TEMPLATE) {
			classDeclaration = ASTTools.findFirstAncestorByType(resolvingName, ICPPASTTemplateDeclaration.class);
			classDeclaration = ASTTools.findFirstAncestorByType(classDeclaration, ICPPASTTemplateDeclaration.class);
		} else if (type == NameTypeKind.METHOD || type == NameTypeKind.DEFERRED_METHOD) {
			classDeclaration = ASTTools.findFirstAncestorByType(resolvingName, ICPPASTTemplateDeclaration.class);
		}

		AbstractResolvedNameInfo classTemplateInfo = null;
		if (parent != null && parent.getDefinition() == classDeclaration) {
			classTemplateInfo = parent;
		} else {
			IASTName definition = analyzer.getTypeDeducer().getDefinitionForName(resolvingName);
			TypeNameToType parentClassTemplate = analyzer
					.getType(analyzer.extractResolvingName(definition, true).getTypeName(), parent);
			classTemplateInfo = parentClassTemplate.getCurrentContext();
		}

		return classTemplateInfo;
	}
}
