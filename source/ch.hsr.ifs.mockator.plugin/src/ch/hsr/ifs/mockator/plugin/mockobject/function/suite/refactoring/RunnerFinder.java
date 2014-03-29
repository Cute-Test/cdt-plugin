/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil, Switzerland,
 * http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any purpose without fee is hereby
 * granted, provided that the above copyright notice and this permission notice appear in all
 * copies.
 ******************************************************************************/
package ch.hsr.ifs.mockator.plugin.mockobject.function.suite.refactoring;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;

import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubMonitor;

import ch.hsr.ifs.mockator.plugin.base.functional.F1;
import ch.hsr.ifs.mockator.plugin.base.i18n.I18N;
import ch.hsr.ifs.mockator.plugin.base.maybe.Maybe;
import ch.hsr.ifs.mockator.plugin.refsupport.finder.NameFinder;
import ch.hsr.ifs.mockator.plugin.refsupport.tu.TranslationUnitLoader;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.NodeContainer;

// Copied and adapted from CUTE
public class RunnerFinder {
  private static final String MAKE_RUNNER_FQ = "cute::makeRunner";
  private final IIndex index;
  private final ICProject cProject;

  public RunnerFinder(ICProject cProject) throws CoreException {
    this.cProject = cProject;
    index = CCorePlugin.getIndexManager().getIndex(cProject);
  }

  public List<IASTFunctionDefinition> findTestRunners(IProgressMonitor monitor)
      throws CoreException {
    SubMonitor mon = SubMonitor.convert(monitor, 2);

    try {
      mon.beginTask(I18N.RunnerFinderFindMain, 1);
      IASTFunctionDefinition mainFunc = findMain(mon);
      mon.beginTask(I18N.RunnerFinderFindRunners, 1);
      return getTestRunnersFunctions(mainFunc, mon);
    } finally {
      mon.done();
    }
  }

  private List<IASTFunctionDefinition> getTestRunnersFunctions(IASTFunctionDefinition mainFunc,
      IProgressMonitor pm) throws CoreException {
    if (mainFunc == null)
      return list();

    IIndex index = mainFunc.getTranslationUnit().getIndex();

    try {
      index.acquireReadLock();
      List<IASTFunctionCallExpression> funcCalls = getFunctionCalls(mainFunc);
      List<IASTFunctionDefinition> testRunners = list();

      for (IASTFunctionCallExpression callExpression : funcCalls) {
        if (callExpression.getFunctionNameExpression() instanceof IASTIdExpression) {
          IASTIdExpression idExp = (IASTIdExpression) callExpression.getFunctionNameExpression();
          IBinding binding = idExp.getName().resolveBinding();
          IASTName[] defs = mainFunc.getTranslationUnit().getDefinitionsInAST(binding);

          if (defs.length > 0) {
            addTestRunnerFuncDef(testRunners, defs);
          } else {
            IIndexName[] indexDefs = index.findDefinitions(binding);
            IASTTranslationUnit ast = getAST(indexDefs[0], pm);

            for (IASTName optName : findName(ast, String.valueOf(indexDefs[0].getSimpleID()))) {
              defs = ast.getDeclarationsInAST(optName.resolveBinding());

              if (defs.length > 0) {
                addTestRunnerFuncDef(testRunners, defs);
              }
            }
          }
        }
      }

      return testRunners;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    } finally {
      index.releaseReadLock();
    }

    return list();
  }

  private static Maybe<IASTName> findName(IASTTranslationUnit ast, final String nameToLookFor) {
    NameFinder nameFinder = new NameFinder(ast);
    return nameFinder.getNameMatchingCriteria(new F1<IASTName, Boolean>() {
      @Override
      public Boolean apply(IASTName name) {
        return nameToLookFor.equals(name.toString());
      }
    });
  }

