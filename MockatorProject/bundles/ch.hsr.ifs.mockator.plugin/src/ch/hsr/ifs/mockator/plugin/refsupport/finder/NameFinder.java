package ch.hsr.ifs.mockator.plugin.refsupport.finder;

import java.util.Optional;
import java.util.function.Function;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;

import ch.hsr.ifs.mockator.plugin.refsupport.utils.NodeContainer;


public class NameFinder {

   private final IASTNode startingNode;

   public NameFinder(final IASTNode startingNode) {
      this.startingNode = startingNode;
   }

   public Optional<IASTName> getNameMatchingCriteria(final Function<IASTName, Boolean> criteria) {
      final NodeContainer<IASTName> matchingName = new NodeContainer<>();
      startingNode.accept(new ASTVisitor() {

         {
            shouldVisitNames = true;
         }

         @Override
         public int visit(final IASTName name) {
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
