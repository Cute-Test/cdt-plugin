package ch.hsr.ifs.mockator.plugin.mockobject.support.allcalls;

import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNodeFactory;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPNodeFactory;

import ch.hsr.ifs.mockator.plugin.MockatorConstants;
import ch.hsr.ifs.mockator.plugin.base.functional.F1;
import ch.hsr.ifs.mockator.plugin.base.maybe.Maybe;
import ch.hsr.ifs.mockator.plugin.refsupport.finder.NameFinder;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.QualifiedNameCreator;

@SuppressWarnings("restriction")
public class AllCallsVectorNameCreator {
  private static final ICPPNodeFactory nodeFactory = CPPNodeFactory.getDefault();
  private final ICPPASTCompositeTypeSpecifier mockObject;
  private final IASTNode parent;

  public AllCallsVectorNameCreator(ICPPASTCompositeTypeSpecifier mockObject, IASTNode parent) {
    this.mockObject = mockObject;
    this.parent = parent;
  }

  public String getNameOfAllCallsVector() {
    for (IASTName optName : getRegistrationVector())
      return optName.toString();

    return createAllCallsVectorName().toString();
  }

  public String getFqNameOfAllCallsVector() {
    for (IASTName optName : getRegistrationVector())
      return new QualifiedNameCreator(optName).createQualifiedName().toString();

    return createNewFqNameForAllCallsVector().toString();
  }

  private ICPPASTQualifiedName createNewFqNameForAllCallsVector() {
    ICPPASTQualifiedName className =
        new QualifiedNameCreator(mockObject.getName()).createQualifiedName();
    ICPPASTQualifiedName fqAllCallsName = nodeFactory.newQualifiedName();

    for (int i = 0; i < className.getNames().length - 1; i++) {
      fqAllCallsName.addName(className.getNames()[i].copy());
    }

    IASTName allCallsName = createAllCallsVectorName();
    fqAllCallsName.addName(allCallsName);
    return fqAllCallsName;
  }

  private Maybe<IASTName> getRegistrationVector() {
    AllCallsVectorFinderVisitor finder = new AllCallsVectorFinderVisitor();
    mockObject.accept(finder);
    return finder.getFoundCallsVector();
  }

  private IASTName createAllCallsVectorName() {
    String proposedName = MockatorConstants.ALL_CALLS_VECTOR_NAME;

    if (parent instanceof IASTCompoundStatement) {
      IASTCompoundStatement funBody = (IASTCompoundStatement) parent;

      if (isNameAlreadyExisting(funBody, proposedName)) {
        proposedName = proposedName + mockObject.getName().toString();
      }
    }
    return nodeFactory.newName(proposedName.toCharArray());
  }

  private static boolean isNameAlreadyExisting(IASTCompoundStatement funBody,
      final String allCallsVectorName) {
    return new NameFinder(funBody).getNameMatchingCriteria(new F1<IASTName, Boolean>() {
      @Override
      public Boolean apply(IASTName name) {
        return name.toString().equals(allCallsVectorName);
      }
    }).isSome();
  }
}
