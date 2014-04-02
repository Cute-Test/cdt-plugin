/******************************************************************************
 * Copyright (c) 2014 Institute for Software, HSR Hochschule fuer Technik 
 * Rapperswil, University of applied sciences and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 *
 * Contributors:
 * 	Thomas Corbat - initial API and implementation
 ******************************************************************************/
package ch.hsr.ifs.cute.macronator.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ch.hsr.ifs.cute.macronator.test.checker.FunctionLikeCheckerNegativeMatchesTest;
import ch.hsr.ifs.cute.macronator.test.checker.FunctionLikeCheckerPositiveMatchesTest;
import ch.hsr.ifs.cute.macronator.test.checker.ObjectLikeCheckerNegativeMatchesTest;
import ch.hsr.ifs.cute.macronator.test.checker.ObjectLikeCheckerPositiveMatchesTest;
import ch.hsr.ifs.cute.macronator.test.checker.UnusedMacroCheckerNegativeMatchesTest;
import ch.hsr.ifs.cute.macronator.test.checker.UnusedMacroCheckerPositiveMatchesTest;
import ch.hsr.ifs.cute.macronator.test.common.LexerAdapterTest;
import ch.hsr.ifs.cute.macronator.test.common.LocalExpansionTest;
import ch.hsr.ifs.cute.macronator.test.common.ParserAdapterTest;
import ch.hsr.ifs.cute.macronator.test.common.SuppressedMacrosTest;
import ch.hsr.ifs.cute.macronator.test.quickfix.FunctionLikeQuickfixTest;
import ch.hsr.ifs.cute.macronator.test.quickfix.ObjectLikeQuickfixTest;
import ch.hsr.ifs.cute.macronator.test.quickfix.UnusedMacroQuickfixTest;
import ch.hsr.ifs.cute.macronator.test.refactoring.ExpandMacroRefactoringTest;
import ch.hsr.ifs.cute.macronator.test.transform.AutoFunctionTransformationTest;
import ch.hsr.ifs.cute.macronator.test.transform.BuiltinMacroTest;
import ch.hsr.ifs.cute.macronator.test.transform.ConstexprTransformationTest;
import ch.hsr.ifs.cute.macronator.test.transform.DeclarationTransformationTest;
import ch.hsr.ifs.cute.macronator.test.transform.VoidFunctionTransformationTest;



@RunWith(Suite.class)
@SuiteClasses({
//@formatter:off
	FunctionLikeCheckerNegativeMatchesTest.class, 
	FunctionLikeCheckerPositiveMatchesTest.class,
	ObjectLikeCheckerNegativeMatchesTest.class,
	ObjectLikeCheckerPositiveMatchesTest.class,
	UnusedMacroCheckerNegativeMatchesTest.class,
	UnusedMacroCheckerPositiveMatchesTest.class,
	LexerAdapterTest.class,
	LocalExpansionTest.class,
	ParserAdapterTest.class,
	SuppressedMacrosTest.class,
	FunctionLikeQuickfixTest.class,
	ObjectLikeQuickfixTest.class,
	UnusedMacroQuickfixTest.class,
	ExpandMacroRefactoringTest.class,
	AutoFunctionTransformationTest.class,
	BuiltinMacroTest.class,
	ConstexprTransformationTest.class,
	DeclarationTransformationTest.class,
	VoidFunctionTransformationTest.class
//@formatter:on
})
public class AllMacronatorTests {
}
