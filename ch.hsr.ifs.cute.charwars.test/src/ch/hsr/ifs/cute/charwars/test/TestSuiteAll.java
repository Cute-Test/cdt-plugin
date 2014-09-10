package ch.hsr.ifs.cute.charwars.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ch.hsr.ifs.cute.charwars.checkers.ArrayCheckerTest;
import ch.hsr.ifs.cute.charwars.checkers.CStrCheckerTest;
import ch.hsr.ifs.cute.charwars.checkers.CStringAliasCheckerTest;
import ch.hsr.ifs.cute.charwars.checkers.CStringCheckerTest;
import ch.hsr.ifs.cute.charwars.checkers.CStringCleanupCheckerTest;
import ch.hsr.ifs.cute.charwars.checkers.CStringParameterCheckerTest;
import ch.hsr.ifs.cute.charwars.checkers.PointerParameterCheckerTest;
import ch.hsr.ifs.cute.charwars.quickfixes.ArrayQuickFixTest;
import ch.hsr.ifs.cute.charwars.quickfixes.CStrQuickFixTest;
import ch.hsr.ifs.cute.charwars.quickfixes.PointerParameterQuickFixTest;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.cleanup.CStringCleanupQuickFixTest;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.general.CStringQuickFixTest;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.general.refactorings.ConvertingFunctionTest;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.general.refactorings.MemchrTest;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.general.refactorings.MemcmpTest;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.general.refactorings.MemcpyTest;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.general.refactorings.MemmoveTest;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.general.refactorings.StrcatTest;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.general.refactorings.StrchrTest;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.general.refactorings.StrcmpTest;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.general.refactorings.StrcpyTest;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.general.refactorings.StrcspnTest;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.general.refactorings.StrdupTest;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.general.refactorings.StrlenTest;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.general.refactorings.StrncatTest;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.general.refactorings.StrncmpTest;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.general.refactorings.StrncpyTest;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.general.refactorings.StrpbrkTest;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.general.refactorings.StrrchrTest;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.general.refactorings.StrspnTest;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.general.refactorings.StrstrTest;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.parameter.CStringParameterQuickFixTest;

@RunWith(Suite.class)
@SuiteClasses({
//@formatter:off
	ArrayCheckerTest.class,
	CStringCheckerTest.class,
	CStringAliasCheckerTest.class,
	CStringCleanupCheckerTest.class,
	CStrCheckerTest.class,
	PointerParameterCheckerTest.class,
	CStringParameterCheckerTest.class,
	ArrayQuickFixTest.class,
	CStringQuickFixTest.class,
	CStringCleanupQuickFixTest.class,
	CStrQuickFixTest.class,
	PointerParameterQuickFixTest.class,
	CStringParameterQuickFixTest.class,
	StrlenTest.class,
	StrcmpTest.class,
	StrncmpTest.class,
	MemcmpTest.class,
	StrcatTest.class,
	StrncatTest.class,
	StrcpyTest.class,
	StrncpyTest.class,
	MemcpyTest.class,
	MemmoveTest.class,
	MemchrTest.class,
	StrchrTest.class,
	StrrchrTest.class,
	StrstrTest.class,
	StrcspnTest.class,
	StrspnTest.class,
	StrdupTest.class,
	StrpbrkTest.class,
	ConvertingFunctionTest.class
//@formatter:on
})
public class TestSuiteAll {
}
