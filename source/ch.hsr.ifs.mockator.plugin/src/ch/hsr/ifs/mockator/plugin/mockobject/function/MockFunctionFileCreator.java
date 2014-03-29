package ch.hsr.ifs.mockator.plugin.mockobject.function;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.array;
import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;
import static ch.hsr.ifs.mockator.plugin.base.maybe.Maybe.maybe;
import static ch.hsr.ifs.mockator.plugin.base.maybe.Maybe.none;

import java.util.Collection;

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
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPNodeFactory;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoringContext;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

import ch.hsr.ifs.mockator.plugin.MockatorConstants;
import ch.hsr.ifs.mockator.plugin.base.maybe.Maybe;
import ch.hsr.ifs.mockator.plugin.base.util.FileUtil;
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
import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;
import ch.hsr.ifs.mockator.plugin.testdouble.entities.ExistingTestDoubleMemFun;

@SuppressWarnings("restriction")
abstract class MockFunctionFileCreator {
  protected static final ICPPNodeFactory nodeFactory = CPPNodeFactory.getDefault();
  protected static final String EXPECTED_VECTOR_NAME = "expected";
  private final ModificationCollector collector;
  private final IProgressMonitor pm;
  private final ICProject mockatorProject;
  private final ITranslationUnit originTu;
  private final CRefactoringContext cRefContext;
  private final ICProject originProject;
  private final CppStandard cppStd;
  private IFile newlyCreatedFile;

  public MockFunctionFileCreator(ModificationCollector collector, CRefactoringContext cRefContext,
      ITranslationUnit originTu, ICProject mockatorProject, ICProject originProject,
      CppStandard cppStd, IProgressMonitor pm) {
    this.collector = collector;
    this.cRefContext = cRefContext;
    this.originTu = originTu;
    this.mockatorProject = mockatorProject;
    this.originProject = originProject;
    this.cppStd = cppStd;
    this.pm = pm;
  }

  public void createHeaderFile(String suiteName, IPath destinationPath, IASTName functionToMock)
      throws CoreException {
    IASTTranslationUnit newTu =
        createAndGetNewTu(suiteName, destinationPath, MockatorConstants.HEADER_SUFFIX, pm);
    ASTRewrite rewriter = collector.rewriterForTranslationUnit(newTu);
    insertContentForHeaderFile(newTu, rewriter, functionToMock, suiteName);
  }

  protected abstract void insertContentForHeaderFile(IASTTranslationUnit newTu,
      ASTRewrite rewriter, IASTName functionToMock, String suiteName);

  public IFile createSourceFile(String suiteName, IPath destination, IASTName funToMockName)
      throws CoreException {
    IASTTranslationUnit newTu =
        createAndGetNewTu(suiteName, destination, MockatorConstants.SOURCE_SUFFIX, pm);
    ASTRewrite rewriter = collector.rewriterForTranslationUnit(newTu);
    insertContentForSourceFile(suiteName, funToMockName, newTu, rewriter);
    return newlyCreatedFile;
  }

  private void insertContentForSourceFile(String suiteName, IASTName funToMockName,
      IASTTranslationUnit newTu, ASTRewrite rewriter) throws CoreException {
    insertIncludeForCurrentTu(newTu, rewriter);
    insertIncludeForMockedFun(funToMockName, newTu, rewriter);
    insertAssertIncludes(newTu, rewriter);
    insertMockatorInclude(newTu, rewriter);
    ICPPASTNamespaceDefinition callsVectorNs = createNewNs(funToMockName);
    IASTName callsVectorName = createCallsVectorName();
    insertNamespaceWithCallsVector(newTu, rewriter, callsVectorNs, callsVectorName);
    String fqCallsVectorName = createFqCallsVectorName(callsVectorNs, callsVectorName);
    ICPPASTFunctionDeclarator funDeclToMock = findFunDeclBy(funToMockName).get();
    ICPPASTFunctionDefinition mockedFunction =
        createMockedFunction(funDeclToMock, fqCallsVectorName);
    insertMockedFunction(newTu, rewriter, mockedFunction);
    insertTestFunction(newTu, rewriter, mockedFunction, funToMockName, fqCallsVectorName);
    createAddtitionalTestSupport(newTu, rewriter, funDeclToMock, suiteName);
  }

  protected abstract void insertAssertIncludes(IASTTranslationUnit newTu, ASTRewrite rewriter);

  protected abstract void createAddtitionalTestSupport(IASTTranslationUnit newTu,
      ASTRewrite rewriter, ICPPASTFunctionDeclarator funDeclToMock, String suiteName);

