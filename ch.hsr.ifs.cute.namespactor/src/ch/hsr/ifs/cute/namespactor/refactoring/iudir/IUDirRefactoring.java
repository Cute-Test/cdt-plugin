/******************************************************************************
 * Copyright (c) 2012 Institute for Software, HSR Hochschule fuer Technik 
 * Rapperswil, University of applied sciences and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 *
 * Contributors:
 * 	Ueli Kunz <kunz@ideadapt.net>, Jules Weder <julesweder@gmail.com> - initial API and implementation
 ******************************************************************************/
package ch.hsr.ifs.cute.namespactor.refactoring.iudir;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDirective;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.model.CoreModelUtil;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTOperatorName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPFunction;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMName;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import ch.hsr.ifs.cute.namespactor.astutil.NSNameHelper;
import ch.hsr.ifs.cute.namespactor.astutil.NSNodeHelper;
import ch.hsr.ifs.cute.namespactor.astutil.NSSelectionHelper;
import ch.hsr.ifs.cute.namespactor.refactoring.TemplateIdFactory;
import ch.hsr.ifs.cute.namespactor.refactoring.iu.IncludeDependencyAnalyser;
import ch.hsr.ifs.cute.namespactor.refactoring.iu.InlineRefactoringBase;
import ch.hsr.ifs.cute.namespactor.refactoring.iu.InlineRefactoringContext;
import ch.hsr.ifs.cute.namespactor.refactoring.iu.NamespaceInlineContext;
import ch.hsr.ifs.cute.namespactor.refactoring.rewrite.ASTRewriteStore;
import ch.hsr.ifs.cute.namespactor.resources.Labels;

/**
 * @author kunz@ideadapt.net
 * */
@SuppressWarnings("restriction")
public class IUDirRefactoring extends InlineRefactoringBase {

	private static final int KEYWORD_OPERATOR_LENGTH = 9;
	private Map<IASTName, List<IIndexName>> namespacesPerUsing = null;
	private Map<NamespaceInlineContext, List<IASTName>> targetsPerNamespace = null;

	public IUDirRefactoring(ICElement element, ISelection selection, ICProject project) {
		super(element, selection, project);
	}

	@Override
	protected void collectModifications(ASTRewriteStore store) {
		store.addRemoveChange(ctx.selectedUsing);
		super.collectModifications(store);
	}

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		SubMonitor sm = SubMonitor.convert(pm, 10);

		super.checkInitialConditions(sm.newChild(6));

		if (initStatus.hasFatalError()) {
			sm.done();
			return initStatus;
		}

		ICPPASTUsingDirective selectedUsingDirective = NSSelectionHelper.getSelectedUsingDirective(selectedRegion, getAST(tu, pm));

		if (selectedUsingDirective == null) {
			initStatus.addFatalError(Labels.IUDIR_NoUDIRSelected);
		} else {
			ctx.selectedUsing = selectedUsingDirective;
			ctx.selectedName = selectedUsingDirective.getQualifiedName();
			ctx.templateIdsToIgnore = new HashSet<ICPPASTTemplateId>();
			ctx.enclosingCompound = NSNodeHelper.findCompoundStatementInAncestors(selectedUsingDirective);
			includeDepAnalyser = new IncludeDependencyAnalyser(getIndex());
			namespacesPerUsing = new HashMap<IASTName, List<IIndexName>>();
			targetsPerNamespace = new HashMap<NamespaceInlineContext, List<IASTName>>();

			findNamespaceDefinitionsRecursively(ctx.selectedName);
			findTargetsInScope(ctx.enclosingCompound);
			processTargets();
		}

