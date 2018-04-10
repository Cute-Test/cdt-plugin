/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.ui.project.wizard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTConditionalExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.ui.refactoring.Container;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubMonitor;


/**
 * @author Emanuel Graf IFS
 * @since 4.0
 *
 */

public class RunnerFinder {

   private final class NameFinder extends ASTVisitor {

      private final String string;
      {
         shouldVisitNames = true;
      }
      IASTName name = null;

      private NameFinder(String string) {
         this.string = string;
      }

      @Override
      public int visit(IASTName name) {
         if (string.equals(new String(name.getSimpleID()))) {
            this.name = name;
            return ASTVisitor.PROCESS_ABORT;
         }
         return super.visit(name);
      }
   }

   private final class TestRunnerVisitor extends ASTVisitor {

      private boolean res = false;
      {
         shouldVisitNames = true;
      }

      @Override
      public int visit(IASTName name) {
         if (name instanceof ICPPASTQualifiedName) {
            ICPPASTQualifiedName qName = (ICPPASTQualifiedName) name;
            if (qName.toString().equals("cute::makeRunner")) {
               res = true;
               return ASTVisitor.PROCESS_ABORT;
            }
         }
         return super.visit(name);
      }

   }

   private static final int AST_STYLE = ITranslationUnit.AST_CONFIGURE_USING_SOURCE_CONTEXT | ITranslationUnit.AST_SKIP_INDEXED_HEADERS;
   private IIndex           index;
   private final ICProject  project;

   public RunnerFinder(ICProject project) {
      this.project = project;
   }

   public IIndex getIndex() throws CoreException {
      if (index == null) {
         index = CCorePlugin.getIndexManager().getIndex(project);
      }
      return index;
   }

   public List<IASTFunctionDefinition> findTestRunners(IProgressMonitor monitor) throws CoreException {
      SubMonitor mon = SubMonitor.convert(monitor, 2);
      mon.beginTask(Messages.getString("RunnerFinder.findMain"), 1);
      IASTFunctionDefinition mainFunc = findMain();
      mon.beginTask(Messages.getString("RunnerFinder.findRunners"), 1);
      List<IASTFunctionDefinition> testRunnersFunctions = getTestRunnersFunctions(mainFunc);
      mon.done();
      return testRunnersFunctions;
   }

   private List<IASTFunctionDefinition> getTestRunnersFunctions(IASTFunctionDefinition mainFunc) throws CoreException {
      if (mainFunc == null) { return Collections.emptyList(); }
      IIndex index = mainFunc.getTranslationUnit().getIndex();

      try {
         index.acquireReadLock();
         List<IASTFunctionCallExpression> funcCalls = getFunctionCalls(mainFunc);
         List<IASTFunctionDefinition> testRunners = new ArrayList<>();
         for (IASTFunctionCallExpression callExpression : funcCalls) {
            if (callExpression.getFunctionNameExpression() instanceof IASTIdExpression) {
               IASTIdExpression idExp = (IASTIdExpression) callExpression.getFunctionNameExpression();
               IBinding bind = idExp.getName().resolveBinding();
               IASTName[] defs = mainFunc.getTranslationUnit().getDefinitionsInAST(bind);
               if (defs.length > 0) {
                  addTestRunnerFuncDef(testRunners, defs);
               } else {
                  IIndexName[] indexDefs = index.findDefinitions(bind);
                  IASTTranslationUnit ast = getAST(indexDefs[0]);
                  IASTName name = findName(ast, new String(indexDefs[0].getSimpleID()));
                  defs = ast.getDeclarationsInAST(name.resolveBinding());
                  if (defs.length > 0) {
                     addTestRunnerFuncDef(testRunners, defs);
                  }
               }
            }
         }

         return testRunners;
      } catch (InterruptedException e) {} finally {
         index.releaseReadLock();
      }
      return Collections.emptyList();
   }

   private IASTName findName(IASTTranslationUnit ast, final String string) {
      NameFinder vis = new NameFinder(string);
      ast.accept(vis);
      return vis.name;
   }

   protected void addTestRunnerFuncDef(List<IASTFunctionDefinition> testRunners, IASTName[] defs) {
      IASTFunctionDefinition funcDef = getFunctionDefinition(defs[0]);
      if (isTestRunner(funcDef)) {
         testRunners.add(funcDef);
      }
   }

