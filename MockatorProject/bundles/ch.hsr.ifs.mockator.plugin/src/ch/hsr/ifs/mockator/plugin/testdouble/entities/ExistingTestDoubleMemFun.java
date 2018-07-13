package ch.hsr.ifs.mockator.plugin.testdouble.entities;

import static ch.hsr.ifs.iltis.core.core.collections.CollectionUtil.list;

import java.util.Collection;
import java.util.Optional;

import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;

import ch.hsr.ifs.iltis.cpp.core.wrappers.CPPVisitor;

import ch.hsr.ifs.mockator.plugin.incompleteclass.AbstractTestDoubleMemFun;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.project.properties.LinkedEditModeStrategy;
import ch.hsr.ifs.mockator.plugin.refsupport.functions.FunctionSignatureFormatter;
import ch.hsr.ifs.mockator.plugin.refsupport.functions.params.DefaultArgumentCreator;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.BindingTypeVerifier;
import ch.hsr.ifs.mockator.plugin.testdouble.CallRegistrationFinder;
import ch.hsr.ifs.mockator.plugin.testdouble.MemFunMockSupportAdder;
import ch.hsr.ifs.mockator.plugin.testdouble.support.MemFunSignature;


public class ExistingTestDoubleMemFun extends AbstractTestDoubleMemFun {

   private final ICPPASTFunctionDefinition function;

   public ExistingTestDoubleMemFun(final ICPPASTFunctionDefinition function) {
      this.function = function;
   }

   private ICPPASTFunctionDeclarator getFunDecl() {
      return (ICPPASTFunctionDeclarator) function.getDeclarator();
   }

   private IASTName getFunctionName() {
      return getFunDecl().getName();
   }

   public Optional<? extends MemFunSignature> getRegisteredCall(final CallRegistrationFinder finder) {
      return finder.findRegisteredCall(function);
   }

   public void addMockSupport(final MemFunMockSupportAdder mockSupportAdder, final CallRegistrationFinder finder) {
      if (hasAlreadyMockSupport(finder)) { return; }

      mockSupportAdder.addMockSupport(function);
   }

   private boolean hasAlreadyMockSupport(final CallRegistrationFinder finder) {
      return getRegisteredCall(finder).isPresent();
   }

   @Override
   public String getFunctionSignature() {
      return new FunctionSignatureFormatter(getFunDecl()).getFunctionSignature();
   }

   @Override
   public Collection<IASTInitializerClause> createDefaultArguments(final CppStandard cppStd, final LinkedEditModeStrategy linkedEditStrategy) {
      return new DefaultArgumentCreator(linkedEditStrategy, cppStd).createDefaultArguments(getFunParams());
   }

   private Collection<ICPPASTParameterDeclaration> getFunParams() {
      return list(getFunDecl().getParameters());
   }

   @Override
   public boolean isStatic() {
      final IBinding binding = getFunctionName().resolveBinding();
      return ((ICPPFunction) binding).isStatic();
   }

   public boolean isConstructor() {
      return BindingTypeVerifier.isOfType(getFunctionName().resolveBinding(), ICPPConstructor.class);
   }

   public ICPPASTCompositeTypeSpecifier getContainingClass() {
      return CPPVisitor.findAncestorWithType(getFunDecl(), ICPPASTCompositeTypeSpecifier.class).orElse(null);
   }
}
