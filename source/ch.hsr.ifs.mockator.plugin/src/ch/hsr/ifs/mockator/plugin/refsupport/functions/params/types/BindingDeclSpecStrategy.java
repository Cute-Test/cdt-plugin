package ch.hsr.ifs.mockator.plugin.refsupport.functions.params.types;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPNodeFactory;
import org.eclipse.core.runtime.CoreException;

import ch.hsr.ifs.mockator.plugin.base.MockatorException;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.TypedefHelper;

@SuppressWarnings("restriction")
class BindingDeclSpecStrategy implements DeclSpecGeneratorStrategy {
  private static final CPPNodeFactory nodeFactory = CPPNodeFactory.getDefault();

  @Override
  public ICPPASTDeclSpecifier createDeclSpec(IType type) {
    String shortestType = getShortestType(type);
    IASTName newName = nodeFactory.newName(shortestType.toCharArray());
    return nodeFactory.newTypedefNameSpecifier(newName);
  }

  private static String getShortestType(IType type) {
    try {
      return new TypedefHelper(type).findShortestType();
    } catch (CoreException e) {
      throw new MockatorException(e);
    }
  }
}
