package ch.hsr.ifs.mockator.plugin.testdouble.movetons;

import java.util.Arrays;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNameSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDirective;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNodeFactory;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPNodeFactory;

import ch.hsr.ifs.mockator.plugin.refsupport.utils.NodeContainer;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.QualifiedNameCreator;


@SuppressWarnings("restriction")
public class TestDoubleUsingNsHandler {

   private static final ICPPNodeFactory        nodeFactory = CPPNodeFactory.getDefault();
   private final ICPPASTCompositeTypeSpecifier testDouble;
   private final ASTRewrite                    rewriter;

   public TestDoubleUsingNsHandler(final ICPPASTCompositeTypeSpecifier testDouble, final ASTRewrite rewriter) {
      this.testDouble = testDouble;
      this.rewriter = rewriter;
   }

   public void insertUsingNamespaceStmt(final ICPPASTFunctionDefinition testFunction) {
      final ICPPASTQualifiedName qNameForUsing = getQNameForUsingNs();

      if (hasUsingNamespaceStmt(testFunction, qNameForUsing)) {
         return;
      }

      final IASTDeclarationStatement usingStmt = createUsingNameSpaceStmt(qNameForUsing);
      final IASTNode firstPosInTestfun = getFirstPositionInFunBody(testFunction);
      rewriter.insertBefore(testFunction.getBody(), firstPosInTestfun, usingStmt, null);
   }

   private static IASTNode getFirstPositionInFunBody(final ICPPASTFunctionDefinition testFunction) {
      final IASTNode[] children = testFunction.getBody().getChildren();
      return children.length > 0 ? children[0] : null;
   }

   private ICPPASTQualifiedName getQNameForUsingNs() {
      final QualifiedNameCreator creator = new QualifiedNameCreator(testDouble.getName());
      final ICPPASTQualifiedName qualifiedName = creator.createQualifiedName();
      final ICPPASTNameSpecifier[] qualifiers = qualifiedName.getQualifier();
      final String[] qualifierNames = Arrays.stream(qualifiers).map(ICPPASTNameSpecifier::toString).toArray(size -> new String[size]);

      final ICPPASTQualifiedName qfNameForNs = nodeFactory.newQualifiedName(Arrays.copyOf(qualifierNames, qualifierNames.length - 1), last(qualifierNames));
      return qfNameForNs;
   }

   private <T> T last(final T[] array) {
      if (array.length == 0) { return null; }
      return array[array.length - 1];
   }

   private static boolean hasUsingNamespaceStmt(final ICPPASTFunctionDefinition testFun, final ICPPASTQualifiedName qNameForUsing) {
      final NodeContainer<ICPPASTUsingDirective> usingDirective = new NodeContainer<>();
      testFun.accept(new ASTVisitor() {

         {
            shouldVisitDeclarations = true;
         }

         @Override
         public int visit(final IASTDeclaration decl) {
            if (!(decl instanceof ICPPASTUsingDirective)) {
               return PROCESS_CONTINUE;
            }

            final ICPPASTUsingDirective using = (ICPPASTUsingDirective) decl;

            if (using.getQualifiedName().toString().equals(qNameForUsing.toString())) {
               usingDirective.setNode(using);
               return PROCESS_ABORT;
            }

            return PROCESS_CONTINUE;
         }
      });
      return usingDirective.getNode().isPresent();
   }

   private static IASTDeclarationStatement createUsingNameSpaceStmt(final ICPPASTQualifiedName qNameForUsing) {
      final ICPPASTUsingDirective usingDirective = nodeFactory.newUsingDirective(qNameForUsing);
      return nodeFactory.newDeclarationStatement(usingDirective);
   }
}
