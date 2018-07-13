/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil, Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any purpose without fee is hereby granted, provided that the above copyright notice
 * and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.mockator.mockobject.function.suite.refactoring;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

import ch.hsr.ifs.iltis.cpp.core.ast.ASTUtil;
import ch.hsr.ifs.iltis.cpp.core.wrappers.CPPVisitor;

import ch.hsr.ifs.cute.mockator.base.i18n.I18N;
import ch.hsr.ifs.cute.mockator.refsupport.finder.NameFinder;
import ch.hsr.ifs.cute.mockator.refsupport.tu.TranslationUnitLoader;
import ch.hsr.ifs.cute.mockator.refsupport.utils.NodeContainer;


// Copied and adapted from CUTE
public class RunnerFinder {

   private static final String MAKE_RUNNER_FQ = "cute::makeRunner";
   private final IIndex        index;
   private final ICProject     cProject;

   public RunnerFinder(final ICProject cProject) throws CoreException {
      this.cProject = cProject;
      index = CCorePlugin.getIndexManager().getIndex(cProject);
   }

   public List<IASTFunctionDefinition> findTestRunners(final IProgressMonitor monitor) throws CoreException {
      final SubMonitor mon = SubMonitor.convert(monitor, 2);

      try {
         mon.beginTask(I18N.RunnerFinderFindMain, 1);
         final IASTFunctionDefinition mainFunc = findMain(mon);
         mon.beginTask(I18N.RunnerFinderFindRunners, 1);
         return getTestRunnersFunctions(mainFunc, mon);
      } finally {
         mon.done();
      }
   }

   private List<IASTFunctionDefinition> getTestRunnersFunctions(final IASTFunctionDefinition mainFunc, final IProgressMonitor pm)
         throws CoreException {
      if (mainFunc == null) { return new ArrayList<>(); }

      final IIndex index = mainFunc.getTranslationUnit().getIndex();

      try {
         index.acquireReadLock();
         final List<IASTFunctionCallExpression> funcCalls = getFunctionCalls(mainFunc);
         final List<IASTFunctionDefinition> testRunners = new ArrayList<>();

         for (final IASTFunctionCallExpression callExpression : funcCalls) {
            if (callExpression.getFunctionNameExpression() instanceof IASTIdExpression) {
               final IASTIdExpression idExp = (IASTIdExpression) callExpression.getFunctionNameExpression();
               final IBinding binding = idExp.getName().resolveBinding();
               IASTName[] defs = mainFunc.getTranslationUnit().getDefinitionsInAST(binding);

               if (defs.length > 0) {
                  addTestRunnerFuncDef(testRunners, defs);
               } else {
                  final IIndexName[] indexDefs = index.findDefinitions(binding);
                  final IASTTranslationUnit ast = getAST(indexDefs[0], pm);

                  final Optional<IASTName> name = findName(ast, String.valueOf(indexDefs[0].getSimpleID()));
                  if (name.isPresent()) {
                     defs = ast.getDeclarationsInAST(name.get().resolveBinding());

                     if (defs.length > 0) {
                        addTestRunnerFuncDef(testRunners, defs);
                     }
                  }
               }
            }
         }

         return testRunners;
      } catch (final InterruptedException e) {
         Thread.currentThread().interrupt();
      } finally {
         index.releaseReadLock();
      }

      return new ArrayList<>();
   }

   private static Optional<IASTName> findName(final IASTTranslationUnit ast, final String nameToLookFor) {
      final NameFinder nameFinder = new NameFinder(ast);
      return nameFinder.getNameMatchingCriteria((name) -> nameToLookFor.equals(name.toString()));
   }

   private static void addTestRunnerFuncDef(final List<IASTFunctionDefinition> testRunners, final IASTName[] defs) {
      final IASTFunctionDefinition funcDef = getFunctionDefinition(defs[0]);

      if (isTestRunner(funcDef)) {
         testRunners.add(funcDef);
      }
   }

   private static boolean isTestRunner(final IASTFunctionDefinition funcDef) {
      final TestRunnerVisitor finder = new TestRunnerVisitor();
      funcDef.getBody().accept(finder);
      return finder.res;
   }

   private static List<IASTFunctionCallExpression> getFunctionCalls(final IASTFunctionDefinition mainFunc) {
      final List<IASTFunctionCallExpression> funCalls = new ArrayList<>();
      mainFunc.getBody().accept(new ASTVisitor() {

         {
            shouldVisitStatements = true;
         }

         @Override
         public int visit(final IASTStatement statement) {
            if (statement instanceof IASTExpressionStatement) {
               final IASTExpressionStatement expStmt = (IASTExpressionStatement) statement;

               if (expStmt.getExpression() instanceof IASTFunctionCallExpression) {
                  final IASTFunctionCallExpression funcCall = (IASTFunctionCallExpression) expStmt.getExpression();
                  funCalls.add(funcCall);
               }
            }

            return PROCESS_CONTINUE;
         }
      });
      return funCalls;
   }

   private IASTFunctionDefinition findMain(final SubMonitor m) throws CoreException {
      try {
         index.acquireReadLock();
         final IIndexBinding[] bindings = index.findBindings("main".toCharArray(), IndexFilter.ALL, new NullProgressMonitor());

         if (bindings.length > 0) {
            final IIndexName[] main = index.findDefinitions(bindings[0]);
            final IASTTranslationUnit ast = getAST(main[0], m);

            final Optional<IASTName> oMain = findDefinitionInTranslationUnit(ast, main[0]);
            if (oMain.isPresent()) { return getFunctionDefinition(oMain.get()); }
         }
      } catch (final InterruptedException e) {
         Thread.currentThread().interrupt();
      } finally {
         index.releaseReadLock();
      }

      return null;
   }

   private static Optional<IASTName> findDefinitionInTranslationUnit(final IASTTranslationUnit ast, final IIndexName iName) {
      final NodeContainer<IASTName> defName = new NodeContainer<>();
      ast.accept(new ASTVisitor() {

         {
            shouldVisitNames = true;
         }

         @Override
         public int visit(final IASTName name) {
            if (name.isDefinition() && name.getNodeLocations().length > 0) {
               final IASTNodeLocation nodeLocation = name.getNodeLocations()[0];

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

   private static boolean isSame(final IIndexName iName, final IASTNodeLocation nodeLocation) {
      final Path fileName = new Path(nodeLocation.asFileLocation().getFileName());
      return iName.getNodeOffset() == nodeLocation.getNodeOffset() && iName.getNodeLength() == nodeLocation.getNodeLength() && new Path(iName
            .getFileLocation().getFileName()).equals(fileName);
   }

   private static IASTFunctionDefinition getFunctionDefinition(final IASTName name) {
      return CPPVisitor.findAncestorWithType(name, IASTFunctionDefinition.class).orElse(null);
   }

   private IASTTranslationUnit getAST(final IIndexName iName, final IProgressMonitor pm) throws CoreException {
      final TranslationUnitLoader loader = new TranslationUnitLoader(cProject, index, pm);
      return loader.loadAst(iName);
   }

   private static final class TestRunnerVisitor extends ASTVisitor {

      private boolean res = false;

      {
         shouldVisitNames = true;
      }

      @Override
      public int visit(final IASTName name) {
         if (ASTUtil.isQualifiedName(name)) {
            final ICPPASTQualifiedName qName = (ICPPASTQualifiedName) name;

            if (qName.toString().equals(MAKE_RUNNER_FQ)) {
               res = true;
               return PROCESS_ABORT;
            }
         }

         return PROCESS_CONTINUE;
      }
   }
}
