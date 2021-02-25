package ch.hsr.ifs.cute.mockator.testdouble.creation.subtype;

import java.util.Optional;

import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNameSpecifier;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.ITextSelection;

import ch.hsr.ifs.iltis.core.resources.StringUtil;

import ch.hsr.ifs.iltis.cpp.core.wrappers.ModificationCollector;

import ch.hsr.ifs.cute.mockator.infos.CreateTestDoubleSubTypeInfo;
import ch.hsr.ifs.cute.mockator.refsupport.includes.AstIncludeNode;
import ch.hsr.ifs.cute.mockator.testdouble.creation.AbstractCreateTestDoubleRefactoring;


public class CreateTestDoubleSubTypeRefactoring extends AbstractCreateTestDoubleRefactoring {

    private final CreateTestDoubleSubTypeInfo info;

    public CreateTestDoubleSubTypeRefactoring(final ICElement cElement, final Optional<ITextSelection> sel, final CreateTestDoubleSubTypeInfo info) {
        super(cElement, sel, null);
        this.info = info;
    }

    @Override
    protected void collectModifications(final IProgressMonitor pm, final ModificationCollector collector) throws CoreException,
            OperationCanceledException {
        final IASTTranslationUnit ast = getAST(getTranslationUnit(), pm);
        final ASTRewrite rewriter = collector.rewriterForTranslationUnit(ast);
        addIncludeIfNecessary(ast, rewriter);
        replaceKindOfPassingArgIfNecessary(ast, rewriter);
        insertBeforeCurrentStmt(createNewTestDoubleClass(), ast, rewriter);
    }

    private void addIncludeIfNecessary(final IASTTranslationUnit ast, final ASTRewrite rewriter) {
        final String targetIncludePath = info.targetIncludePath;
        final AstIncludeNode includeNode = new AstIncludeNode(targetIncludePath);
        includeNode.insertInTu(ast, rewriter);
    }

    private void replaceKindOfPassingArgIfNecessary(final IASTTranslationUnit ast, final ASTRewrite rewriter) {
        getSelectedName(ast).ifPresent((problemArgName) -> info.passByStrategy.adaptArguments(problemArgName, info.nameOfMissingInstance, rewriter));
    }

    private IASTDeclarationStatement createNewTestDoubleClass() {
        final String className = StringUtil.capitalize(info.nameOfMissingInstance);
        final ICPPASTCompositeTypeSpecifier newClass = createNewTestDoubleClass(className);
        addPublicInheritance(newClass);
        final IASTSimpleDeclaration newSimpleDeclaration = nodeFactory.newSimpleDeclaration(newClass);
        addClassInstance(newSimpleDeclaration);
        return nodeFactory.newDeclarationStatement(newSimpleDeclaration);
    }

    private void addClassInstance(final IASTSimpleDeclaration newSimpleDeclaration) {
        final IASTName classInstanceName = nodeFactory.newName(info.nameOfMissingInstance);
        newSimpleDeclaration.addDeclarator(nodeFactory.newDeclarator(classInstanceName));
    }

    private void addPublicInheritance(final ICPPASTCompositeTypeSpecifier newClass) {
        final boolean nonVirtual = false;
        final int noVisibility = 0; /* we always create the new test doubles as 'struct' and use default (public) visibility */
        final IASTName parentClassName = nodeFactory.newName(info.parentClassName);
        final ICPPASTBaseSpecifier baseSpecifier = nodeFactory.newBaseSpecifier((ICPPASTNameSpecifier) parentClassName, noVisibility, nonVirtual);
        newClass.addBaseSpecifier(baseSpecifier);
    }
}
