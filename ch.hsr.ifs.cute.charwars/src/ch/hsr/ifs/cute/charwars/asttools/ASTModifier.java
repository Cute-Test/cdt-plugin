package ch.hsr.ifs.cute.charwars.asttools;

import java.util.HashSet;

import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorObjectStyleMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

import ch.hsr.ifs.cute.charwars.constants.ErrorMessages;
import ch.hsr.ifs.cute.charwars.loggers.ErrorLogger;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressWarnings("restriction")
public class ASTModifier {
	public static void replaceNode(IASTNode oldNode, IASTNode newNode) {
		IASTAmbiguityParent parent = (IASTAmbiguityParent)oldNode.getParent();
		parent.replace(oldNode, newNode);
	}
	
	public static void includeHeaders(HashSet<String> headers, IASTTranslationUnit ast, IDocument document) {
		StringBuffer includeText = new StringBuffer();
		for(String headerName : headers) {
			if(!isHeaderAlreadyIncluded(headerName, ast)) {
				includeText.append("\n#include <" + headerName + ">");	
			}
		}

		if(includeText.length() > 0) {
			try {
				includeText.append("\n");
				int position = getPositionForIncludeStatements(ast);
				document.replace(position, 0, includeText.toString());
			} 
			catch(BadLocationException e) {
				ErrorLogger.log(ErrorMessages.UNABLE_TO_ADD_INCLUDE_DIRECTIVE, e);
				throw new RuntimeException(e);
			}
		}
	}
	
	private static boolean isHeaderAlreadyIncluded(String headerName, IASTTranslationUnit ast) {
		IASTPreprocessorIncludeStatement[] includeStatements = ast.getTranslationUnit().getIncludeDirectives();
		for(IASTPreprocessorIncludeStatement includeStatement : includeStatements) {
			if(includeStatement.getName().getRawSignature().equals(headerName) && includeStatement.isSystemInclude()) {
				return true;
			}
		}
		
		return false;
	}
	
	private static int getPositionForIncludeStatements(IASTTranslationUnit ast) {
		int position = 0;
		
		IASTPreprocessorIncludeStatement[] includeStatements = ast.getTranslationUnit().getIncludeDirectives();
		for(IASTPreprocessorIncludeStatement includeStatement : includeStatements) {
			if(includeStatement.isSystemInclude() && includeStatement.isPartOfTranslationUnitFile()) {
				IASTNodeLocation nodeLocation = includeStatement.getNodeLocations()[0];
				position = nodeLocation.getNodeOffset() + nodeLocation.getNodeLength();
			}
		}
		
		if(position == 0) {
			for(IASTPreprocessorStatement preprocessorStatement : ast.getAllPreprocessorStatements()) {
				if(preprocessorStatement instanceof IASTPreprocessorObjectStyleMacroDefinition) {
					IASTNodeLocation nodeLocation = preprocessorStatement.getNodeLocations()[0];
					position = nodeLocation.getNodeOffset() + nodeLocation.getNodeLength();
				}
			}
		}
		
		return position;
	}
	
	@SuppressFBWarnings(value="RV_RETURN_VALUE_IGNORED_INFERRED")
	public static void replace(IASTNode node1, IASTNode node2, ASTRewrite rewrite) {
		rewrite.replace(node1, node2, null);
	}
	
	public static void remove(IASTNode node, ASTRewrite rewrite) {
		rewrite.remove(node, null);
	}
	
	@SuppressFBWarnings(value="RV_RETURN_VALUE_IGNORED_INFERRED")
	public static void insertBefore(IASTNode parent, IASTNode insertionPoint, IASTNode node, ASTRewrite rewrite) {
		rewrite.insertBefore(parent, insertionPoint, node, null);
	}
	
	public static IASTNode transformToPointerOffset(IASTIdExpression idExpression) {
		IASTNode lastNode = idExpression;
		IASTNode currentNode = idExpression.getParent();
		while(ASTAnalyzer.isSubtractionExpression(currentNode) || ASTAnalyzer.isAdditionExpression(currentNode)) {
			lastNode = currentNode;
			currentNode = currentNode.getParent();
		}
		
		//return null to indicate, that there is no pointer offset
		if(lastNode == idExpression)
			return null;
		
		IASTBinaryExpression binaryExpression = (IASTBinaryExpression)idExpression.getParent();
		IASTExpression remainingOperand;
		if(ASTAnalyzer.isSubtractionExpression(binaryExpression) && idExpression == binaryExpression.getOperand1()) {
			remainingOperand = ExtendedNodeFactory.newNegatedExpression(binaryExpression.getOperand2());
		}
		else  {
			remainingOperand = (binaryExpression.getOperand1() == idExpression) ? binaryExpression.getOperand2() : binaryExpression.getOperand1();
		}
		
		ASTModifier.replaceNode(binaryExpression, remainingOperand);
		return (binaryExpression == lastNode) ? remainingOperand : lastNode;
	}
}