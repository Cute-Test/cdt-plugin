package ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.refactorings;

import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;

import ch.hsr.ifs.cute.charwars.asttools.ASTAnalyzer;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.Context;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.transformers.ExpressionTransformer;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.transformers.Transformer;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.transformers.ExpressionTransformer.Transformation;

public class ExpressionRefactoring extends Refactoring {
	@Override
	public Transformer createTransformer(IASTIdExpression idExpression, Context context) {
		Transformer transformer = null;
		
		if(ASTAnalyzer.isStringLengthCalculation(idExpression)) {
			//sizeof(str) / sizeof(*str) - 1 -> str.size()
			//sizeof str / sizeof *str -1 -> str.size()
			IASTNode nodeToReplace = idExpression.getParent();
			while(!ASTAnalyzer.isSubtractionExpression(nodeToReplace)) {
				nodeToReplace = nodeToReplace.getParent();
			}
			transformer = new ExpressionTransformer(context, idExpression, nodeToReplace, Transformation.SIZE);
		}
		else if(ASTAnalyzer.isCheckedForEmptiness(idExpression, true)) {
			//!*str -> str.empty()
			//*str == 0 -> str.empty()
			//if modified: !*str -> !str[str_pos]
			transformer = new ExpressionTransformer(context, idExpression, ASTAnalyzer.getEnclosingBoolean(idExpression), Transformation.EMPTY);
		}
		else if(ASTAnalyzer.isCheckedForEmptiness(idExpression, false)) {
			//if(*str) -> if(!str.empty())
			//*str != 0 -> !str.empty()
			//if modified: *str -> str[str_pos]
			transformer = new ExpressionTransformer(context, idExpression, ASTAnalyzer.getEnclosingBoolean(idExpression), Transformation.NOT_EMPTY);
		}
		else if(ASTAnalyzer.isDereferencedToChar(idExpression)) {
			//*str -> str[0]
			//*(str) -> str[0]
			//*(str+n) -> str[n]
			//if modified: *str -> str[str_pos]
			IASTNode nodeToReplace = idExpression.getParent();
			while(!ASTAnalyzer.isDereferenceExpression(nodeToReplace)) {
				nodeToReplace = nodeToReplace.getParent();
			}
			transformer = new ExpressionTransformer(context, idExpression, nodeToReplace, Transformation.DEREFERENCED);
		}
		else if(ASTAnalyzer.modifiesCharPointer(idExpression)) {
			//++str -> ++str_pos
			//str++ -> str_pos++
			//str += n -> str_pos += n
			transformer = new ExpressionTransformer(context, idExpression, idExpression, Transformation.MODIFIED);
		}
		else if(ASTAnalyzer.isArraySubscriptExpression(idExpression) && context.isPotentiallyModifiedCharPointer(idExpression)) {
			//str[0] -> str[str_pos]
			//str[1] -> str[str_pos + 1]
			transformer = new ExpressionTransformer(context, idExpression, idExpression.getParent(), Transformation.ARRAY_SUBSCRIPTION);
		}

		return transformer;
	}
}