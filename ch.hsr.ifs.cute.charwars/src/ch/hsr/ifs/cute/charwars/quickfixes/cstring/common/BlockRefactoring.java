package ch.hsr.ifs.cute.charwars.quickfixes.cstring.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTNode.CopyStyle;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;

import ch.hsr.ifs.cute.charwars.asttools.ASTAnalyzer;
import ch.hsr.ifs.cute.charwars.asttools.ASTModifier;
import ch.hsr.ifs.cute.charwars.constants.StringType;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.refactorings.ASTChangeDescription;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.refactorings.Context;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.refactorings.Refactoring;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.refactorings.RefactoringFactory;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.refactorings.Context.ContextState;
import ch.hsr.ifs.cute.charwars.utils.ExtendedNodeFactory;
import ch.hsr.ifs.cute.charwars.utils.analyzers.BoolAnalyzer;

public class BlockRefactoring  {
	private BlockRefactoringConfiguration config;
	private HashSet<String> headersToInclude;
	private List<Refactoring> refactorings;
	
	public BlockRefactoring(BlockRefactoringConfiguration config) {
		this.config = config;
		this.headersToInclude = new HashSet<String>();
		this.refactorings = Arrays.asList(RefactoringFactory.createRefactorings());
	}
	
	public void refactorAllStatements() {
		StatementsVisitor visitor = new StatementsVisitor();
		config.getBlock().accept(visitor);
		IASTStatement allStatements[] = visitor.getStatements();
		
		Context context = prepareContext(allStatements);
		
		for(IASTStatement statement : allStatements) {
			if(!config.shouldSkipStatement(statement)) {
				refactorStatement(statement, context);	
			}
		}
	}
	
	private Context prepareContext(IASTStatement[] allStatements) {
		StringType stringType = config.getStringType();
		String strNameString = config.getStrName().toString();
		String varNameString = config.getVarName().toString();
		
		if(config.isAlias()) {
			return new Context(ContextState.CStringAlias, strNameString, config.getNewVarNameString(), null, stringType);
		}
		
		for(IASTStatement statement : allStatements) {
			if(modifiesCharPointer(statement)) {
				//find insertion point
				IASTStatement firstAffectedStatement = ASTAnalyzer.getTopLevelParentStatement(statement);
				if(firstAffectedStatement == null) break;

				String newVarNameString = varNameString + "_pos";
				config.setNewVarNameString(newVarNameString);
				IASTLiteralExpression zeroLiteral = ExtendedNodeFactory.newIntegerLiteral(0);
				
				IASTDeclarationStatement offsetVarDeclaration = ExtendedNodeFactory.newDeclarationStatement(stringType.getSizeType(), newVarNameString, zeroLiteral);
				ASTModifier.insertBefore(firstAffectedStatement.getParent(), firstAffectedStatement, offsetVarDeclaration, config.getASTRewrite());
				return new Context(ContextState.CStringModified, varNameString, newVarNameString, firstAffectedStatement, stringType);
			}
		}
		return new Context(ContextState.CString, varNameString, null, null, stringType);
	}
	
	private boolean modifiesCharPointer(IASTStatement statement) {
		List<IASTIdExpression> occurrences = new ArrayList<IASTIdExpression>(); 
		collectStringOccurrencesInSubtree(statement, occurrences);
		
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
		collectStringOccurrencesInSubtree(newStatement, stringOccurrences);
		sortStringOccurrences(stringOccurrences);
		
		ASTChangeDescription changeDescription = new ASTChangeDescription();
		refactorStringOccurrences(stringOccurrences, changeDescription, context);
		
		//workaround: copy statement again in order to remove original node locations
		newStatement = newStatement.copy();
		
		ASTRewrite rewrite = config.getASTRewrite();
		if(changeDescription.shouldRemoveStatement()) {
			rewrite.remove(oldStatement, null);
		}
		else if(changeDescription.statementHasChanged()) {
			IASTExpression oldCondition = BoolAnalyzer.getCondition(oldStatement);
			IASTExpression newCondition = BoolAnalyzer.getCondition(newStatement);
			
			if(oldCondition != null) {
				ASTModifier.replace(oldCondition, newCondition, rewrite);
			}
			else {
				ASTModifier.replace(oldStatement, newStatement, rewrite);
			}
		}
	}
	
	private void collectStringOccurrencesInSubtree(IASTNode subtree, List<IASTIdExpression> stringOccurrences) {
		if(subtree instanceof IASTIdExpression) {
			IASTIdExpression copiedIdExpression = (IASTIdExpression)subtree;
			IASTIdExpression originalIdExpression = (IASTIdExpression)copiedIdExpression.getOriginalNode();
			if(ASTAnalyzer.isSameName(originalIdExpression.getName(), config.getVarName())) {
				stringOccurrences.add(copiedIdExpression);
			}
		}
		else  {
			for(IASTNode child : subtree.getChildren()) {
				if(!(child instanceof IASTStatement)) {
					collectStringOccurrencesInSubtree(child, stringOccurrences);
				}
			}
		}
	}
	
	private void sortStringOccurrences(List<IASTIdExpression> stringOccurrences) {
		Collections.sort(stringOccurrences, new Comparator<IASTIdExpression>() {
			@Override
			public int compare(IASTIdExpression o1, IASTIdExpression o2) {
				return getDepth(o2) - getDepth(o1);
			}
		});
	}
	
	private void refactorStringOccurrences(List<IASTIdExpression> stringOccurrences, ASTChangeDescription changeDescription, Context context) {
		for(IASTIdExpression occurrence : stringOccurrences) {
			refactorStringOccurrence(occurrence, changeDescription, context);
		}
		headersToInclude.addAll(changeDescription.getHeadersToInclude());
	} 
	
	private void refactorStringOccurrence(IASTIdExpression stringOccurrence, ASTChangeDescription changeDescription, Context context) {
		for(Refactoring refactoring : this.refactorings) {
			boolean wasApplied = refactoring.tryToApply(stringOccurrence, context, changeDescription);
			if(wasApplied) {
				return;
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