  private IASTTranslationUnit createAndGetNewTu(String suiteName, IPath destinationPath,
      String suffix, IProgressMonitor pm) throws CoreException {
    IPath newFilePath = createPathForNewFile(suiteName, destinationPath, suffix);
    newlyCreatedFile = FileUtil.toIFile(newFilePath);
    TranslationUnitCreator creator =
        new TranslationUnitCreator(mockatorProject.getProject(), cRefContext);
    return creator.createAndGetNewTu(newFilePath, pm);
  }

  private static IPath createPathForNewFile(String suiteName, IPath destinationPath, String suffix) {
    PathProposalUtil proposal = new PathProposalUtil(destinationPath);
    return proposal.getUniquePathForNewFile(suiteName, suffix);
  }

  private static String createFqCallsVectorName(ICPPASTNamespaceDefinition callsVectorNs,
      IASTName callsVectorName) {
    return AstUtil.getQfName(array(callsVectorNs.getName().toString(), callsVectorName.toString()));
  }

  private static void insertNamespaceWithCallsVector(IASTTranslationUnit newTu,
      ASTRewrite rewriter, ICPPASTNamespaceDefinition callsVectorNs, IASTName callsVectorName) {
    IASTSimpleDeclaration callsVector = createCallsVector(callsVectorName);
    callsVectorNs.addDeclaration(callsVector);
    rewriter.insertBefore(newTu, null, callsVectorNs, null);
  }

  private static IASTName createCallsVectorName() {
    return nodeFactory.newName(MockatorConstants.ALL_CALLS_VECTOR_NAME.toCharArray());
  }

  // mockator::calls callsfoo;
  private static IASTSimpleDeclaration createCallsVector(IASTName callsVectorName) {
    ICPPASTQualifiedName callsVectorType = createCallsVectorType();
    ICPPASTNamedTypeSpecifier namedTypeSpec = nodeFactory.newTypedefNameSpecifier(callsVectorType);
    IASTSimpleDeclaration callsVectorDecl = nodeFactory.newSimpleDeclaration(namedTypeSpec);
    callsVectorDecl.addDeclarator(nodeFactory.newDeclarator(callsVectorName));
    return callsVectorDecl;
  }

  private static ICPPASTQualifiedName createCallsVectorType() {
    ICPPASTQualifiedName callsVectorTypeName = nodeFactory.newQualifiedName();
    callsVectorTypeName.addName(nodeFactory.newName(MockatorConstants.MOCKATOR_NS.toCharArray()));
    callsVectorTypeName.addName(nodeFactory.newName(MockatorConstants.CALLS.toCharArray()));
    return callsVectorTypeName;
  }

  // namespace testfoo_Ns { }
  private static ICPPASTNamespaceDefinition createNewNs(IASTName funToMockName) {
    String namespaceName =
        MockatorConstants.TEST_FUNCTION_PREFIX + funToMockName.toString()
            + MockatorConstants.NS_SUFFIX;
    return nodeFactory.newNamespaceDefinition(nodeFactory.newName(namespaceName.toCharArray()));
  }

  private static void insertMockedFunction(IASTTranslationUnit newTu, ASTRewrite rewriter,
      ICPPASTFunctionDefinition mockedFun) {
    rewriter.insertBefore(newTu, null, mockedFun, null);
  }

  private ICPPASTFunctionDefinition createMockedFunction(ICPPASTFunctionDeclarator funToMock,
      String callsVectorName) {
    IASTCompoundStatement funBody = nodeFactory.newCompoundStatement();
    addArgumentRegistration(funToMock.copy(), callsVectorName, funBody);
    return new ShadowFunctionGenerator(cppStd).createShadowedFunction(funToMock, funBody);
  }

  private void addArgumentRegistration(ICPPASTFunctionDeclarator funDecl, String callsVectorName,
      IASTCompoundStatement funBody) {
    ParameterNameFunDecorator funDecorator = new ParameterNameFunDecorator(funDecl);
    funDecorator.adjustParamNamesIfNecessary();
    new FreeFunCallRegistrationAdder(funDecl, cppStd, callsVectorName).addRegistrationTo(funBody);
  }

  private void insertIncludeForCurrentTu(IASTTranslationUnit newTu, ASTRewrite rewriter)
      throws CoreException {
    for (String optPath : getPathToCurrentTuHeader()) {
      CppIncludeResolver resolver =
          new CppIncludeResolver(newTu, mockatorProject, cRefContext.getIndex());
      AstIncludeNode includeToCurrentTu = resolver.resolveIncludeNode(optPath);
      rewriter.insertBefore(newTu, null, includeToCurrentTu, null);
    }
  }

