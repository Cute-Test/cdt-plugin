package ch.hsr.ifs.cute.charwars.quickfixes.cstring.common;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTDoStatement;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTNode.CopyStyle;
import org.eclipse.cdt.core.dom.ast.IASTWhileStatement;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;

import ch.hsr.ifs.cute.charwars.asttools.ASTAnalyzer;
import ch.hsr.ifs.cute.charwars.asttools.ASTModifier;
import ch.hsr.ifs.cute.charwars.asttools.ExtendedNodeFactory;
import ch.hsr.ifs.cute.charwars.constants.StdString;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.Context.ContextState;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.mappings.Mapping;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.mappings.MappingFactory;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.refactorings.CStringConversionRefactoring;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.refactorings.ComparisonRefactoring;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.refactorings.DefaultRefactoring;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.refactorings.ExpressionRefactoring;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.refactorings.FunctionRefactoring;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.refactorings.NullRefactoring;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.refactorings.OperatorRefactoring;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.refactorings.Refactoring;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.refactorings.RemoveStatementRefactoring;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.transformers.Transformer;

public class BlockRefactoring  {
	private ASTRewrite rewrite;
	private List<Refactoring> refactorings;
	private String stringName;
	private IASTName variableToRefactor;
	private IASTNode block;
	private IASTDeclarationStatement declarationStatement;
	private boolean isAlias;
	private HashSet<String> headersToInclude;
	
	public BlockRefactoring(ASTRewrite rewrite, String stringName, IASTName variableToRefactor, IASTNode block, IASTDeclarationStatement declarationStatement, boolean isAlias) {
		this.rewrite = rewrite;
		this.stringName = stringName;
		this.variableToRefactor = variableToRefactor;
		this.block = block;
		this.declarationStatement = declarationStatement;
		this.isAlias = isAlias;
		this.headersToInclude = new HashSet<String>();
		
		this.refactorings = new ArrayList<Refactoring>();
		this.refactorings.add(new ExpressionRefactoring());
		for(Mapping m : MappingFactory.createOperatorRefactoringMappings()) this.refactorings.add(new OperatorRefactoring(m));
		for(Mapping m : MappingFactory.createComparisonRefactoringMappings()) this.refactorings.add(new ComparisonRefactoring(m));
		for(Mapping m : MappingFactory.createFunctionRefactoringMappings()) this.refactorings.add(new FunctionRefactoring(m));
		for(Mapping m : MappingFactory.createCStringConversionRefactoringMappings()) this.refactorings.add(new CStringConversionRefactoring(m));
		for(Mapping m : MappingFactory.createRemoveStatementRefactoringMappings()) this.refactorings.add(new RemoveStatementRefactoring(m));
		this.refactorings.add(new NullRefactoring());
		this.refactorings.add(new DefaultRefactoring());
	}
	
	public void refactorAllStatements() {
		StatementsVisitor visitor = new StatementsVisitor();
		block.accept(visitor);
		IASTStatement allStatements[] = visitor.getStatements();
		
		Context context = prepareContext(allStatements);
		
		for(IASTStatement statement : allStatements) {
			if(statement != declarationStatement) {
				refactorStatement(statement, context);
			}
		}
	}
	
	private Context prepareContext(IASTStatement[] allStatements) {
		if(isAlias) {
			return new Context(ContextState.CStringAlias, stringName, variableToRefactor.toString(), null);
		}
		
		for(IASTStatement statement : allStatements) {
			if(modifiesCharPointer(statement)) {
				//find insertion point
				IASTStatement firstAffectedStatement = ASTAnalyzer.getTopLevelParentStatement(statement);
				if(firstAffectedStatement == null) break;

				String offsetVarName = this.variableToRefactor.toString() + "_pos";
				IASTLiteralExpression zeroLiteral = ExtendedNodeFactory.newIntegerLiteral(0);
				IASTDeclarationStatement offsetVarDeclaration = ExtendedNodeFactory.newDeclarationStatement(StdString.SIZE_TYPE, offsetVarName, zeroLiteral);
				ASTModifier.insertBefore(firstAffectedStatement.getParent(), firstAffectedStatement, offsetVarDeclaration, rewrite);
				return new Context(ContextState.CStringModified, variableToRefactor.toString(), offsetVarName, firstAffectedStatement);
			}
		}
		return new Context(ContextState.CString, variableToRefactor.toString(), null, null);
	}
	
	private boolean modifiesCharPointer(IASTStatement statement) {
		List<IASTIdExpression> occurrences = new ArrayList<IASTIdExpression>(); 
		findStringOccurrencesInSubtree(statement, occurrences);
		
		for(IASTIdExpression occurrence : occurrences) {
			if(ASTAnalyzer.modifiesCharPointer(occurrence)) {
				return true;
			}
		}
		return false;
	}
	
