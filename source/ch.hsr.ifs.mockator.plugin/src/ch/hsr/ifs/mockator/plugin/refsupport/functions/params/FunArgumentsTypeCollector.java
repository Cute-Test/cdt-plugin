package ch.hsr.ifs.mockator.plugin.refsupport.functions.params;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IType;

import ch.hsr.ifs.mockator.plugin.refsupport.utils.TypeCreator;


public class FunArgumentsTypeCollector {

   private final Collection<IASTInitializerClause> funArgs;

   public FunArgumentsTypeCollector(final Collection<IASTInitializerClause> funArgs) {
      this.funArgs = funArgs;
   }

   public List<IType> getFunArgTypes() {
      final List<IType> argTypes = list();

      for (final IASTInitializerClause arg : funArgs) {
         getType(arg).ifPresent((type) -> argTypes.add(type));
      }

      return argTypes;
   }

   private static Optional<IType> getType(final IASTInitializerClause clause) {
      if (clause instanceof IASTExpression) {
         return Optional.of(((IASTExpression) clause).getExpressionType());
      }
      if (clause instanceof IASTTypeId) {
         return Optional.of(TypeCreator.byDeclarator(((IASTTypeId) clause).getAbstractDeclarator()));
      }
      if (clause instanceof IASTParameterDeclaration) {
         return Optional.of(TypeCreator.byDeclarator(((IASTParameterDeclaration) clause).getDeclarator()));
      }

      return Optional.empty();
   }
}