   private boolean isTestRunner(IASTFunctionDefinition funcDef) {
      TestRunnerVisitor visitor = new TestRunnerVisitor();
      funcDef.getBody().accept(visitor);
      return visitor.res;
   }

   private List<IASTFunctionCallExpression> getFunctionCalls(IASTFunctionDefinition mainFunc) {
      final LinkedList<IASTFunctionCallExpression> res = new LinkedList<>();
      mainFunc.getBody().accept(new ASTVisitor() {

         {
            shouldVisitStatements = true;
         }

         @Override
         public int visit(IASTStatement statement) {
            if (statement instanceof IASTExpressionStatement) {
               IASTExpressionStatement expStmt = (IASTExpressionStatement) statement;
               if (expStmt.getExpression() instanceof IASTFunctionCallExpression) {
                  IASTFunctionCallExpression funcCall = (IASTFunctionCallExpression) expStmt.getExpression();
                  res.add(funcCall);
               }
            }
            if (statement instanceof IASTCompoundStatement) {
               IASTCompoundStatement compStmt = (IASTCompoundStatement) statement;
               if (compStmt.getStatements()[0] instanceof IASTReturnStatement) {
                  IASTReturnStatement retStmt = (IASTReturnStatement) compStmt.getStatements()[0];
                  if (retStmt.getReturnValue() instanceof IASTConditionalExpression) {
                     IASTConditionalExpression condExpr = (IASTConditionalExpression) retStmt.getReturnValue();
                     if (condExpr.getLogicalConditionExpression() instanceof IASTFunctionCallExpression) {
                        IASTFunctionCallExpression funcCall = (IASTFunctionCallExpression) condExpr.getLogicalConditionExpression();
                        res.add(funcCall);
                     }
                  }
               }
            }
            return super.visit(statement);
         }
      });
      return res;
   }

   protected IASTFunctionDefinition findMain() throws CoreException {
      IIndex index = getIndex();
      try {
         index.acquireReadLock();
         IIndexBinding[] bind = index.findBindings("main".toCharArray(), IndexFilter.ALL, new NullProgressMonitor());
         if (bind.length > 0) {

            IIndexName[] main = index.findDefinitions(bind[0]);

            IASTTranslationUnit ast = getAST(main[0]);
            IASTName mainASTname = findDefinitionInTranslationUnit(ast, main[0]);

            return getFunctionDefinition(mainASTname);
         }
      } catch (InterruptedException e) {} finally {
         index.releaseReadLock();
      }
      return null;
   }

   private IASTName findDefinitionInTranslationUnit(IASTTranslationUnit transUnit, final IIndexName indexName) {
      final Container<IASTName> defName = new Container<>();
      transUnit.accept(new ASTVisitor() {

         {
            shouldVisitNames = true;
         }

         @Override
         public int visit(IASTName name) {
            if (name.isDefinition() && name.getNodeLocations().length > 0) {
               IASTNodeLocation nodeLocation = name.getNodeLocations()[0];
               if (indexName.getNodeOffset() == nodeLocation.getNodeOffset() && indexName.getNodeLength() == nodeLocation.getNodeLength() && new Path(
                     indexName.getFileLocation().getFileName()).equals(new Path(nodeLocation.asFileLocation().getFileName()))) {
                  defName.setObject(name);
                  return ASTVisitor.PROCESS_ABORT;
               }
            }
            return ASTVisitor.PROCESS_CONTINUE;
         }

      });
      return defName.getObject();
   }

   protected IASTFunctionDefinition getFunctionDefinition(IASTName iastName) {
      IASTNode n = iastName.getParent();
      while (n != null && !(n instanceof IASTFunctionDefinition)) {
         n = n.getParent();
      }
      return (IASTFunctionDefinition) n;
   }

   protected IASTTranslationUnit getAST(IIndexName mainName) throws CoreException {
      IFile[] tmpFile = null;
      tmpFile = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(mainName.getFile().getLocation().getURI());
      ITranslationUnit tu = (ITranslationUnit) CCorePlugin.getDefault().getCoreModel().create(tmpFile[0]);
      IASTTranslationUnit ast = tu.getAST(getIndex(), AST_STYLE);
      return ast;
   }

}
