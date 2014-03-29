package ch.hsr.ifs.mockator.plugin.refsupport.finder;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;

import ch.hsr.ifs.mockator.plugin.base.functional.F1;
import ch.hsr.ifs.mockator.plugin.base.maybe.Maybe;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.NodeContainer;

public class NameFinder {
  private final IASTNode startingNode;

  public NameFinder(IASTNode startingNode) {
    this.startingNode = startingNode;
  }

  public Maybe<IASTName> getNameMatchingCriteria(final F1<IASTName, Boolean> criteria) {
    final NodeContainer<IASTName> matchingName = new NodeContainer<IASTName>();
    startingNode.accept(new ASTVisitor() {
      {
        shouldVisitNames = true;
      }

      @Override
      public int visit(IASTName name) {
        if (criteria.apply(name)) {
          matchingName.setNode(name);
          return PROCESS_ABORT;
        }

        return PROCESS_CONTINUE;
      }
    });
    return matchingName.getNode();
  }
}