  private Maybe<String> getPathToCurrentTuHeader() throws CoreException {
    IASTTranslationUnit ast = cRefContext.getAST(originTu, pm);

    if (originTu.isHeaderUnit())
      return maybe(ast.getFilePath());

    IFile fileForTu = FileUtil.toIFile(originTu.getPath());
    SiblingTranslationUnitFinder finder =
        new SiblingTranslationUnitFinder(fileForTu, ast, cRefContext.getIndex());
    return finder.getSiblingTuPath();
  }

  private ICPPASTFunctionDeclarator insertIncludeForMockedFun(IASTName funToMockName,
      IASTTranslationUnit newTu, ASTRewrite rewriter) throws CoreException {
    ICPPASTFunctionDeclarator funDecl = findFunDeclBy(funToMockName).get();
    CppIncludeResolver resolver =
        new CppIncludeResolver(newTu, mockatorProject, cRefContext.getIndex());
    String funDeclTuPath = funDecl.getTranslationUnit().getFilePath();
    AstIncludeNode includeForFunDecl = resolver.resolveIncludeNode(funDeclTuPath);
    rewriter.insertBefore(newTu, null, includeForFunDecl, null);
    return funDecl;
  }

  private Maybe<ICPPASTFunctionDeclarator> findFunDeclBy(IASTName funName) {
    NodeLookup lookup = new NodeLookup(originProject, pm);
    return lookup.findFunctionDeclaration(funName, cRefContext);
  }

  private static void insertMockatorInclude(IASTTranslationUnit newTu, ASTRewrite rewriter) {
    MockatorIncludeInserter inserter = new MockatorIncludeInserter(newTu);
    inserter.insertWith(rewriter);
  }

  private void insertTestFunction(IASTTranslationUnit newTu, ASTRewrite rewriter,
      ICPPASTFunctionDefinition funToMock, IASTName funToMockName, String fqCallsVectorName) {
    ICPPASTFunctionDeclarator funDecl = createTestFunctionDecl(funToMockName);
    ICPPASTSimpleDeclSpecifier funDeclSpec = nodeFactory.newSimpleDeclSpecifier();
    funDeclSpec.setType(IASTSimpleDeclSpecifier.t_void);
    IASTCompoundStatement testFunBody = nodeFactory.newCompoundStatement();
    ICPPASTFunctionDefinition testFunction =
        nodeFactory.newFunctionDefinition(funDeclSpec, funDecl, testFunBody);
    createInitMockator(testFunBody, testFunction);
    createExpectations(funToMock, testFunBody, testFunction);
    testFunBody.addStatement(createAssertEqualStmt(fqCallsVectorName));
    rewriter.insertBefore(newTu, null, testFunction, null);
  }

  private void createExpectations(ICPPASTFunctionDefinition functionToMock,
      IASTCompoundStatement testFunBody, ICPPASTFunctionDefinition testFunction) {
    ExistingTestDoubleMemFun testDoubleMemFun = new ExistingTestDoubleMemFun(functionToMock);

    for (IASTStatement stmt : createExpectationsVector(testFunction, testDoubleMemFun)) {
      testFunBody.addStatement(stmt);
    }
  }

  protected ICPPASTFunctionDeclarator createTestFunctionDecl(IASTName nameOfFunToMock) {
    String testFunName = MockatorConstants.TEST_FUNCTION_PREFIX + nameOfFunToMock.toString();
    IASTName testFunctionName = nodeFactory.newName(testFunName.toCharArray());
    return nodeFactory.newFunctionDeclarator(testFunctionName);
  }

  private static void createInitMockator(IASTCompoundStatement funBody,
      ICPPASTFunctionDefinition testFunction) {
    MockatorInitCallCreator creator = new MockatorInitCallCreator(testFunction);
    funBody.addStatement((IASTExpressionStatement) creator.createMockatorInitCall());
  }

  private Collection<IASTStatement> createExpectationsVector(ICPPASTFunctionDefinition testFun,
      ExistingTestDoubleMemFun functionToMock) {
    Maybe<IASTName> noExistingExpectations = none();
    return getCppStdStrategy().createExpectationsVector(list(functionToMock), EXPECTED_VECTOR_NAME,
        testFun, noExistingExpectations, LinkedEditModeStrategy.ChooseArguments);
  }

  private ExpectationsCppStdStrategy getCppStdStrategy() {
    return new ExpectationsVectorFactory(cppStd).getStrategy();
  }

  protected abstract IASTExpressionStatement createAssertEqualStmt(String fqCallsVectorName);
}
