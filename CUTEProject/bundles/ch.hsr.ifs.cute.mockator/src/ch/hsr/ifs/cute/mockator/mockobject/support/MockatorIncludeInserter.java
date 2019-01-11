package ch.hsr.ifs.cute.mockator.mockobject.support;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;

import ch.hsr.ifs.cute.mockator.MockatorConstants;
import ch.hsr.ifs.cute.mockator.refsupport.includes.AstIncludeNode;


// Adds the include directive for the mockator header:
// #include "mockator.h"
public class MockatorIncludeInserter {

    private final IASTTranslationUnit ast;

    public MockatorIncludeInserter(final IASTTranslationUnit ast) {
        this.ast = ast;
    }

    public void insertWith(final ASTRewrite rewriter) {
        final AstIncludeNode include = new AstIncludeNode(MockatorConstants.MOCKATOR_HEADER);
        include.insertInTu(ast, rewriter);
    }
}
