package ch.hsr.ifs.mockator.plugin.mockobject.registrations;

import static ch.hsr.ifs.mockator.plugin.MockatorConstants.PUSH_BACK;
import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;

import java.util.Collection;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFieldReference;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPNodeFactory;

import ch.hsr.ifs.mockator.plugin.base.util.StringUtil;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.refsupport.functions.FunctionSignatureFormatter;


// Adds the following statement to a function body for registering function calls:
//
// void foo1(int const& i) const {
// C++03:
// allCalls[mock_id].push_back(call("foo1(int const&) const", i));
// C++11:
// allCalls[mock_id].push_back(call{"foo1(int const&) const", i});
// }
//
// Note: If it is a static function, index 0 is used in the subscript.
@SuppressWarnings("restriction")
abstract class AbstractFunCallRegistrationAdder {

   protected static final CPPNodeFactory       nodeFactory = CPPNodeFactory.getDefault();
   private final String                        signature;
   private final ICPPASTParameterDeclaration[] funParameters;
   private final CppStandard                   cppStd;

   public AbstractFunCallRegistrationAdder(ICPPASTFunctionDeclarator function, CppStandard cppStd) {
      this.cppStd = cppStd;
      funParameters = function.getParameters();
      signature = new FunctionSignatureFormatter(function).getFunctionSignature();
   }

   public void addRegistrationTo(IASTCompoundStatement functionBody) {
      functionBody.addStatement(createFunCallRegistration());
   }

   protected IASTExpressionStatement createFunCallRegistration() {
      IASTName pushBack = nodeFactory.newName(PUSH_BACK.toCharArray());
      ICPPASTFieldReference fieldRef = nodeFactory.newFieldReference(pushBack, getPushBackOwner());
      Collection<IASTInitializerClause> args = createRegistrationArgs();
      IASTInitializerClause[] callParam = new IASTInitializerClause[] { cppStd.getCtorCall(getNameForCall(), args) };
      IASTFunctionCallExpression funExpr = nodeFactory.newFunctionCallExpression(fieldRef, callParam);
      return nodeFactory.newExpressionStatement(funExpr);
   }

   protected abstract String getNameForCall();

   protected abstract String getNameForCallsVector();

   protected abstract IASTExpression getPushBackOwner();

   protected IASTIdExpression createCallSequence() {
      return nodeFactory.newIdExpression(nodeFactory.newName(getNameForCallsVector().toCharArray()));
   }

   protected Collection<IASTInitializerClause> createRegistrationArgs() {
      List<IASTInitializerClause> params = list();
      params.add(createArgument(StringUtil.quote(signature)));

      for (ICPPASTParameterDeclaration param : funParameters) {
         IASTName funParamName = param.getDeclarator().getName();
         // e.g. operator ++(int)
         if (funParamName.toString().isEmpty()) {
            continue;
         }
         params.add(createArgument(funParamName.toString()));
      }
      return params;
   }

   private static IASTIdExpression createArgument(String arg) {
      return nodeFactory.newIdExpression(nodeFactory.newName(arg.toCharArray()));
   }
}