  private static void addTestRunnerFuncDef(List<IASTFunctionDefinition> testRunners, IASTName[] defs) {
    IASTFunctionDefinition funcDef = getFunctionDefinition(defs[0]);

    if (isTestRunner(funcDef)) {
      testRunners.add(funcDef);
    }
  }

  private static boolean isTestRunner(IASTFunctionDefinition funcDef) {
    TestRunnerVisitor finder = new TestRunnerVisitor();
    funcDef.getBody().accept(finder);
    return finder.res;
  }

  private static List<IASTFunctionCallExpression> getFunctionCalls(IASTFunctionDefinition mainFunc) {
    final List<IASTFunctionCallExpression> funCalls = list();
    mainFunc.getBody().accept(new ASTVisitor() {
      {
        shouldVisitStatements = true;
      }

      @Override
      public int visit(IASTStatement statement) {
        if (statement instanceof IASTExpressionStatement) {
          IASTExpressionStatement expStmt = (IASTExpressionStatement) statement;

          if (expStmt.getExpression() instanceof IASTFunctionCallExpression) {
            IASTFunctionCallExpression funcCall =
                (IASTFunctionCallExpression) expStmt.getExpression();
            funCalls.add(funcCall);
          }
        }

        return PROCESS_CONTINUE;
      }
    });
    return funCalls;
  }

  private IASTFunctionDefinition findMain(SubMonitor m) throws CoreException {
    try {
      index.acquireReadLock();
      IIndexBinding[] bindings =
          index.findBindings("main".toCharArray(), IndexFilter.ALL, new NullProgressMonitor());

      if (bindings.length > 0) {
        IIndexName[] main = index.findDefinitions(bindings[0]);
        IASTTranslationUnit ast = getAST(main[0], m);

        for (IASTName optMain : findDefinitionInTranslationUnit(ast, main[0]))
          return getFunctionDefinition(optMain);
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    } finally {
      index.releaseReadLock();
    }

    return null;
  }

  private static Maybe<IASTName> findDefinitionInTranslationUnit(IASTTranslationUnit ast,
      final IIndexName iName) {
    final NodeContainer<IASTName> defName = new NodeContainer<IASTName>();
    ast.accept(new ASTVisitor() {
      {
        shouldVisitNames = true;
      }

      @Override
      public int visit(IASTName name) {
        if (name.isDefinition() && name.getNodeLocations().length > 0) {
          IASTNodeLocation nodeLocation = name.getNodeLocations()[0];

          if (isSame(iName, nodeLocation)) {
            defName.setNode(name);
            return PROCESS_ABORT;
          }
        }
        return PROCESS_CONTINUE;
      }

    });
    return defName.getNode();
  }

  private static boolean isSame(IIndexName iName, IASTNodeLocation nodeLocation) {
    Path fileName = new Path(nodeLocation.asFileLocation().getFileName());
    return iName.getNodeOffset() == nodeLocation.getNodeOffset()
        && iName.getNodeLength() == nodeLocation.getNodeLength()
        && new Path(iName.getFileLocation().getFileName()).equals(fileName);
  }

  private static IASTFunctionDefinition getFunctionDefinition(IASTName name) {
    return AstUtil.getAncestorOfType(name, IASTFunctionDefinition.class);
  }

  private IASTTranslationUnit getAST(IIndexName iName, IProgressMonitor pm) throws CoreException {
    TranslationUnitLoader loader = new TranslationUnitLoader(cProject, index, pm);
    return loader.loadAst(iName);
  }

  private static final class TestRunnerVisitor extends ASTVisitor {
    private boolean res = false;

    {
      shouldVisitNames = true;
    }

    @Override
    public int visit(IASTName name) {
      if (AstUtil.isQualifiedName(name)) {
        ICPPASTQualifiedName qName = (ICPPASTQualifiedName) name;

        if (qName.toString().equals(MAKE_RUNNER_FQ)) {
          res = true;
          return PROCESS_ABORT;
        }
      }

      return PROCESS_CONTINUE;
    }
  }
}
