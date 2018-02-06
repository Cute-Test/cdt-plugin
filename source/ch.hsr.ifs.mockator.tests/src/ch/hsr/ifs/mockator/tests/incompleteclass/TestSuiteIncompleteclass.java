package ch.hsr.ifs.mockator.tests.incompleteclass;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ch.hsr.ifs.mockator.tests.incompleteclass.staticpoly.BugWithSutInNamespaceTest;
import ch.hsr.ifs.mockator.tests.incompleteclass.staticpoly.CallInCtorBugTest;
import ch.hsr.ifs.mockator.tests.incompleteclass.staticpoly.CharArrayAndStringTest;
import ch.hsr.ifs.mockator.tests.incompleteclass.staticpoly.CharArrayBugTest;
import ch.hsr.ifs.mockator.tests.incompleteclass.staticpoly.CharPointerBugTest;
import ch.hsr.ifs.mockator.tests.incompleteclass.staticpoly.ClassTemplateMemberFunctionsTest;
import ch.hsr.ifs.mockator.tests.incompleteclass.staticpoly.ConstMemberFunctionMissingTest;
import ch.hsr.ifs.mockator.tests.incompleteclass.staticpoly.ConstructorMissingTest;
import ch.hsr.ifs.mockator.tests.incompleteclass.staticpoly.DefaultTemplateArgumentTest;
import ch.hsr.ifs.mockator.tests.incompleteclass.staticpoly.EqualsOperatorProvidedTest;
import ch.hsr.ifs.mockator.tests.incompleteclass.staticpoly.ExplicitInstantiationMemFunMissingTest;
import ch.hsr.ifs.mockator.tests.incompleteclass.staticpoly.FakeInMemberFunctionTest;
import ch.hsr.ifs.mockator.tests.incompleteclass.staticpoly.FakeNotUsedInSutTest;
import ch.hsr.ifs.mockator.tests.incompleteclass.staticpoly.FunctionCallInExpressionTest;
import ch.hsr.ifs.mockator.tests.incompleteclass.staticpoly.FunctionCallSignaturesNoneMissingTest;
import ch.hsr.ifs.mockator.tests.incompleteclass.staticpoly.FunctionCallSignaturesOneMissingTest;
import ch.hsr.ifs.mockator.tests.incompleteclass.staticpoly.FunctionCallWithPointerSyntaxTest;
import ch.hsr.ifs.mockator.tests.incompleteclass.staticpoly.FunctionWithinNamespaceTest;
import ch.hsr.ifs.mockator.tests.incompleteclass.staticpoly.IgnoreNonTestFunctionsTest;
import ch.hsr.ifs.mockator.tests.incompleteclass.staticpoly.IndirectlyReferencedSutFunctionTest;
import ch.hsr.ifs.mockator.tests.incompleteclass.staticpoly.LocalSutNotUsedTest;
import ch.hsr.ifs.mockator.tests.incompleteclass.staticpoly.MemberFunctionAlreadyExistingTest;
import ch.hsr.ifs.mockator.tests.incompleteclass.staticpoly.MemberFunctionChainingBugTest;
import ch.hsr.ifs.mockator.tests.incompleteclass.staticpoly.MissingMemFunWhenCalledFromTestFunctorTest;
import ch.hsr.ifs.mockator.tests.incompleteclass.staticpoly.MultipleFakeObjectsOnlyOneIncompleteTest;
import ch.hsr.ifs.mockator.tests.incompleteclass.staticpoly.MultipleFakeObjectsTest;
import ch.hsr.ifs.mockator.tests.incompleteclass.staticpoly.MultipleReferencesOneMemberFunctionMissingTest;
import ch.hsr.ifs.mockator.tests.incompleteclass.staticpoly.MultipleReferencesWithSameNameBugTest;
import ch.hsr.ifs.mockator.tests.incompleteclass.staticpoly.MultipleSutInDifferentNamespacesTest;
import ch.hsr.ifs.mockator.tests.incompleteclass.staticpoly.NestedFunctionCallTest;
import ch.hsr.ifs.mockator.tests.incompleteclass.staticpoly.NoConstructorMissingTest;
import ch.hsr.ifs.mockator.tests.incompleteclass.staticpoly.NoSolelyDefaultCtorNecessaryTest;
import ch.hsr.ifs.mockator.tests.incompleteclass.staticpoly.NonLocalNotInNamespaceFakeClassTest;
import ch.hsr.ifs.mockator.tests.incompleteclass.staticpoly.NotBuiltInTypeNoneMissingTest;
import ch.hsr.ifs.mockator.tests.incompleteclass.staticpoly.NotProvidedMemberFunctionTest;
import ch.hsr.ifs.mockator.tests.incompleteclass.staticpoly.NotReferencedFromATestFunctionTest;
import ch.hsr.ifs.mockator.tests.incompleteclass.staticpoly.NotReferencedSutFunctionShouldbeIgnoredTest;
import ch.hsr.ifs.mockator.tests.incompleteclass.staticpoly.OldVsNewConstStyleTest;
import ch.hsr.ifs.mockator.tests.incompleteclass.staticpoly.OnlyWhenUsedWithStaticCall1Test;
import ch.hsr.ifs.mockator.tests.incompleteclass.staticpoly.OnlyWhenUsedWithStaticCall2Test;
import ch.hsr.ifs.mockator.tests.incompleteclass.staticpoly.OnlyWhenUsedWithStaticCall3Test;
import ch.hsr.ifs.mockator.tests.incompleteclass.staticpoly.OperatorWithNonTemplateParamTypeTest;
import ch.hsr.ifs.mockator.tests.incompleteclass.staticpoly.ParametersNotConsideredConstTest;
import ch.hsr.ifs.mockator.tests.incompleteclass.staticpoly.PassedAsPointerTest;
import ch.hsr.ifs.mockator.tests.incompleteclass.staticpoly.PassingThisOfSutTest;
import ch.hsr.ifs.mockator.tests.incompleteclass.staticpoly.PointerBug1Test;
import ch.hsr.ifs.mockator.tests.incompleteclass.staticpoly.PointerBug2Test;
import ch.hsr.ifs.mockator.tests.incompleteclass.staticpoly.PointerParameterExistingTest;
import ch.hsr.ifs.mockator.tests.incompleteclass.staticpoly.PointerParameterMissingTest;
import ch.hsr.ifs.mockator.tests.incompleteclass.staticpoly.PrefixOperatorMissingTest;
import ch.hsr.ifs.mockator.tests.incompleteclass.staticpoly.PrefixOperatorProvidedTest;
import ch.hsr.ifs.mockator.tests.incompleteclass.staticpoly.PrefixProvidedButPostfixOperatorUsedTest;
import ch.hsr.ifs.mockator.tests.incompleteclass.staticpoly.ProvidedAllMemberFunctionsTest;
import ch.hsr.ifs.mockator.tests.incompleteclass.staticpoly.QualifiedNamesInFunCalls1Test;
import ch.hsr.ifs.mockator.tests.incompleteclass.staticpoly.QualifiedNamesInFunCalls2Test;
import ch.hsr.ifs.mockator.tests.incompleteclass.staticpoly.StaticMemberFunctionMissingTest;
import ch.hsr.ifs.mockator.tests.incompleteclass.staticpoly.StringParameterBugTest;
import ch.hsr.ifs.mockator.tests.incompleteclass.staticpoly.SutAsNonTemplateFunctionTest;
import ch.hsr.ifs.mockator.tests.incompleteclass.staticpoly.SutFunctionNotCalledTest;
import ch.hsr.ifs.mockator.tests.incompleteclass.staticpoly.TypedefForFakeInSutTest;
import ch.hsr.ifs.mockator.tests.incompleteclass.staticpoly.UsingBaseClassNoneMissingTest;
import ch.hsr.ifs.mockator.tests.incompleteclass.staticpoly.VisibilityWithClassTypeTest;
import ch.hsr.ifs.mockator.tests.incompleteclass.staticpoly.VisibilityWithStructTypeTest;
import ch.hsr.ifs.mockator.tests.incompleteclass.staticpoly.WithArgumentDeductionTest;
import ch.hsr.ifs.mockator.tests.incompleteclass.staticpoly.WithDefaultTemplateParameterTest;
import ch.hsr.ifs.mockator.tests.incompleteclass.staticpoly.WithEmptyTemplateIdTest;
import ch.hsr.ifs.mockator.tests.incompleteclass.staticpoly.WithNotBuiltInTypeMissingTest;
import ch.hsr.ifs.mockator.tests.incompleteclass.staticpoly.WithNotInlinedTemplateMemFunNothingMissingTest;
import ch.hsr.ifs.mockator.tests.incompleteclass.staticpoly.WithNotInlinedTemplateMemberFunctionTest;
import ch.hsr.ifs.mockator.tests.incompleteclass.staticpoly.WithOverloadingFakeNotCalledTest;
import ch.hsr.ifs.mockator.tests.incompleteclass.staticpoly.WithOverloadingTest;
import ch.hsr.ifs.mockator.tests.incompleteclass.staticpoly.WithStaticCallTest;
import ch.hsr.ifs.mockator.tests.incompleteclass.staticpoly.WithStlContainerImplMissing1Test;
import ch.hsr.ifs.mockator.tests.incompleteclass.staticpoly.WithStlContainersAsArgumentsImplProvidedTest;
import ch.hsr.ifs.mockator.tests.incompleteclass.staticpoly.WithStlVectorMissingTest;
import ch.hsr.ifs.mockator.tests.incompleteclass.staticpoly.WithStlVectorNotMissingTest;
import ch.hsr.ifs.mockator.tests.incompleteclass.staticpoly.WithTemplateMemberFunctionTest;
import ch.hsr.ifs.mockator.tests.incompleteclass.staticpoly.WithTemporaryFakeInstanceTest;
import ch.hsr.ifs.mockator.tests.incompleteclass.staticpoly.WithTestDoubleInNsTest;
import ch.hsr.ifs.mockator.tests.incompleteclass.staticpoly.WithTwoTemplateParamsTest;
import ch.hsr.ifs.mockator.tests.incompleteclass.staticpoly.WithTwoTestFunctionsTest;
import ch.hsr.ifs.mockator.tests.incompleteclass.staticpoly.WithTypedefForSutTest;
import ch.hsr.ifs.mockator.tests.incompleteclass.subtype.AllMemFunsProvidedInLocalClassTest;
import ch.hsr.ifs.mockator.tests.incompleteclass.subtype.InstantiationOfAbstractClassTest;
import ch.hsr.ifs.mockator.tests.incompleteclass.subtype.MissingMemFunInLocalClassTest;
import ch.hsr.ifs.mockator.tests.incompleteclass.subtype.NonLocalClassWithMissingMemFunsTest;
import ch.hsr.ifs.mockator.tests.incompleteclass.subtype.NonLocalCompleteClassTest;
import ch.hsr.ifs.mockator.tests.incompleteclass.subtype.OnlyDestructorTest;
import ch.hsr.ifs.mockator.tests.incompleteclass.subtype.OnlyPointerAndReferenceDeclarationTest;
import ch.hsr.ifs.mockator.tests.incompleteclass.subtype.WithMultipleInheritanceHierarchiesTest;


