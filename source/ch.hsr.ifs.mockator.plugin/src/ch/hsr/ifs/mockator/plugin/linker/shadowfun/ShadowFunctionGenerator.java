package ch.hsr.ifs.mockator.plugin.linker.shadowfun;

import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPNodeFactory;

import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.refsupport.functions.params.ParameterNameFunDecorator;
import ch.hsr.ifs.mockator.plugin.refsupport.functions.returntypes.ReturnStatementCreator;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.QualifiedNameCreator;

@SuppressWarnings("restriction")
public class ShadowFunctionGenerator {
  private static final CPPNodeFactory nodeFactory = CPPNodeFactory.getDefault();
  private final CppStandard cppStd;

  public ShadowFunctionGenerator(CppStandard cppStd) {
    this.cppStd = cppStd;
  }

  public ICPPASTFunctionDefinition createShadowedFunction(ICPPASTFunctionDeclarator funDecl,
      IASTCompoundStatement newBody) {
    ICPPASTDeclSpecifier newDeclSpec = AstUtil.getDeclSpec(funDecl).copy();
    AstUtil.removeExternalStorageIfSet(newDeclSpec);
    ICPPASTFunctionDeclarator newFunDecl = funDecl.copy();
    adjustParamNamesIfNecessary(newFunDecl);
    newFunDecl.setName(createFullyQualifiedName(funDecl));
    ReturnStatementCreator creator = new ReturnStatementCreator(cppStd);
    newBody.addStatement(creator.createReturnStatement(funDecl, newDeclSpec));
    return nodeFactory.newFunctionDefinition(newDeclSpec, newFunDecl, newBody);
  }

  private static IASTName createFullyQualifiedName(ICPPASTFunctionDeclarator funDecl) {
    QualifiedNameCreator resolver = new QualifiedNameCreator(funDecl.getName());
    ICPPASTQualifiedName qualifiedName = resolver.createQualifiedName();
    qualifiedName.addName(funDecl.getName().copy());
    return qualifiedName;
  }

  private static void adjustParamNamesIfNecessary(ICPPASTFunctionDeclarator newFunDecl) {
    ParameterNameFunDecorator funDecorator = new ParameterNameFunDecorator(newFunDecl);
    funDecorator.adjustParamNamesIfNecessary();
  }
}
