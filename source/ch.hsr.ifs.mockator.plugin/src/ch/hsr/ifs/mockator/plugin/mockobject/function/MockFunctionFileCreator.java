package ch.hsr.ifs.mockator.plugin.mockobject.function;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.array;
import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;

import java.util.Collection;
import java.util.Optional;

import org.eclipse.cdt.core.dom.ast.ASTNodeFactoryFactory;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNodeFactory;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

import ch.hsr.ifs.iltis.core.resources.FileUtil;
import ch.hsr.ifs.iltis.cpp.wrappers.CRefactoringContext;
import ch.hsr.ifs.iltis.cpp.wrappers.ModificationCollector;

import ch.hsr.ifs.mockator.plugin.MockatorConstants;
import ch.hsr.ifs.mockator.plugin.base.util.PathProposalUtil;
import ch.hsr.ifs.mockator.plugin.linker.shadowfun.ShadowFunctionGenerator;
import ch.hsr.ifs.mockator.plugin.mockobject.expectations.vector.ExpectationsCppStdStrategy;
import ch.hsr.ifs.mockator.plugin.mockobject.expectations.vector.ExpectationsVectorFactory;
import ch.hsr.ifs.mockator.plugin.mockobject.registrations.FreeFunCallRegistrationAdder;
import ch.hsr.ifs.mockator.plugin.mockobject.support.MockatorIncludeInserter;
import ch.hsr.ifs.mockator.plugin.mockobject.support.MockatorInitCallCreator;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.project.properties.LinkedEditModeStrategy;
import ch.hsr.ifs.mockator.plugin.refsupport.functions.params.ParameterNameFunDecorator;
import ch.hsr.ifs.mockator.plugin.refsupport.includes.AstIncludeNode;
import ch.hsr.ifs.mockator.plugin.refsupport.includes.CppIncludeResolver;
import ch.hsr.ifs.mockator.plugin.refsupport.lookup.NodeLookup;
import ch.hsr.ifs.mockator.plugin.refsupport.tu.SiblingTranslationUnitFinder;
import ch.hsr.ifs.mockator.plugin.refsupport.tu.TranslationUnitCreator;
import ch.hsr.ifs.iltis.cpp.ast.ASTUtil;
import ch.hsr.ifs.mockator.plugin.testdouble.entities.ExistingTestDoubleMemFun;

abstract class MockFunctionFileCreator {

   protected static final ICPPNodeFactory nodeFactory          = ASTNodeFactoryFactory.getDefaultCPPNodeFactory();
   protected static final String          EXPECTED_VECTOR_NAME = "expected";
   private final ModificationCollector    collector;
   private final IProgressMonitor         pm;
   private final ICProject                mockatorProject;
   private final ITranslationUnit         originTu;
   private final CRefactoringContext      cRefContext;
   private final ICProject                originProject;
   private final CppStandard              cppStd;
   private IFile                          newlyCreatedFile;

   public MockFunctionFileCreator(final ModificationCollector collector, final CRefactoringContext cRefContext, final ITranslationUnit originTu,
         final ICProject mockatorProject, final ICProject originProject, final CppStandard cppStd,
         final IProgressMonitor pm) {
      this.collector = collector;
      this.cRefContext = cRefContext;
      this.originTu = originTu;
      this.mockatorProject = mockatorProject;
      this.originProject = originProject;
      this.cppStd = cppStd;
      this.pm = pm;
   }

   public void createHeaderFile(final String suiteName, final IPath destinationPath, final IASTName functionToMock) throws CoreException {
      final IASTTranslationUnit newTu = createAndGetNewTu(suiteName, destinationPath, MockatorConstants.HEADER_SUFFIX, pm);
      final ASTRewrite rewriter = collector.rewriterForTranslationUnit(newTu);
      insertContentForHeaderFile(newTu, rewriter, functionToMock, suiteName);
   }

   protected abstract void insertContentForHeaderFile(IASTTranslationUnit newTu, ASTRewrite rewriter, IASTName functionToMock, String suiteName);

   public IFile createSourceFile(final String suiteName, final IPath destination, final IASTName funToMockName) throws CoreException {
      final IASTTranslationUnit newTu = createAndGetNewTu(suiteName, destination, MockatorConstants.SOURCE_SUFFIX, pm);
      final ASTRewrite rewriter = collector.rewriterForTranslationUnit(newTu);
      insertContentForSourceFile(suiteName, funToMockName, newTu, rewriter);
      return newlyCreatedFile;
   }

   private void insertContentForSourceFile(final String suiteName, final IASTName funToMockName, final IASTTranslationUnit newTu,
         final ASTRewrite rewriter) throws CoreException {
      insertIncludeForCurrentTu(newTu, rewriter);
      insertIncludeForMockedFun(funToMockName, newTu, rewriter);
      insertAssertIncludes(newTu, rewriter);
      insertMockatorInclude(newTu, rewriter);
      final ICPPASTNamespaceDefinition callsVectorNs = createNewNs(funToMockName);
      final IASTName callsVectorName = createCallsVectorName();
      insertNamespaceWithCallsVector(newTu, rewriter, callsVectorNs, callsVectorName);
      final String fqCallsVectorName = createFqCallsVectorName(callsVectorNs, callsVectorName);
      final ICPPASTFunctionDeclarator funDeclToMock = findFunDeclBy(funToMockName).get();
      final ICPPASTFunctionDefinition mockedFunction = createMockedFunction(funDeclToMock, fqCallsVectorName);
      insertMockedFunction(newTu, rewriter, mockedFunction);
      insertTestFunction(newTu, rewriter, mockedFunction, funToMockName, fqCallsVectorName);
      createAddtitionalTestSupport(newTu, rewriter, funDeclToMock, suiteName);
   }

