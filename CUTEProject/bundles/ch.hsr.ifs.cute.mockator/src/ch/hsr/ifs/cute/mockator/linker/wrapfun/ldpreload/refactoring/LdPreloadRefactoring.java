package ch.hsr.ifs.cute.mockator.linker.wrapfun.ldpreload.refactoring;

import java.util.Optional;

import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.ITextSelection;

import ch.hsr.ifs.iltis.core.exception.ILTISException;
import ch.hsr.ifs.iltis.core.resources.FileUtil;

import ch.hsr.ifs.iltis.cpp.core.ast.ASTUtil;
import ch.hsr.ifs.iltis.cpp.core.wrappers.CPPVisitor;
import ch.hsr.ifs.iltis.cpp.core.wrappers.ModificationCollector;

import ch.hsr.ifs.cute.mockator.MockatorConstants;
import ch.hsr.ifs.cute.mockator.base.i18n.I18N;
import ch.hsr.ifs.cute.mockator.base.util.PathProposalUtil;
import ch.hsr.ifs.cute.mockator.linker.LinkerRefactoring;
import ch.hsr.ifs.cute.mockator.project.properties.CppStandard;
import ch.hsr.ifs.cute.mockator.refsupport.includes.AstIncludeNode;
import ch.hsr.ifs.cute.mockator.refsupport.includes.CppIncludeResolver;
import ch.hsr.ifs.cute.mockator.refsupport.tu.TranslationUnitCreator;
import ch.hsr.ifs.cute.mockator.refsupport.utils.QualifiedNameCreator;


public class LdPreloadRefactoring extends LinkerRefactoring {

    private final CppStandard cppStd;
    private final IProject    targetProject;
    private IFile             newFile;

    public LdPreloadRefactoring(final CppStandard cppStd, final ICElement element, final Optional<ITextSelection> selection, final ICProject cProject,
                                final IProject targetProject) {
        super(element, selection, cProject);
        this.cppStd = cppStd;
        this.targetProject = targetProject;
    }

    @Override
    protected void createLinkerSeamSupport(final ModificationCollector collector, final IASTName funName, final IProgressMonitor pm)
            throws CoreException {
        final Optional<ICPPASTFunctionDeclarator> optFunDecl = findFunDeclaration(funName, pm);
        if (optFunDecl.isPresent()) {
            final IASTTranslationUnit newTu = createAndGetNewTu(funName.toString(), pm);
            final ASTRewrite rewriter = collector.rewriterForTranslationUnit(newTu);
            insertFunDeclInclude(optFunDecl.get(), newTu, rewriter);
            insertSupportingIncludes(optFunDecl.get(), newTu, rewriter);
            insertFunDefinition(createFunDefinition(optFunDecl.get()), newTu, rewriter);
        }
    }

    private static void insertSupportingIncludes(final ICPPASTFunctionDeclarator funDecl, final IASTTranslationUnit tu, final ASTRewrite rewriter) {
        final AstIncludeNode dlfcn = new AstIncludeNode("dlfcn.h", true);
        dlfcn.insertInTu(tu, rewriter);

        if (isMemberFunction(funDecl)) {
            final AstIncludeNode cstring = new AstIncludeNode("cstring", true);
            cstring.insertInTu(tu, rewriter);
        }
    }

    private static boolean isMemberFunction(final ICPPASTFunctionDeclarator funDecl) {
        return CPPVisitor.findAncestorWithType(funDecl, ICPPASTCompositeTypeSpecifier.class).orElse(null) != null;
    }

    private void insertFunDeclInclude(final ICPPASTFunctionDeclarator funDecl, final IASTTranslationUnit tu, final ASTRewrite rewriter) {
        try {
            final CppIncludeResolver resolver = new CppIncludeResolver(tu, getProject(), getIndex());
            final AstIncludeNode includeForFunDecl = resolver.resolveIncludeNode(funDecl.getTranslationUnit().getFilePath());
            rewriter.insertBefore(tu, null, includeForFunDecl, null);
        } catch (final Exception e) {
            throw new ILTISException(e).rethrowUnchecked();
        }
    }

    private static void insertFunDefinition(final ICPPASTFunctionDefinition funDef, final IASTTranslationUnit newTu, final ASTRewrite rewriter) {
        rewriter.insertBefore(newTu, null, funDef, null);
    }

    private IASTTranslationUnit createAndGetNewTu(final String funName, final IProgressMonitor pm) throws CoreException {
        final IPath newLocation = getPathForNewFile(funName);
        newFile = FileUtil.toIFile(newLocation);
        final TranslationUnitCreator creator = new TranslationUnitCreator(targetProject, refactoringContext);
        return creator.createAndGetNewTu(newLocation, pm);
    }

    private IPath getPathForNewFile(final String funName) {
        final PathProposalUtil proposal = new PathProposalUtil(targetProject.getFullPath());
        return proposal.getUniquePathForNewFile(funName, MockatorConstants.SOURCE_SUFFIX);
    }

    private ICPPASTFunctionDefinition createFunDefinition(final ICPPASTFunctionDeclarator funDecl) {
        final ICPPASTDeclSpecifier newDeclSpec = getNewFunDeclSpec(funDecl);
        ASTUtil.removeExternalStorageIfSet(newDeclSpec);
        final ICPPASTFunctionDeclarator newFunDecl = funDecl.copy();
        adjustParamNamesIfNecessary(newFunDecl);
        newFunDecl.setName(getFullyQualifiedName(funDecl));
        final IASTCompoundStatement funBody = getFunBody(funDecl);
        return nodeFactory.newFunctionDefinition(newDeclSpec, newFunDecl, funBody);
    }

    private static ICPPASTDeclSpecifier getNewFunDeclSpec(final ICPPASTFunctionDeclarator funDecl) {
        return ASTUtil.getDeclSpec(funDecl).copy();
    }

    private IASTCompoundStatement getFunBody(final ICPPASTFunctionDeclarator funDecl) {
        final LdPreloadFunBodyFactory factory = new LdPreloadFunBodyFactory();
        final LdPreloadFunBodyStrategy funBodyStrategy = factory.getFunBodyStrategy(funDecl);
        return funBodyStrategy.getPreloadFunBody(cppStd, funDecl);
    }

    private static IASTName getFullyQualifiedName(final ICPPASTFunctionDeclarator funDecl) {
        final QualifiedNameCreator resolver = new QualifiedNameCreator(funDecl.getName());
        final ICPPASTQualifiedName qualifiedName = resolver.createQualifiedName();
        qualifiedName.addName(funDecl.getName().copy());
        return qualifiedName;
    }

    public IFile getNewFile() {
        return newFile;
    }

    @Override
    public String getDescription() {
        return I18N.LdPreloadRefactoringDesc;
    }
}
