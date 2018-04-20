package ch.hsr.ifs.mockator.plugin.testdouble.support;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.cdt.core.dom.ast.ASTNodeFactoryFactory;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNodeFactory;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;

import ch.hsr.ifs.iltis.core.core.exception.ILTISException;
import ch.hsr.ifs.iltis.cpp.core.ast.ASTUtil;

import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.refsupport.functions.params.types.DeclSpecGenerator;


public class BaseClassCtorCallHandler {

   private static final ICPPNodeFactory nodeFactory = ASTNodeFactoryFactory.getDefaultCPPNodeFactory();
   private final ICPPClassType          baseClass;

   public BaseClassCtorCallHandler(final ICPPClassType testDouble) {
      final ICPPBase[] bases = testDouble.getBases();
      ILTISException.Unless.isFalse("Test double is expected to at least one base class!", bases.length < 1);
      final IBinding binding = bases[0].getBaseClass(); // just consider the first one
      ILTISException.Unless.assignableFrom("Class type as base class expected", ICPPClassType.class, binding);
      baseClass = (ICPPClassType) binding;
   }

   public boolean hasBaseClassDefaultCtor() {
      final Collection<ICPPConstructor> defaultCtors = Arrays.asList(baseClass.getConstructors()).stream().filter((ctor) -> ASTUtil.isDefaultCtor(
            ctor)).collect(Collectors.toList());
      return !defaultCtors.isEmpty();
   }

   public Optional<ICPPASTConstructorChainInitializer> getBaseClassInitializer(final CppStandard cppStd) {
      final ICPPConstructor baseCtor = getBaseCtorWithMinParams();

      if (baseCtor == null) { return Optional.empty(); }

      final List<IASTInitializerClause> clauses = getInitializerClauses(baseCtor, cppStd);
      return Optional.of(getInitializer(baseCtor, clauses));
   }

   private static ICPPASTConstructorChainInitializer getInitializer(final ICPPConstructor baseCtor, final List<IASTInitializerClause> initializers) {
      final ICPPASTConstructorInitializer ctorInitializer = nodeFactory.newConstructorInitializer(initializers.toArray(
            new IASTInitializerClause[initializers.size()]));
      final IASTName baseCtorName = nodeFactory.newName(baseCtor.getName().toCharArray());
      return nodeFactory.newConstructorChainInitializer(baseCtorName, ctorInitializer);
   }

   private static List<IASTInitializerClause> getInitializerClauses(final ICPPConstructor baseCtor, final CppStandard cppStd) {
      final List<IASTInitializerClause> clauses = new ArrayList<>();

      for (final ICPPParameter param : baseCtor.getParameters()) {
         final ICPPASTDeclSpecifier declSpec = new DeclSpecGenerator(param.getType()).getDeclSpec();
         declSpec.setStorageClass(IASTDeclSpecifier.sc_unspecified);
         clauses.add(nodeFactory.newSimpleTypeConstructorExpression(declSpec, cppStd.getEmptyInitializer()));
      }

      return clauses;
   }

   private ICPPConstructor getBaseCtorWithMinParams() {
      int minArgCount = Integer.MAX_VALUE;
      ICPPConstructor chosen = null;

      for (final ICPPConstructor ctor : baseClass.getConstructors()) {
         if (ASTUtil.isDefaultCtor(ctor) || ASTUtil.isCopyCtor(ctor, baseClass)) {
            continue;
         }

         final int requiredArgs = ctor.getRequiredArgumentCount();
         if (requiredArgs < minArgCount) {
            minArgCount = requiredArgs;
            chosen = ctor;
         }
      }
      return chosen;
   }
}
