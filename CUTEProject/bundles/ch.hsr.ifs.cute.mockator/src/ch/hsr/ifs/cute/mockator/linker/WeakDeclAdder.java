package ch.hsr.ifs.cute.mockator.linker;

import java.net.URI;

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.gnu.IGCCASTAttributeList;
import org.eclipse.core.resources.IFile;

import ch.hsr.ifs.iltis.core.core.resources.FileUtil;
import ch.hsr.ifs.iltis.core.core.resources.WorkspaceUtil;
import ch.hsr.ifs.iltis.cpp.core.ast.nodefactory.ASTNodeFactoryFactory;
import ch.hsr.ifs.iltis.cpp.core.ast.nodefactory.IBetterFactory;
import ch.hsr.ifs.iltis.cpp.core.wrappers.ModificationCollector;

public class WeakDeclAdder {

	private static final IBetterFactory nodeFactory = ASTNodeFactoryFactory.getDefaultCPPNodeFactory();
	private final ModificationCollector collector;

	public WeakDeclAdder(final ModificationCollector collector) {
		this.collector = collector;
	}

	public void addWeakDeclAttribute(final ICPPASTFunctionDeclarator funDecl) {
		final IASTTranslationUnit tu = funDecl.getTranslationUnit();
		
		if (!isTuPartOfWorkspace(tu)) {
			return;
		}
		
		IASTDeclaration parent = (IASTDeclaration) funDecl.getParent();
		IASTDeclaration replacementParent = parent.copy();
		IASTDeclSpecifier declSpec;
		
		if(replacementParent instanceof IASTSimpleDeclaration) {
			declSpec = ((IASTSimpleDeclaration) replacementParent).getDeclSpecifier();
		} else {
			declSpec = ((ICPPASTFunctionDefinition) replacementParent).getDeclSpecifier();
		}
		
		declSpec.setStorageClass(IASTDeclSpecifier.sc_unspecified);
		
		IGCCASTAttributeList attributeList = nodeFactory.newGCCAttributeList();
		attributeList.addAttribute(nodeFactory.newAttribute("weak".toCharArray(), null));
		declSpec.addAttributeSpecifier(attributeList);
		
		collector.rewriterForTranslationUnit(tu).replace(parent, replacementParent, null);
	}

	private static boolean isTuPartOfWorkspace(final IASTTranslationUnit tuOfFunDef) {
		tuOfFunDef.getOriginatingTranslationUnit().getFile().getLocationURI();
		final URI uriOfTu = FileUtil.stringToUri(tuOfFunDef.getFilePath());
		final IFile[] files = WorkspaceUtil.getWorkspaceRoot().findFilesForLocationURI(uriOfTu);
		return files.length > 0;
	}
	
}
