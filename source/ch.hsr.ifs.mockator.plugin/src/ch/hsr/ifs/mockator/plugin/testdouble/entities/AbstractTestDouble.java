package ch.hsr.ifs.mockator.plugin.testdouble.entities;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;
import static ch.hsr.ifs.mockator.plugin.base.functional.HigherOrder.filter;
import static ch.hsr.ifs.mockator.plugin.base.functional.HigherOrder.map;

import java.util.Collection;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoringContext;
import org.eclipse.core.runtime.IProgressMonitor;

import ch.hsr.ifs.mockator.plugin.base.dbc.Assert;
import ch.hsr.ifs.mockator.plugin.base.functional.F1;
import ch.hsr.ifs.mockator.plugin.incompleteclass.DefaultCtorProvider;
import ch.hsr.ifs.mockator.plugin.incompleteclass.MissingMemFunFinder;
import ch.hsr.ifs.mockator.plugin.incompleteclass.MissingMemberFunction;
import ch.hsr.ifs.mockator.plugin.incompleteclass.TestDoubleMemFun;
import ch.hsr.ifs.mockator.plugin.incompleteclass.TestDoubleMemFunImplStrategy;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.refsupport.finder.PublicMemFunFinder;
import ch.hsr.ifs.mockator.plugin.refsupport.finder.ReferencingTestFunFinder;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.ClassPublicVisibilityInserter;
import ch.hsr.ifs.mockator.plugin.testdouble.PolymorphismKind;

@SuppressWarnings("restriction")
public abstract class AbstractTestDouble implements TestDouble {
  private final ICPPASTCompositeTypeSpecifier klass;

  public AbstractTestDouble(ICPPASTCompositeTypeSpecifier klass) {
    this.klass = klass;
  }

  @Override
  public PolymorphismKind getPolymorphismKind() {
    return PolymorphismKind.from(klass);
  }

  @Override
  public IASTName getName() {
    return klass.getName();
  }

  @Override
  public ICPPASTCompositeTypeSpecifier getKlass() {
    return klass;
  }

  @Override
  public Collection<ExistingTestDoubleMemFun> getPublicMemFuns() {
    return map(AstUtil.getFunctionDefinitions(collectPublicMemFuns()),
        new F1<ICPPASTFunctionDefinition, ExistingTestDoubleMemFun>() {
          @Override
          public ExistingTestDoubleMemFun apply(ICPPASTFunctionDefinition function) {
            return new ExistingTestDoubleMemFun(function);
          }
        });
  }

  private Collection<IASTDeclaration> collectPublicMemFuns() {
    PublicMemFunFinder finder = new PublicMemFunFinder(klass, PublicMemFunFinder.ALL_TYPES);
    return finder.getPublicMemFuns();
  }

  @Override
  public Collection<ICPPASTFunctionDefinition> getReferencingTestFunctions(
      CRefactoringContext context, ICProject cProject, IProgressMonitor pm) {
    ReferencingTestFunFinder testFunFinder = new ReferencingTestFunFinder(cProject, klass);
    return testFunFinder.findByIndexLookup(context, pm);
  }

  @Override
  public boolean hasOnlyStaticFunctions(Collection<? extends MissingMemberFunction> missingMemFuns) {
    return hasOnlyStatics(getPublicMemFuns()) && hasOnlyStatics(missingMemFuns);
  }

  private static <T extends TestDoubleMemFun> boolean hasOnlyStatics(Collection<T> testDoubleFuns) {
    Collection<T> nonStatics = filter(testDoubleFuns, new F1<T, Boolean>() {
      @Override
      public Boolean apply(T memFun) {
        return !memFun.isStatic();
      }
    });
    return nonStatics.isEmpty();
  }

  @Override
  public Collection<? extends MissingMemberFunction> collectMissingMemFuns(
      MissingMemFunFinder finder, CppStandard cppStd) {
    List<MissingMemberFunction> missingMemFuns = list();
    missingMemFuns.addAll(finder.findMissingMemberFunctions(klass));
    addDefaultCtorIfNecessary(cppStd, missingMemFuns);
    return missingMemFuns;
  }

  private void addDefaultCtorIfNecessary(CppStandard cppStd, List<MissingMemberFunction> missing) {
    DefaultCtorProvider provider = getDefaultCtorProvider(cppStd);

    for (MissingMemberFunction optDefaultCtor : provider.createMissingDefaultCtor(missing)) {
      missing.add(0, optDefaultCtor);
    }
  }

  @Override
  public abstract DefaultCtorProvider getDefaultCtorProvider(CppStandard cppStd);

  @Override
  public void addMissingMemFuns(Collection<? extends MissingMemberFunction> missingMemFuns,
      ClassPublicVisibilityInserter inserter, CppStandard cppStd) {
    for (MissingMemberFunction m : missingMemFuns) {
      TestDoubleMemFunImplStrategy implStrategy = getImplStrategy(cppStd);
      inserter.insert(m.createFunctionDefinition(implStrategy, cppStd));
    }
  }

  protected abstract TestDoubleMemFunImplStrategy getImplStrategy(CppStandard cppStd);

  @Override
  public abstract void addAdditionalCtorSupport(ICPPASTFunctionDefinition defaultCtor,
      CppStandard cppStd);

  @Override
  public ICPPClassType getClassType() {
    IBinding binding = getName().resolveBinding();
    Assert.instanceOf(binding, ICPPClassType.class, "Test double must be of class type");
    return (ICPPClassType) binding;
  }

  @Override
  public boolean hasPublicCtor() {
    Collection<IASTDeclaration> onlyCtors =
        filter(collectPublicMemFuns(), new F1<IASTDeclaration, Boolean>() {
          @Override
          public Boolean apply(IASTDeclaration function) {
            return AstUtil.isDeclConstructor(function);
          }
        });
    return !onlyCtors.isEmpty();
  }

  @Override
  public IASTNode getParent() {
    IASTNode currentNode = klass;

    while (currentNode != null) {
      if (currentNode instanceof ICPPASTFunctionDefinition)
        return ((ICPPASTFunctionDefinition) currentNode).getBody();

      if (currentNode instanceof ICPPASTNamespaceDefinition)
        return currentNode;

      currentNode = currentNode.getParent();
    }
    return null;
  }

  @Override
  public String toString() {
    return getName().toString();
  }
}