   protected abstract void insertAssertIncludes(IASTTranslationUnit newTu, ASTRewrite rewriter);

   protected abstract void createAddtitionalTestSupport(IASTTranslationUnit newTu, ASTRewrite rewriter, ICPPASTFunctionDeclarator funDeclToMock,
         String suiteName);

   private IASTTranslationUnit createAndGetNewTu(final String suiteName, final IPath destinationPath, final String suffix, final IProgressMonitor pm)
         throws CoreException {
      final IPath newFilePath = createPathForNewFile(suiteName, destinationPath, suffix);
      newlyCreatedFile = FileUtil.toIFile(newFilePath);
      final TranslationUnitCreator creator = new TranslationUnitCreator(mockatorProject.getProject(), cRefContext);
      return creator.createAndGetNewTu(newFilePath, pm);
   }

   private static IPath createPathForNewFile(final String suiteName, final IPath destinationPath, final String suffix) {
      final PathProposalUtil proposal = new PathProposalUtil(destinationPath);
      return proposal.getUniquePathForNewFile(suiteName, suffix);
   }

   private static String createFqCallsVectorName(final ICPPASTNamespaceDefinition callsVectorNs, final IASTName callsVectorName) {
      return ASTUtil.getQfName(array(callsVectorNs.getName().toString(), callsVectorName.toString()));
   }

   private static void insertNamespaceWithCallsVector(final IASTTranslationUnit newTu, final ASTRewrite rewriter,
         final ICPPASTNamespaceDefinition callsVectorNs, final IASTName callsVectorName) {
      final IASTSimpleDeclaration callsVector = createCallsVector(callsVectorName);
      callsVectorNs.addDeclaration(callsVector);
      rewriter.insertBefore(newTu, null, callsVectorNs, null);
   }

   private static IASTName createCallsVectorName() {
      return nodeFactory.newName(MockatorConstants.ALL_CALLS_VECTOR_NAME.toCharArray());
   }

   // mockator::calls callsfoo;
   private static IASTSimpleDeclaration createCallsVector(final IASTName callsVectorName) {
      final ICPPASTQualifiedName callsVectorType = createCallsVectorType();
      final ICPPASTNamedTypeSpecifier namedTypeSpec = nodeFactory.newTypedefNameSpecifier(callsVectorType);
      final IASTSimpleDeclaration callsVectorDecl = nodeFactory.newSimpleDeclaration(namedTypeSpec);
      callsVectorDecl.addDeclarator(nodeFactory.newDeclarator(callsVectorName));
      return callsVectorDecl;
   }

   private static ICPPASTQualifiedName createCallsVectorType() {
      final ICPPASTQualifiedName callsVectorTypeName = nodeFactory.newQualifiedName(null);
      callsVectorTypeName.addName(nodeFactory.newName(MockatorConstants.MOCKATOR_NS.toCharArray()));
      callsVectorTypeName.addName(nodeFactory.newName(MockatorConstants.CALLS.toCharArray()));
      return callsVectorTypeName;
   }

   // namespace testfoo_Ns { }
   private static ICPPASTNamespaceDefinition createNewNs(final IASTName funToMockName) {
      final String namespaceName = MockatorConstants.TEST_FUNCTION_PREFIX + funToMockName.toString() + MockatorConstants.NS_SUFFIX;
      return nodeFactory.newNamespaceDefinition(nodeFactory.newName(namespaceName.toCharArray()));
   }

   private static void insertMockedFunction(final IASTTranslationUnit newTu, final ASTRewrite rewriter, final ICPPASTFunctionDefinition mockedFun) {
      rewriter.insertBefore(newTu, null, mockedFun, null);
   }

   private ICPPASTFunctionDefinition createMockedFunction(final ICPPASTFunctionDeclarator funToMock, final String callsVectorName) {
      final IASTCompoundStatement funBody = nodeFactory.newCompoundStatement();
      addArgumentRegistration(funToMock.copy(), callsVectorName, funBody);
      return new ShadowFunctionGenerator(cppStd).createShadowedFunction(funToMock, funBody);
   }

   private void addArgumentRegistration(final ICPPASTFunctionDeclarator funDecl, final String callsVectorName, final IASTCompoundStatement funBody) {
      final ParameterNameFunDecorator funDecorator = new ParameterNameFunDecorator(funDecl);
      funDecorator.adjustParamNamesIfNecessary();
      new FreeFunCallRegistrationAdder(funDecl, cppStd, callsVectorName).addRegistrationTo(funBody);
   }

