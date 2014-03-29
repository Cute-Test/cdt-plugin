package ch.hsr.ifs.mockator.plugin.testdouble.creation.subtype;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTInitializerList;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ICProject;

import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;

class InjectionInfoCollectorFactory {
  private final IIndex index;
  private final ICProject cProject;

  public InjectionInfoCollectorFactory(IIndex index, ICProject cProject) {
    this.index = index;
    this.cProject = cProject;
  }

  public DepInjectInfoCollector getInfoCollectorStrategy(IASTName name) {
    if (isPartOfCtorCall(name))
      return new CtorInjectionInfoCollector(index, cProject);
    else
      return new FunCallInjectionInfoCollector(index, cProject);
  }

  private static boolean isPartOfCtorCall(IASTNode node) {
    return AstUtil.getAncestorOfType(node, ICPPASTConstructorInitializer.class) != null
        || AstUtil.getAncestorOfType(node, ICPPASTInitializerList.class) != null;
  }
}