@RunWith(Suite.class)
//@formatter:off
@SuiteClasses({
 
   /*IncompleteClass*/
   NonLocalCompleteClassTest.class,
   NonLocalClassWithMissingMemFunsTest.class,
   WithMultipleInheritanceHierarchiesTest.class,
   AllMemFunsProvidedInLocalClassTest.class,
   MissingMemFunInLocalClassTest.class,
   NotReferencedFromATestFunctionTest.class,
   OnlyDestructorTest.class,
   InstantiationOfAbstractClassTest.class,
   OnlyPointerAndReferenceDeclarationTest.class,
   OldVsNewConstStyleTest.class,
   IgnoreNonTestFunctionsTest.class,
   WithStlVectorMissingTest.class,
   MissingMemFunWhenCalledFromTestFunctorTest.class,
   OnlyWhenUsedWithStaticCall3Test.class,
   UsingBaseClassNoneMissingTest.class,
   WithStlContainerImplMissing1Test.class,
   CharArrayBugTest.class,
   FakeInMemberFunctionTest.class,
   WithTemplateMemberFunctionTest.class,
   EqualsOperatorProvidedTest.class,
   IndirectlyReferencedSutFunctionTest.class,
   CharArrayAndStringTest.class,
   OnlyWhenUsedWithStaticCall1Test.class,
   PassedAsPointerTest.class,
   PointerBug2Test.class,
   MemberFunctionAlreadyExistingTest.class,
   PrefixProvidedButPostfixOperatorUsedTest.class,
   WithTwoTestFunctionsTest.class,
   CharPointerBugTest.class,
   BugWithSutInNamespaceTest.class,
   PointerParameterExistingTest.class,
   NotReferencedSutFunctionShouldbeIgnoredTest.class,
   WithTemporaryFakeInstanceTest.class,
   WithTwoTemplateParamsTest.class,
   WithOverloadingFakeNotCalledTest.class,
   WithStlVectorNotMissingTest.class,
   LocalSutNotUsedTest.class,
   NotProvidedMemberFunctionTest.class,
   WithStlContainersAsArgumentsImplProvidedTest.class,
   FunctionWithinNamespaceTest.class,
   StaticMemberFunctionMissingTest.class,
   NoSolelyDefaultCtorNecessaryTest.class,
   ClassTemplateMemberFunctionsTest.class,
   CallInCtorBugTest.class,
   MemberFunctionChainingBugTest.class,
   WithNotInlinedTemplateMemFunNothingMissingTest.class,
   QualifiedNamesInFunCalls1Test.class,
   ConstMemberFunctionMissingTest.class,
   NonLocalNotInNamespaceFakeClassTest.class,
   ParametersNotConsideredConstTest.class,
   OnlyWhenUsedWithStaticCall2Test.class,
   FunctionCallInExpressionTest.class,
   ProvidedAllMemberFunctionsTest.class,
   MultipleFakeObjectsTest.class,
   FunctionCallSignaturesOneMissingTest.class,
   FunctionCallSignaturesNoneMissingTest.class,
   FunctionCallWithPointerSyntaxTest.class,
   SutAsNonTemplateFunctionTest.class,
   PassingThisOfSutTest.class,
   VisibilityWithClassTypeTest.class,
   MultipleFakeObjectsOnlyOneIncompleteTest.class,
   DefaultTemplateArgumentTest.class,
   NotReferencedFromATestFunctionTest.class,
   WithTestDoubleInNsTest.class,
   WithStaticCallTest.class,
   NotBuiltInTypeNoneMissingTest.class,
   QualifiedNamesInFunCalls2Test.class,
   MultipleReferencesWithSameNameBugTest.class,
   PrefixOperatorProvidedTest.class,
   ConstructorMissingTest.class,
   MultipleReferencesOneMemberFunctionMissingTest.class,
   WithDefaultTemplateParameterTest.class,
   OperatorWithNonTemplateParamTypeTest.class,
   PointerBug1Test.class,
   WithNotInlinedTemplateMemberFunctionTest.class,
   PrefixOperatorMissingTest.class,
   ExplicitInstantiationMemFunMissingTest.class,
   NestedFunctionCallTest.class,
   WithArgumentDeductionTest.class,
   PointerParameterMissingTest.class,
   WithNotBuiltInTypeMissingTest.class,
   StringParameterBugTest.class,
   WithOverloadingTest.class,
   VisibilityWithStructTypeTest.class,
   TypedefForFakeInSutTest.class,
   NoConstructorMissingTest.class,
   WithEmptyTemplateIdTest.class,
   SutFunctionNotCalledTest.class,
   FakeNotUsedInSutTest.class,
   MultipleSutInDifferentNamespacesTest.class,
   WithTypedefForSutTest.class,

})
public class TestSuiteIncompleteclass {
}
