package ch.hsr.ifs.mockator.plugin.incompleteclass.subtype;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.head;
import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;
import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.orderPreservingSet;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
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

import ch.hsr.ifs.iltis.core.exception.ILTISException;


import ch.hsr.ifs.mockator.plugin.incompleteclass.MissingMemFunFinder;
import ch.hsr.ifs.mockator.plugin.refsupport.lookup.NodeLookup;
import ch.hsr.ifs.iltis.cpp.ast.ASTUtil;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.PureVirtualMemFunCollector;


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
      ILTISException.Unless.instanceOf(binding, ICPPClassType.class, "Class type expected");
      return collectPureVirtualMemFuns(clazz.getTranslationUnit(), (ICPPClassType) binding);
   }

   private Set<MissingMemFun> collectPureVirtualMemFuns(final IASTTranslationUnit ast, final ICPPClassType clazz) {
      final Set<MissingMemFun> missingMethods = orderPreservingSet();

      for (final ICPPMethod method : getPureVirtualMemFunsIn(clazz)) {
         ILTISException.Unless.notInstanceOf(method, ICPPConstructor.class, "Ctors are not supported because they are not inherited");
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
      final IASTSimpleDeclaration simpleDecl = ASTUtil.getAncestorOfType(name, IASTSimpleDeclaration.class);
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