   private void insertIncludeForCurrentTu(final IASTTranslationUnit newTu, final ASTRewrite rewriter) throws CoreException {
      final Optional<String> path = getPathToCurrentTuHeader();
      if (path.isPresent()) {
         final CppIncludeResolver resolver = new CppIncludeResolver(newTu, mockatorProject, cRefContext.getIndex());
         final AstIncludeNode includeToCurrentTu = resolver.resolveIncludeNode(path.get());
         rewriter.insertBefore(newTu, null, includeToCurrentTu, null);
      }
   }

   private Optional<String> getPathToCurrentTuHeader() throws CoreException {
      final IASTTranslationUnit ast = cRefContext.getAST(originTu, pm);

      if (originTu.isHeaderUnit()) { return Optional.of(ast.getFilePath()); }

      final IFile fileForTu = FileUtil.toIFile(originTu.getPath());
      final SiblingTranslationUnitFinder finder = new SiblingTranslationUnitFinder(fileForTu, ast, cRefContext.getIndex());
      return finder.getSiblingTuPath();
   }

   private ICPPASTFunctionDeclarator insertIncludeForMockedFun(final IASTName funToMockName, final IASTTranslationUnit newTu,
         final ASTRewrite rewriter) throws CoreException {
      final ICPPASTFunctionDeclarator funDecl = findFunDeclBy(funToMockName).get();
      final CppIncludeResolver resolver = new CppIncludeResolver(newTu, mockatorProject, cRefContext.getIndex());
      final String funDeclTuPath = funDecl.getTranslationUnit().getFilePath();
      final AstIncludeNode includeForFunDecl = resolver.resolveIncludeNode(funDeclTuPath);
      rewriter.insertBefore(newTu, null, includeForFunDecl, null);
      return funDecl;
   }

   private Optional<ICPPASTFunctionDeclarator> findFunDeclBy(final IASTName funName) {
      final NodeLookup lookup = new NodeLookup(originProject, pm);
      return lookup.findFunctionDeclaration(funName, cRefContext);
   }

   private static void insertMockatorInclude(final IASTTranslationUnit newTu, final ASTRewrite rewriter) {
      final MockatorIncludeInserter inserter = new MockatorIncludeInserter(newTu);
      inserter.insertWith(rewriter);
   }

   private void insertTestFunction(final IASTTranslationUnit newTu, final ASTRewrite rewriter, final ICPPASTFunctionDefinition funToMock,
         final IASTName funToMockName, final String fqCallsVectorName) {
      final ICPPASTFunctionDeclarator funDecl = createTestFunctionDecl(funToMockName);
      final ICPPASTSimpleDeclSpecifier funDeclSpec = nodeFactory.newSimpleDeclSpecifier();
      funDeclSpec.setType(IASTSimpleDeclSpecifier.t_void);
      final IASTCompoundStatement testFunBody = nodeFactory.newCompoundStatement();
      final ICPPASTFunctionDefinition testFunction = nodeFactory.newFunctionDefinition(funDeclSpec, funDecl, testFunBody);
      createInitMockator(testFunBody, testFunction);
      createExpectations(funToMock, testFunBody, testFunction);
      testFunBody.addStatement(createAssertEqualStmt(fqCallsVectorName));
      rewriter.insertBefore(newTu, null, testFunction, null);
   }

   private void createExpectations(final ICPPASTFunctionDefinition functionToMock, final IASTCompoundStatement testFunBody,
         final ICPPASTFunctionDefinition testFunction) {
      final ExistingTestDoubleMemFun testDoubleMemFun = new ExistingTestDoubleMemFun(functionToMock);

      for (final IASTStatement stmt : createExpectationsVector(testFunction, testDoubleMemFun)) {
         testFunBody.addStatement(stmt);
      }
   }

   protected ICPPASTFunctionDeclarator createTestFunctionDecl(final IASTName nameOfFunToMock) {
      final String testFunName = MockatorConstants.TEST_FUNCTION_PREFIX + nameOfFunToMock.toString();
      final IASTName testFunctionName = nodeFactory.newName(testFunName.toCharArray());
      return nodeFactory.newFunctionDeclarator(testFunctionName);
   }

   private static void createInitMockator(final IASTCompoundStatement funBody, final ICPPASTFunctionDefinition testFunction) {
      final MockatorInitCallCreator creator = new MockatorInitCallCreator(testFunction);
      funBody.addStatement((IASTExpressionStatement) creator.createMockatorInitCall());
   }

   private Collection<IASTStatement> createExpectationsVector(final ICPPASTFunctionDefinition testFun,
         final ExistingTestDoubleMemFun functionToMock) {
      final Optional<IASTName> noExistingExpectations = Optional.empty();
      return getCppStdStrategy().createExpectationsVector(list(functionToMock), EXPECTED_VECTOR_NAME, testFun, noExistingExpectations,
            LinkedEditModeStrategy.ChooseArguments);
   }

   private ExpectationsCppStdStrategy getCppStdStrategy() {
      return new ExpectationsVectorFactory(cppStd).getStrategy();
   }

   protected abstract IASTExpressionStatement createAssertEqualStmt(String fqCallsVectorName);
}
