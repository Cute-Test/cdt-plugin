package ch.hsr.ifs.cute.elevator.ast.analysis;

import static ch.hsr.ifs.cute.elevator.ast.analysis.conditions.Condition.not;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;

import ch.hsr.ifs.cute.elevator.ast.analysis.conditions.Condition;
import ch.hsr.ifs.cute.elevator.ast.analysis.conditions.ContainsPackExpansion;
import ch.hsr.ifs.cute.elevator.ast.analysis.conditions.HasInitializerListConstructor;
import ch.hsr.ifs.cute.elevator.ast.analysis.conditions.HasNarrowingTypeConversion;
import ch.hsr.ifs.cute.elevator.ast.analysis.conditions.IsElevated;
import ch.hsr.ifs.cute.elevator.ast.analysis.conditions.IsElevatedNewExpression;
import ch.hsr.ifs.cute.elevator.ast.analysis.conditions.IsInstanceOf;
import ch.hsr.ifs.cute.elevator.ast.analysis.conditions.IsUninitializedReference;
import ch.hsr.ifs.cute.elevator.ast.analysis.conditions.OtherDelaratorElevationConditions;

public class DeclaratorCollector extends ASTVisitor {
    
    final private List<IASTDeclarator> declarators;
    
    private final Condition isAlreadyElevated = new IsInstanceOf(IASTDeclarator.class).and(new IsElevated().or(new IsElevatedNewExpression()));
    private final Condition hasNarrowingTypeConversion = new HasNarrowingTypeConversion();
    private final Condition hasInitializerListConstructor = new HasInitializerListConstructor();
    private final Condition containsPackExpansion = new ContainsPackExpansion();
    private final Condition isElevationCandidate = not(
            isAlreadyElevated
            .or(hasNarrowingTypeConversion)
            .or(hasInitializerListConstructor)
            .or(containsPackExpansion)
            .or(new IsUninitializedReference())
            ).and(new OtherDelaratorElevationConditions());
    
    public DeclaratorCollector() {
        declarators = new ArrayList<IASTDeclarator>();
        this.shouldVisitDeclarators = true;
    }
    
    @Override
    public int visit(IASTDeclarator declarator) {
        if (isFunctionDeclarator(declarator)) {
            return PROCESS_SKIP;
        }
        if (isElevationCandidate.satifies(declarator)) {
            declarators.add(declarator);
        }
        return PROCESS_CONTINUE;
    }
    
    public List<IASTDeclarator> getDeclarators() {
        return declarators;
    }
  
    private boolean isFunctionDeclarator(IASTDeclarator element) {
        return element instanceof IASTFunctionDeclarator;
    }
}
