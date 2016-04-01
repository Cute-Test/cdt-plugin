/*******************************************************************************
 * Copyright (c) 2010 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved.
 * 
 * Contributors:
 *     Institute for Software - initial API and implementation
 ******************************************************************************/
package ch.hsr.ifs.templator.test.testhelpertest;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionCallExpression;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDeclarator;
import org.junit.Test;

import ch.hsr.ifs.cdttesting.cdttest.CDTTestingTest;
import ch.hsr.ifs.templator.test.TestHelper;

public class FindTextOccuranceTest extends CDTTestingTest {

    @Test
    public void findTextOccuranceNullTest() throws Throwable {
        String source = fileMap.get(activeFileName).getSource();
        IASTTranslationUnit ast = TestHelper.parse(source, ParserLanguage.CPP);

        IASTNode occurance = TestHelper.findName(ast, 0, "foo");
        assertNotNull("Occurance 0 is null", occurance);
        assertEquals("foo", occurance.toString());

        occurance = TestHelper.findName(ast, 3, "foo");
        assertNotNull("Occurance 3 is null", occurance);
        assertEquals("foo", occurance.toString());

        occurance = TestHelper.findName(ast, 4, "foo");
        assertNull("Occurance 4 should be null", occurance);
    }

    @Test
    public void findTextOccuranceTypeTest() throws Throwable {
        String source = fileMap.get(activeFileName).getSource();
        IASTTranslationUnit ast = TestHelper.parse(source, ParserLanguage.CPP);

        IASTNode occurance = TestHelper.findName(ast, 0, "bar");
        assertTrue("Occurance 0 should be a declaration", occurance.getParent() instanceof CPPASTFunctionDeclarator);

        occurance = TestHelper.findName(ast, 1, "bar");
        assertTrue("Occurance 1 should be a definition",
                occurance.getParent().getParent() instanceof ICPPASTFunctionCallExpression);

    }
}
