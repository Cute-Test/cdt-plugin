package ch.hsr.ifs.mockator.plugin.mockobject.support.allcalls;

import static ch.hsr.ifs.mockator.plugin.MockatorConstants.CALLS;
import static org.eclipse.cdt.core.dom.ast.IASTLiteralExpression.lk_integer_constant;

import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTypeId;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPNodeFactory;

import ch.hsr.ifs.mockator.plugin.base.MockatorException;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;


@SuppressWarnings("restriction")
public class AllCallsVectorCreator {

   private static final CPPNodeFactory nodeFactory = CPPNodeFactory.getDefault();
   private static final String         STD         = "std";
   private static final String         VECTOR      = "vector";
   private final CallsVectorParent     callsVectorParent;
   private final String                nameOfAllCallsVector;
   private final CppStandard           cppStd;

   public enum CallsVectorParent {
      Function {

      @Override
      int getStorageClassForCallsVector() {
         // C++0x standard 9.8: Declarations in a local class can
         // use only type names, static variables, extern variables
         // and functions, and enumerators from the enclosing scope.
         return IASTDeclSpecifier.sc_static;
      }
      },
      Namespace {

      @Override
      int getStorageClassForCallsVector() {
         return IASTDeclSpecifier.sc_unspecified;
      }
      };

      abstract int getStorageClassForCallsVector();

      public static CallsVectorParent fromAstNode(IASTNode node) {
         if (node instanceof ICPPASTNamespaceDefinition) return Namespace;
         else if (node instanceof IASTCompoundStatement) return Function;

         throw new MockatorException("Unexpected test double parent");
      }
   }

   public AllCallsVectorCreator(String nameOfAllCallsVector, IASTNode callsVectorParent, CppStandard cppStd) {
      this(nameOfAllCallsVector, CallsVectorParent.fromAstNode(callsVectorParent), cppStd);
   }

   public AllCallsVectorCreator(String nameOfAllCallsVector, CallsVectorParent callsVectorParent, CppStandard cppStd) {
      this.nameOfAllCallsVector = nameOfAllCallsVector;
      this.callsVectorParent = callsVectorParent;
      this.cppStd = cppStd;
   }

   public IASTSimpleDeclaration getAllCallsVector() {
      ICPPASTTemplateId stdVectorTemplateId = createStdVectorOfCalls();
      return createDeclarator(stdVectorTemplateId);
   }

   private IASTSimpleDeclaration createDeclarator(ICPPASTTemplateId stdVectorTemplateId) {
      ICPPASTNamedTypeSpecifier declSpecifier = nodeFactory.newTypedefNameSpecifier(stdVectorTemplateId);
      IASTSimpleDeclaration result = nodeFactory.newSimpleDeclaration(declSpecifier);
      IASTName typeDefName = nodeFactory.newName(nameOfAllCallsVector.toCharArray());
      ICPPASTLiteralExpression literal1 = nodeFactory.newLiteralExpression(lk_integer_constant, "1");
      ICPPASTDeclarator declarator = new CPPASTDeclarator(typeDefName, cppStd.getInitializer(literal1));
      result.addDeclarator(declarator);
      declSpecifier.setStorageClass(callsVectorParent.getStorageClassForCallsVector());
      return result;
   }

   private static ICPPASTTemplateId createStdVectorOfCalls() {
      ICPPASTQualifiedName allCalls = nodeFactory.newQualifiedName();
      allCalls.addName(nodeFactory.newName(STD.toCharArray()));
      allCalls.addName(nodeFactory.newName(VECTOR.toCharArray()));
      ICPPASTNamedTypeSpecifier calls = nodeFactory.newTypedefNameSpecifier(nodeFactory.newName(CALLS.toCharArray()));
      ICPPASTTemplateId stdVectorOfCalls = nodeFactory.newTemplateId(nodeFactory.newName(allCalls.toCharArray()));
      ICPPASTTypeId id = nodeFactory.newTypeId(calls, null);
      stdVectorOfCalls.addTemplateArgument(id);
      return stdVectorOfCalls;
   }
}
