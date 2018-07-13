package ch.hsr.ifs.cute.mockator.testdouble.entities;

import java.util.Collection;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.runtime.IProgressMonitor;

import ch.hsr.ifs.iltis.cpp.core.wrappers.CRefactoringContext;

import ch.hsr.ifs.cute.mockator.incompleteclass.DefaultCtorProvider;
import ch.hsr.ifs.cute.mockator.incompleteclass.MissingMemFunFinder;
import ch.hsr.ifs.cute.mockator.incompleteclass.MissingMemberFunction;
import ch.hsr.ifs.cute.mockator.project.properties.CppStandard;
import ch.hsr.ifs.cute.mockator.refsupport.utils.ClassPublicVisibilityInserter;
import ch.hsr.ifs.cute.mockator.testdouble.PolymorphismKind;


public interface TestDouble {

   PolymorphismKind getPolymorphismKind();

   IASTName getName();

   ICPPASTCompositeTypeSpecifier getKlass();

   Collection<ExistingTestDoubleMemFun> getPublicMemFuns();

   Collection<ICPPASTFunctionDefinition> getReferencingTestFunctions(CRefactoringContext c, ICProject p, IProgressMonitor pm);

   boolean hasOnlyStaticFunctions(Collection<? extends MissingMemberFunction> missingMemFuns);

   Collection<? extends MissingMemberFunction> collectMissingMemFuns(MissingMemFunFinder finder, CppStandard cppStd);

   DefaultCtorProvider getDefaultCtorProvider(CppStandard cppStd);

   void addMissingMemFuns(Collection<? extends MissingMemberFunction> missingMemFuns, ClassPublicVisibilityInserter inserter, CppStandard cppStd);

   void addAdditionalCtorSupport(ICPPASTFunctionDefinition defaultCtor, CppStandard cppStd);

   ICPPClassType getClassType();

   boolean hasPublicCtor();

   IASTNode getParent();

   void addToNamespace(ICPPASTNamespaceDefinition parentNs, IASTSimpleDeclaration testDouble, ICPPASTCompositeTypeSpecifier toMove,
         CppStandard cppStd, ASTRewrite rewriter);
}