		sm.done();
		return initStatus;
	}

	private void processTargets() throws CoreException {

		for (Entry<NamespaceInlineContext, List<IASTName>> entry : targetsPerNamespace.entrySet()) {

			for (IASTName name : entry.getValue()) {

				ctx.enclosingNSContext = new NamespaceInlineContext();
				ctx.enclosingNSContext.usingName = entry.getKey().usingName;
				ctx.enclosingNSContext.namespaceDefName = entry.getKey().namespaceDefName;
				ctx.enclosingNSContext.namespaceDefBinding = ((PDOMName) ctx.enclosingNSContext.namespaceDefName).getBinding();
				processReplaceOf(name);
			}
		}
	}

	private void findTargetsInScope(IASTNode enclosingCompound) throws OperationCanceledException, CoreException {

		final IIndex indexer = this.getIndex();

		ASTVisitor v = new ASTVisitor() {
			{
				shouldVisitNames = true;
				shouldVisitNamespaces = true;
			}

			@Override
			public int visit(ICPPASTNamespaceDefinition namespaceDefinition) {

				boolean isAnoNamespace = namespaceDefinition.getName().toString().length() == 0;
				if (isAnoNamespace) {
					return super.visit(namespaceDefinition);
				}

				for (Entry<IASTName, List<IIndexName>> usingNamespaces : namespacesPerUsing.entrySet()) {
					for (IIndexName indexName : usingNamespaces.getValue()) {

						if (isASTNameSameAsIndexName(namespaceDefinition.getName(), indexName)) {
							return PROCESS_SKIP;
						}
					}
				}

				return super.visit(namespaceDefinition);
			}

			@Override
			public int visit(IASTName name) {

				if (!isCandidate(name)) {
					return super.visit(name);
				}

				IBinding candidateBinding = name.resolveBinding();
				IBinding candidateBindingOwner = candidateBinding.getOwner();
				while (candidateBindingOwner instanceof ICPPNamespace && ((ICPPNamespace) candidateBindingOwner).isInline()) {
					candidateBindingOwner = candidateBindingOwner.getOwner();
				}
				try {
					for (Entry<IASTName, List<IIndexName>> usingNamespaces : namespacesPerUsing.entrySet()) {
						for (IIndexName nsDefName : usingNamespaces.getValue()) {

							IIndexBinding pdomCandidateOwnerBinding = indexer.adaptBinding(candidateBindingOwner);
							IIndexBinding pdomNsDefBinding = indexer.adaptBinding(((PDOMName) nsDefName).getBinding());
							boolean currentNameIsATarget = pdomCandidateOwnerBinding.equals(pdomNsDefBinding) && isInlineRequiredFor(name, nsDefName);

							if (currentNameIsATarget) {
								addNameToTargets(name, nsDefName, usingNamespaces.getKey());
								break;
							}
						}
					}
				} catch (CoreException e) {
					e.printStackTrace();
				}

				return super.visit(name);
			}

			private boolean isCandidate(IASTName name) {
				// only visit real IASTNames
				if (name instanceof ICPPASTQualifiedName) {
					return false;
				}

				boolean isAnonymousName = name.toString().length() == 0;
				if (isAnonymousName) {
					return false;
				}

				IBinding candidateBinding = name.resolveBinding();
				IBinding candidateBindingOwner = candidateBinding.getOwner();
				// no owner => no qualification required
				if (candidateBindingOwner == null) {
					return false;
				}

				if (candidateBindingOwner instanceof ICPPNamespace) {
					boolean isAnoNamespace = ((ICPPNamespace) candidateBindingOwner).getName().toString().isEmpty();
					if (isAnoNamespace) {
						return false;
					}
				}
				return true;
			}

			private void addNameToTargets(IASTName name, IIndexName nsDefName, IASTName usingName) {
				List<IASTName> names = null;
				for (NamespaceInlineContext ctx : targetsPerNamespace.keySet()) {
					if (ctx.namespaceDefName.equals(nsDefName)) {
						names = targetsPerNamespace.get(ctx);
					}
				}

				if (names == null) {
					names = new ArrayList<IASTName>();
					NamespaceInlineContext ictx = new NamespaceInlineContext();
					ictx.namespaceDefName = nsDefName;
					ictx.usingName = usingName;
					targetsPerNamespace.put(ictx, names);
				}
				names.add(name);
			}

			private boolean isASTNameSameAsIndexName(IASTName astNname, IIndexName indexName) {
				IASTFileLocation astFileLocation = astNname.getFileLocation();
				IASTFileLocation indexFileLocation = indexName.getFileLocation();

				int astOffset = astFileLocation.getNodeOffset();
				int indexOffset = indexFileLocation.getNodeOffset();
				int astLength = astFileLocation.getNodeOffset();
				int indexLength = indexFileLocation.getNodeOffset();
				String fAstName = astFileLocation.getFileName();
				String fIndexName = indexFileLocation.getFileName();

				return astOffset == indexOffset && astLength == indexLength && fAstName.equals(fIndexName);
			}
		};

		boolean isTUScope = enclosingCompound == null;
		if (isTUScope) {
			visitIncludeDependentTUs(v);
			enclosingCompound = getAST(tu, npm);
		}
		enclosingCompound.accept(v);
	}

	private void visitIncludeDependentTUs(ASTVisitor v) throws CoreException {
		IASTNode enclosingCompound;
		List<IPath> paths = includeDepAnalyser.getIncludeDependentPathsOf(tu);
		for (IPath filePath : paths) {
			IFile[] files = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(new File(filePath.toOSString()).toURI());
			if (files.length < 1)
				continue;
			ITranslationUnit tu = CoreModelUtil.findTranslationUnit(files[0]);
			enclosingCompound = getAST(tu, npm);
			enclosingCompound.accept(v);
		}
	}

	private void findNamespaceDefinitionsRecursively(IASTName selectedName) throws OperationCanceledException, CoreException {

		IBinding nsBinding = selectedName.resolveBinding();
		IIndexName[] nsDefBindings = getIndex().findDefinitions(nsBinding);
		List<IIndexName> nsDefs = namespacesPerUsing.get(selectedName);
		if (nsDefs == null) {
			nsDefs = new ArrayList<IIndexName>();
			namespacesPerUsing.put(selectedName, nsDefs);
		}

		for (IIndexName nsDefName : nsDefBindings) {
			nsDefs.add(nsDefName);
		}
	}

	private static IASTNode getNodeOnSameLineAs(IASTFileLocation loc, IASTTranslationUnit ast) {
		int nodeOffset = loc.getNodeOffset();
		int tuLength = ast.getFileLocation().getNodeLength();

		return ast.getNodeSelector(null).findFirstContainedNode(nodeOffset, tuLength);
	}

	private boolean isInlineRequiredFor(IASTName name, IName enclosingNSName) {

		// TODO operator overloads are not currently supported, #270
		// because the visitor only visits names, a BinaryExpression (e.g. in cout << "\n") for example is never found
		if (isImplicitOperator(name.resolveBinding(), name)) {
			initStatus.addWarning(String.format(Labels.IUDEC_ImplicitOperatorCall, name.getFileLocation().getFileName(),
					getNodeOnSameLineAs(name.getFileLocation(), getASTOf(name, npm)).getFileLocation().getStartingLineNumber()));
			return false;
		}

		if (ctx.enclosingCompound != null) {
			if (!NSNodeHelper.isNodeEnclosedBy(ctx.enclosingCompound, name)) {
				return false;
			}
		} else {
			// on TU level, should check if node is after node of using directive
			if (!isAfterUsingDirective(name)) {
				return false;
			}
		}

		// only replace node if its not part of a qualified name with the previous qualifier being the namespace to be inlined
		// e.g. inlining B::C on C::c() does not change anything
		if (NSNameHelper.isNodeQualifiedWithName(name, enclosingNSName)) {
			return false;
		}

		return true;
	}

	private boolean isAfterUsingDirective(IASTName name) {
		IASTDeclaration[] topLevelDecls = ctx.selectedUsing.getTranslationUnit().getDeclarations();
		IASTNode usingdirparent = findTopLevelDeclaration(ctx.selectedUsing);
		IASTNode nameParent = findTopLevelDeclaration(name);
		for (int i = 0; i < topLevelDecls.length; ++i) {
			if (usingdirparent.equals(topLevelDecls[i])) {
				return true;
			}
			if (nameParent.equals(topLevelDecls[i])) {
				return false;
			}
		}
		return true; // be conservative....
	}

	public IASTNode findTopLevelDeclaration(IASTNode usingdirparent) {
		while (usingdirparent != null && usingdirparent.getParent() != null && !(usingdirparent.getParent() instanceof IASTTranslationUnit)) {
			usingdirparent = usingdirparent.getParent();
		}
		return usingdirparent;
	}

	private void processReplaceOf(IASTName childRefNode) {
		// template ids are part of more complex qualified names, see #225
		if (isPartOfTemplateVariableDeclaration(childRefNode)) { // check for inline namespace elements

			processTemplateVariableDeclaration(childRefNode, ctx);

		} else if (isPartOfTemplateMethodDefinition(childRefNode)) {

			processTemplateMethodDefinition(childRefNode);

		} else {
			processDefaultReplace(childRefNode);
		}
	}

	private void processDefaultReplace(IASTName childRefNode) {
		IASTName nodeToReplace = childRefNode;
		if (childRefNode.getParent() instanceof ICPPASTQualifiedName) {
			nodeToReplace = (IASTName) childRefNode.getParent();
		}
		addReplacement(nodeToReplace, NSNameHelper.prefixNameWith(ctx.enclosingNSContext.usingName, childRefNode));
	}

	private void processTemplateMethodDefinition(IASTName childRefNode) {
		// TODO inlining method definitions does not work, #271
		ICPPASTQualifiedName qName = (ICPPASTQualifiedName) childRefNode.getParent();
		ICPPASTQualifiedName qInlinedNameNode = NSNameHelper.prefixNameWith(ctx.enclosingNSContext.usingName, childRefNode);
		ICPPASTQualifiedName templNameNode = NSNameHelper.copyQualifers(qInlinedNameNode);
		// copy over the original names (i.e. the templateId and its following siblings)
		for (IASTName n : qName.getNames()) {
			if (n instanceof ICPPASTTemplateId) {
				n = new IUDirTemplateIdFactory((ICPPASTTemplateId) n, ctx).buildTemplate();// ((ICPPASTTemplateId) n).getTemplateName();
				templNameNode.addName(n);
			} else {
				templNameNode.addName(n.copy());
			}
		}
		addReplacement(qName, templNameNode);
	}

	private boolean isImplicitOperator(IBinding binding, IName bindingRef) {
		boolean isOperatorBinding = binding instanceof CPPFunction && ((CPPFunction) binding).getDefinition() != null
				&& ((CPPFunction) binding).getDefinition().getName() instanceof CPPASTOperatorName;
		return isOperatorBinding && bindingRef.getFileLocation().getNodeLength() < KEYWORD_OPERATOR_LENGTH;
	}

	/**
	 * @return true for names inside template method definitions (e.g. template<class T> T SC<T>::get(){})
	 * */
	private boolean isPartOfTemplateMethodDefinition(IASTName childRefNode) {
		return childRefNode instanceof ICPPASTTemplateId && childRefNode.getParent() instanceof ICPPASTQualifiedName;
	}

	@Override
	protected TemplateIdFactory getTemplateIdFactory(ICPPASTTemplateId templateId, InlineRefactoringContext ctx) {
		return new IUDirTemplateIdFactory(templateId, ctx);
	}
}