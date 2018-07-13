package ch.hsr.ifs.cute.mockator.incompleteclass.subtype;

import static ch.hsr.ifs.iltis.core.core.collections.CollectionUtil.head;
import static ch.hsr.ifs.iltis.core.core.collections.CollectionUtil.list;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import ch.hsr.ifs.iltis.core.core.exception.ILTISException;
import ch.hsr.ifs.iltis.cpp.core.wrappers.CPPVisitor;

import ch.hsr.ifs.cute.mockator.incompleteclass.MissingMemFunFinder;
import ch.hsr.ifs.cute.mockator.refsupport.lookup.NodeLookup;
import ch.hsr.ifs.cute.mockator.refsupport.utils.PureVirtualMemFunCollector;


public class SubtypeMissingMemFunFinder implements MissingMemFunFinder {

   private final IIndex           index;
   private final ICProject        cProject;
   private final IProgressMonitor pm;

   public SubtypeMissingMemFunFinder(final ICProject cProject, final IIndex index) {
      this(cProject, index, new NullProgressMonitor());
   }

   public SubtypeMissingMemFunFinder(final ICProject cProject, final IIndex index, final IProgressMonitor pm) {
      this.cProject = cProject;
      this.index = index;
      this.pm = pm;
   }

   @Override
   public Collection<MissingMemFun> findMissingMemberFunctions(final ICPPASTCompositeTypeSpecifier clazz) {
      final IBinding binding = clazz.getName().resolveBinding();
      ILTISException.Unless.assignableFrom("Class type expected", ICPPClassType.class, binding);
      return collectPureVirtualMemFuns(clazz.getTranslationUnit(), (ICPPClassType) binding);
   }

   private Set<MissingMemFun> collectPureVirtualMemFuns(final IASTTranslationUnit ast, final ICPPClassType clazz) {
      final Set<MissingMemFun> missingMethods = new LinkedHashSet<>();

      for (final ICPPMethod method : getPureVirtualMemFunsIn(clazz)) {
         ILTISException.Unless.notAssignableFrom("Ctors are not supported because they are not inherited", ICPPConstructor.class, method);
         final IASTName[] declarations = ast.getDeclarationsInAST(method);

         if (declarations.length > 0) {
            collectIfFound(missingMethods, declarations[0]);
         } else {
            lookUpInIndex(method).ifPresent((declName) -> collectIfFound(missingMethods, declName));
         }
      }
      return missingMethods;
   }

   private Optional<IASTName> lookUpInIndex(final ICPPMethod method) {
      final NodeLookup lookup = new NodeLookup(cProject, pm);
      return head(lookup.findDeclarations(method, index));
   }

   private static void collectIfFound(final Set<MissingMemFun> missingMethods, final IASTName declName) {
      getFunDeclarator(declName).ifPresent((decl) -> missingMethods.add(new MissingMemFun(decl)));
   }

   private static Optional<IASTSimpleDeclaration> getFunDeclarator(final IASTName name) {
      final IASTSimpleDeclaration simpleDecl = CPPVisitor.findAncestorWithType(name, IASTSimpleDeclaration.class).orElse(null);
      return Optional.of(simpleDecl);
   }

   private Collection<ICPPMethod> getPureVirtualMemFunsIn(final ICPPClassType clazz) {
      final List<ICPPMethod> pureVirtualMemFuns = list(new PureVirtualMemFunCollector(clazz).collectPureVirtualMemFuns());
      Collections.sort(pureVirtualMemFuns, createMethodNameComparator());
      return pureVirtualMemFuns;
   }

   private static Comparator<ICPPMethod> createMethodNameComparator() {
      return (m1, m2) -> m1.getName().compareTo(m2.getName());
   }
}
