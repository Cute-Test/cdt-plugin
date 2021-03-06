package ch.hsr.ifs.cute.mockator.mockobject.support.allcalls;

import java.util.Optional;

import org.eclipse.cdt.core.dom.ast.ASTNodeFactoryFactory;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNodeFactory;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;

import ch.hsr.ifs.iltis.cpp.core.wrappers.CPPVisitor;

import ch.hsr.ifs.cute.mockator.project.properties.CppStandard;
import ch.hsr.ifs.cute.mockator.testdouble.TestDoubleParentFinder;


// Inserts the following code before the mock object:
// static std::vector<calls> allCalls(1);
// Note that the vector is allocated with one element reserved for static function calls.
public class AllCallsVectorInserter {

    private static final ICPPNodeFactory        nodeFactory = ASTNodeFactoryFactory.getDefaultCPPNodeFactory();
    private final ICPPASTCompositeTypeSpecifier mockObject;
    private final CppStandard                   cppStd;
    private final Optional<IASTName>            allCallsVector;
    private final IASTNode                      parent;

    public AllCallsVectorInserter(final ICPPASTCompositeTypeSpecifier mockObject, final IASTNode parent, final Optional<IASTName> allCallsVector,
                                  final CppStandard cppStd) {
        this.mockObject = mockObject;
        this.parent = parent;
        this.allCallsVector = allCallsVector;
        this.cppStd = cppStd;
    }

    public void insert(final ASTRewrite rewriter) {
        if (!allCallsVector.isPresent()) {
            final IASTNode parent = new TestDoubleParentFinder(mockObject).getParentOfTestDouble();
            final AllCallsVectorCreator creator = new AllCallsVectorCreator(getNameOfAllCallsVector(), parent, cppStd);
            final IASTSimpleDeclaration allCallsVector = creator.getAllCallsVector();
            insertAllCallsVector(getAllCallsVector(allCallsVector, parent), parent, rewriter);
        }
    }

    private String getNameOfAllCallsVector() {
        final AllCallsVectorNameCreator creator = new AllCallsVectorNameCreator(mockObject, parent);
        return creator.getNameOfAllCallsVector();
    }

    private static IASTNode getAllCallsVector(final IASTSimpleDeclaration allCallsVector, final IASTNode parent) {
        if (isPartOfFunction(parent)) {
            return nodeFactory.newDeclarationStatement(allCallsVector);
        }

        return allCallsVector;
    }

    private void insertAllCallsVector(final IASTNode partToInsert, final IASTNode parent, final ASTRewrite rewrite) {
        final IASTNode insertionPoint = getInsertionPoint(parent);
        rewrite.insertBefore(parent, insertionPoint, partToInsert, null);
    }

    private IASTNode getInsertionPoint(final IASTNode parent) {
        if (isPartOfFunction(parent)) {
            // right before test double
            return CPPVisitor.findAncestorWithType(mockObject, IASTDeclarationStatement.class).orElse(null);
        } else {
            return CPPVisitor.findAncestorWithType(mockObject, IASTSimpleDeclaration.class).orElse(null);
        }
    }

    private static boolean isPartOfFunction(final IASTNode node) {
        return CPPVisitor.findAncestorWithType(node, ICPPASTFunctionDefinition.class).orElse(null) != null;
    }
}
