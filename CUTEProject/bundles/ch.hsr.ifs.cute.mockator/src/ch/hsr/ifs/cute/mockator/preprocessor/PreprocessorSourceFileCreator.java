package ch.hsr.ifs.cute.mockator.preprocessor;

import static ch.hsr.ifs.iltis.core.core.collections.CollectionUtil.orderPreservingSet;

import java.util.Set;

import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

import ch.hsr.ifs.iltis.core.core.resources.FileUtil;

import ch.hsr.ifs.iltis.cpp.core.ast.ASTUtil;
import ch.hsr.ifs.iltis.cpp.core.ast.utilities.ASTStatementUtil;
import ch.hsr.ifs.iltis.cpp.core.wrappers.CRefactoringContext;
import ch.hsr.ifs.iltis.cpp.core.wrappers.ModificationCollector;

import ch.hsr.ifs.cute.mockator.MockatorConstants;
import ch.hsr.ifs.cute.mockator.refsupport.functions.FunctionDelegateCallCreator;
import ch.hsr.ifs.cute.mockator.refsupport.includes.AstIncludeNode;
import ch.hsr.ifs.cute.mockator.refsupport.utils.QualifiedNameCreator;


class PreprocessorSourceFileCreator extends PreprocessorFileCreator {

    private final IPath newHeaderFilePath;

    public PreprocessorSourceFileCreator(final IPath newHeaderFilePath, final ModificationCollector collector, final ICProject cProject,
                                         final CRefactoringContext context) {
        super(collector, cProject, context);
        this.newHeaderFilePath = newHeaderFilePath;
    }

    @Override
    protected void addContentToTu(final IASTTranslationUnit newAst, final ASTRewrite rewriter, final ICPPASTFunctionDeclarator funDecl,
            final IProgressMonitor pm) throws CoreException {
        addHeaderInclude(newAst, rewriter);
        addUndef(newAst, rewriter, null, funDecl.getName().toString());
        insertFunctionDefinition(funDecl, newAst, rewriter);
    }

    private static void addUndef(final IASTTranslationUnit tu, final ASTRewrite rewriter, final IASTNode insertionPoint, final String funName) {
        new UndefMacroAdder(tu, rewriter, insertionPoint).addUndefMacro(funName);
    }

    private void insertFunctionDefinition(final ICPPASTFunctionDeclarator funDecl, final IASTTranslationUnit source, final ASTRewrite rewriter) {
        final ICPPASTDeclSpecifier newDeclSpec = getReturnValue(funDecl);
        final ICPPASTFunctionDeclarator newFunDecl = createNewFunDecl(funDecl);
        final IASTCompoundStatement funBody = createFunctionBody(funDecl, newFunDecl);
        final ICPPASTFunctionDefinition funDef = nodeFactory.newFunctionDefinition(newDeclSpec, newFunDecl, funBody);
        rewriter.insertBefore(source, null, funDef, null);
    }

    private static IASTCompoundStatement createFunctionBody(final ICPPASTFunctionDeclarator funDecl, final ICPPASTFunctionDeclarator newFunDecl) {
        final int numOfParams = newFunDecl.getParameters().length;
        final Set<Integer> lastTwoParamsToBeIgnored = orderPreservingSet(numOfParams - 2, numOfParams - 1);
        final FunctionDelegateCallCreator creator = new FunctionDelegateCallCreator(newFunDecl, lastTwoParamsToBeIgnored);
        final IASTStatement delegateToOriginal = creator.createDelegate(funDecl.getName(), ASTUtil.getDeclSpec(funDecl));
        return ASTStatementUtil.wrapInCompountStmtIfNecessary(delegateToOriginal);
    }

    private void addHeaderInclude(final IASTTranslationUnit source, final ASTRewrite rewriter) {
        final AstIncludeNode astIncludeNode = new AstIncludeNode(FileUtil.getFilename(newHeaderFilePath.toString()));
        rewriter.insertBefore(source, null, astIncludeNode, null);
    }

    @Override
    protected IASTName getNewFunName(final ICPPASTFunctionDeclarator funDecl) {
        final QualifiedNameCreator resolver = new QualifiedNameCreator(funDecl.getName());
        final ICPPASTQualifiedName qualifiedName = resolver.createQualifiedName();
        final String traceFunName = MockatorConstants.MOCKED_TRACE_PREFIX + funDecl.getName().toString();
        final IASTName newName = nodeFactory.newName(traceFunName.toCharArray());
        qualifiedName.addName(newName);
        return qualifiedName;
    }
}
