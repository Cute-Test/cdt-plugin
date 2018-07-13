package ch.hsr.ifs.cute.mockator.testdouble.creation.subtype;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTInitializerList;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ICProject;

import ch.hsr.ifs.iltis.cpp.core.wrappers.CPPVisitor;


class InjectionInfoCollectorFactory {

   private final IIndex    index;
   private final ICProject cProject;

   public InjectionInfoCollectorFactory(final IIndex index, final ICProject cProject) {
      this.index = index;
      this.cProject = cProject;
   }

   public DepInjectInfoCollector getInfoCollectorStrategy(final IASTName name) {
      if (isPartOfCtorCall(name)) return new CtorInjectionInfoCollector(index, cProject);
      else return new FunCallInjectionInfoCollector(index, cProject);
   }

   private static boolean isPartOfCtorCall(final IASTNode node) {
      return CPPVisitor.findAncestorWithType(node, ICPPASTConstructorInitializer.class).orElse(null) != null || CPPVisitor.findAncestorWithType(node,
            ICPPASTInitializerList.class).orElse(null) != null;
   }
}
