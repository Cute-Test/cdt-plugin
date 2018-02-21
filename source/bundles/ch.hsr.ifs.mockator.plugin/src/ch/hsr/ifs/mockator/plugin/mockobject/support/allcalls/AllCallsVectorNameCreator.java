package ch.hsr.ifs.mockator.plugin.mockobject.support.allcalls;

import java.util.Optional;

import org.eclipse.cdt.core.dom.ast.ASTNodeFactoryFactory;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNameSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNodeFactory;

import ch.hsr.ifs.iltis.core.functional.OptionalUtil;

import ch.hsr.ifs.mockator.plugin.MockatorConstants;
import ch.hsr.ifs.mockator.plugin.refsupport.finder.NameFinder;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.QualifiedNameCreator;


public class AllCallsVectorNameCreator {

   private static final ICPPNodeFactory        nodeFactory = ASTNodeFactoryFactory.getDefaultCPPNodeFactory();
   private final ICPPASTCompositeTypeSpecifier mockObject;
   private final IASTNode                      parent;

   public AllCallsVectorNameCreator(final ICPPASTCompositeTypeSpecifier mockObject, final IASTNode parent) {
      this.mockObject = mockObject;
      this.parent = parent;
   }

   public String getNameOfAllCallsVector() {
      return OptionalUtil.returnIfPresentElse(getRegistrationVector(), (name) -> name.toString(), () -> createAllCallsVectorName().toString());
   }

   public String getFqNameOfAllCallsVector() {
      return OptionalUtil.returnIfPresentElse(getRegistrationVector(), (name) -> new QualifiedNameCreator(name).createQualifiedName().toString(),
            () -> createNewFqNameForAllCallsVector().toString());
   }

   private ICPPASTQualifiedName createNewFqNameForAllCallsVector() {
      final ICPPASTQualifiedName className = new QualifiedNameCreator(mockObject.getName()).createQualifiedName();
      final ICPPASTName allCallsName = createAllCallsVectorName();
      final ICPPASTQualifiedName fqAllCallsName = nodeFactory.newQualifiedName(allCallsName);

      final ICPPASTNameSpecifier[] allSegments = className.getQualifier();
      for (final ICPPASTNameSpecifier segment : allSegments) {
         fqAllCallsName.addNameSpecifier(segment.copy());
      }

      return fqAllCallsName;
   }

   private Optional<IASTName> getRegistrationVector() {
      final AllCallsVectorFinderVisitor finder = new AllCallsVectorFinderVisitor();
      mockObject.accept(finder);
      return finder.getFoundCallsVector();
   }

   private ICPPASTName createAllCallsVectorName() {
      String proposedName = MockatorConstants.ALL_CALLS_VECTOR_NAME;

      if (parent instanceof IASTCompoundStatement) {
         final IASTCompoundStatement funBody = (IASTCompoundStatement) parent;

         if (isNameAlreadyExisting(funBody, proposedName)) {
            proposedName = proposedName + mockObject.getName().toString();
         }
      }
      return nodeFactory.newName(proposedName.toCharArray());
   }

   private static boolean isNameAlreadyExisting(final IASTCompoundStatement funBody, final String allCallsVectorName) {
      return new NameFinder(funBody).getNameMatchingCriteria((name) -> name.toString().equals(allCallsVectorName)).isPresent();
   }
}
