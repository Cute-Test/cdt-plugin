package ch.hsr.ifs.mockator.plugin.refsupport.functions.params;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;
import static ch.hsr.ifs.mockator.plugin.base.maybe.Maybe.maybe;
import static ch.hsr.ifs.mockator.plugin.base.maybe.Maybe.none;

import java.util.Collection;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IType;

import ch.hsr.ifs.mockator.plugin.base.maybe.Maybe;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.TypeCreator;

public class FunArgumentsTypeCollector {
  private final Collection<IASTInitializerClause> funArgs;

  public FunArgumentsTypeCollector(Collection<IASTInitializerClause> funArgs) {
    this.funArgs = funArgs;
  }

  public List<IType> getFunArgTypes() {
    List<IType> argTypes = list();

    for (IASTInitializerClause arg : funArgs) {
      for (IType optType : getType(arg)) {
        argTypes.add(optType);
      }
    }

    return argTypes;
  }

  private static Maybe<IType> getType(IASTInitializerClause clause) {
    if (clause instanceof IASTExpression)
      return maybe(((IASTExpression) clause).getExpressionType());
    if (clause instanceof IASTTypeId)
      return maybe(TypeCreator.byDeclarator(((IASTTypeId) clause).getAbstractDeclarator()));
    if (clause instanceof IASTParameterDeclaration)
      return maybe(TypeCreator.byDeclarator(((IASTParameterDeclaration) clause).getDeclarator()));

    return none();
  }
}
