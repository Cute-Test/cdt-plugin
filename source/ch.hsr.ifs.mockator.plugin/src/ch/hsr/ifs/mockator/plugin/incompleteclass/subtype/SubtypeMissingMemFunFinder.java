package ch.hsr.ifs.mockator.plugin.incompleteclass.subtype;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.head;
import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;
import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.orderPreservingSet;
import static ch.hsr.ifs.mockator.plugin.base.maybe.Maybe.maybe;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
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

import ch.hsr.ifs.mockator.plugin.base.dbc.Assert;
import ch.hsr.ifs.mockator.plugin.base.maybe.Maybe;
import ch.hsr.ifs.mockator.plugin.incompleteclass.MissingMemFunFinder;
import ch.hsr.ifs.mockator.plugin.refsupport.lookup.NodeLookup;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.PureVirtualMemFunCollector;

public class SubtypeMissingMemFunFinder implements MissingMemFunFinder {
  private final IIndex index;
  private final ICProject cProject;
  private final IProgressMonitor pm;

  public SubtypeMissingMemFunFinder(ICProject cProject, IIndex index) {
    this(cProject, index, new NullProgressMonitor());
  }

  public SubtypeMissingMemFunFinder(ICProject cProject, IIndex index, IProgressMonitor pm) {
    this.cProject = cProject;
    this.index = index;
    this.pm = pm;
  }

  @Override
  public Collection<MissingMemFun> findMissingMemberFunctions(ICPPASTCompositeTypeSpecifier klass) {
    IBinding binding = klass.getName().resolveBinding();
    Assert.instanceOf(binding, ICPPClassType.class, "Class type expected");
    return collectPureVirtualMemFuns(klass.getTranslationUnit(), (ICPPClassType) binding);
  }

  private Set<MissingMemFun> collectPureVirtualMemFuns(IASTTranslationUnit ast, ICPPClassType klass) {
    Set<MissingMemFun> missingMethods = orderPreservingSet();

    for (ICPPMethod method : getPureVirtualMemFunsIn(klass)) {
      Assert.notInstanceOf(method, ICPPConstructor.class,
          "Ctors are not supported because they are not inherited");
      IASTName[] declarations = ast.getDeclarationsInAST(method);

      if (declarations.length > 0) {
        collectIfFound(missingMethods, declarations[0]);
      } else {
        for (IASTName declName : lookUpInIndex(method)) {
          collectIfFound(missingMethods, declName);
        }
      }
    }
    return missingMethods;
  }

  private Maybe<IASTName> lookUpInIndex(ICPPMethod method) {
    NodeLookup lookup = new NodeLookup(cProject, pm);
    return head(lookup.findDeclarations(method, index));
  }

  private static void collectIfFound(Set<MissingMemFun> missingMethods, IASTName declName) {
    for (IASTSimpleDeclaration optDecl : getFunDeclarator(declName)) {
      missingMethods.add(new MissingMemFun(optDecl));
    }
  }

  private static Maybe<IASTSimpleDeclaration> getFunDeclarator(IASTName name) {
    IASTSimpleDeclaration simpleDecl = AstUtil.getAncestorOfType(name, IASTSimpleDeclaration.class);
    return maybe(simpleDecl);
  }

  private Collection<ICPPMethod> getPureVirtualMemFunsIn(ICPPClassType klass) {
    List<ICPPMethod> pureVirtualMemFuns =
        list(new PureVirtualMemFunCollector(klass).collectPureVirtualMemFuns());
    Collections.sort(pureVirtualMemFuns, createMethodNameComparator());
    return pureVirtualMemFuns;
  }

  private static Comparator<ICPPMethod> createMethodNameComparator() {
    return new Comparator<ICPPMethod>() {
      @Override
      public int compare(ICPPMethod m1, ICPPMethod m2) {
        return m1.getName().compareTo(m2.getName());
      }
    };
  }
}