	private void refactorStatement(IASTStatement statement, Context context) {
		IASTStatement oldStatement = statement;
		IASTStatement newStatement = statement.copy(CopyStyle.withLocations);
		
		List<IASTIdExpression> stringOccurrences = new ArrayList<IASTIdExpression>();
		findStringOccurrencesInSubtree(newStatement, stringOccurrences);
		
		ASTChangeDescription changeDescription = new ASTChangeDescription();
		refactorStringOccurrences(stringOccurrences, changeDescription, context);
		
		//workaround: copy statement again in order to remove original node locations
		newStatement = newStatement.copy();
		
		if(changeDescription.shouldRemoveStatement()) {
			rewrite.remove(oldStatement, null);
		}
		else if(changeDescription.statementHasChanged()) {
			if(oldStatement instanceof IASTIfStatement) {
				IASTIfStatement oldIfStatement = (IASTIfStatement)oldStatement;
				IASTIfStatement newIfStatement = (IASTIfStatement)newStatement;
				ASTModifier.replace(oldIfStatement.getConditionExpression(), newIfStatement.getConditionExpression(), rewrite);
			}
			else if(oldStatement instanceof IASTWhileStatement) {
				IASTWhileStatement oldWhileStatement = (IASTWhileStatement)oldStatement;
				IASTWhileStatement newWhileStatement = (IASTWhileStatement)newStatement;
				ASTModifier.replace(oldWhileStatement.getCondition(), newWhileStatement.getCondition(), rewrite);
			}
			else if(oldStatement instanceof IASTForStatement) {
				IASTForStatement oldForStatement = (IASTForStatement)oldStatement;
				IASTForStatement newForStatement = (IASTForStatement)newStatement;
				ASTModifier.replace(oldForStatement.getConditionExpression(), newForStatement.getConditionExpression(), rewrite);
			}
			else if(oldStatement instanceof IASTDoStatement) {
				IASTDoStatement oldDoStatement = (IASTDoStatement)oldStatement;
				IASTDoStatement newDoStatement = (IASTDoStatement)newStatement;
				ASTModifier.replace(oldDoStatement.getCondition(), newDoStatement.getCondition(), rewrite);
			}
			else {
				ASTModifier.replace(oldStatement, newStatement, rewrite);
			}
		}
	}
	
	private void findStringOccurrencesInSubtree(IASTNode subtree, List<IASTIdExpression> stringOccurrences) {
		if(subtree instanceof IASTIdExpression) {
			IASTIdExpression copiedIdExpression = (IASTIdExpression)subtree;
			IASTIdExpression originalIdExpression = (IASTIdExpression)copiedIdExpression.getOriginalNode();
			IBinding originalBinding = originalIdExpression.getName().resolveBinding();
			
			if(originalBinding.equals(variableToRefactor.resolveBinding())) {
				stringOccurrences.add(copiedIdExpression);
			}
		}
		else  {
			for(IASTNode child : subtree.getChildren()) {
				if(!(child instanceof IASTStatement)) {
					findStringOccurrencesInSubtree(child, stringOccurrences);
				}
			}
		}
	}
	
	private void refactorStringOccurrences(List<IASTIdExpression> stringOccurrences, ASTChangeDescription changeDescription, Context context) {
		List<IASTIdExpression> occurrences = new ArrayList<IASTIdExpression>(stringOccurrences);
		while(!occurrences.isEmpty()) {
			IASTIdExpression deepestNode = occurrences.get(0);
			for(IASTIdExpression occurrence : occurrences) {
				if(getDepth(occurrence) > getDepth(deepestNode)) {
					deepestNode = occurrence;
				}
			}
			occurrences.remove(deepestNode);
			refactorStringOccurrence(deepestNode, changeDescription, context);
		}
		headersToInclude.addAll(changeDescription.getHeadersToInclude());
	} 
	
	private void refactorStringOccurrence(IASTIdExpression stringOccurrence, ASTChangeDescription changeDescription, Context context) {
		for(Refactoring refactoring : refactorings) {
			Transformer transformer = refactoring.createTransformer(stringOccurrence, context);
			if(transformer != null) {
				System.out.println("Applying transformer " + transformer.getClass() + " from refactoring " + refactoring.getClass());
				transformer.transform(changeDescription);
				break;
			}
		}
	}
	
	private int getDepth(IASTNode node) {
		int depth = 0;
		for(IASTNode parent = node; parent != null && !(parent instanceof IASTStatement); parent = parent.getParent()) {
			++depth;
		}
		return depth;
	}

	public HashSet<String> getHeadersToInclude() {
		return headersToInclude;
	}
}
