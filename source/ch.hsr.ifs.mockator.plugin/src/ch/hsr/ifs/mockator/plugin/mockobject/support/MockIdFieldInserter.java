package ch.hsr.ifs.mockator.plugin.mockobject.support;

import static ch.hsr.ifs.mockator.plugin.MockatorConstants.MOCK_ID;
import static ch.hsr.ifs.mockator.plugin.MockatorConstants.SIZE_T;

import org.eclipse.cdt.core.dom.ast.ASTNodeFactoryFactory;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNodeFactory;

import ch.hsr.ifs.mockator.plugin.refsupport.utils.ClassPublicVisibilityInserter;


// Inserts the mock_id member variable to the mock object:
// struct Mock {
// const size_t mock_id;
// };
class MockIdFieldInserter {

   private static final ICPPNodeFactory        nodeFactory = ASTNodeFactoryFactory.getDefaultCPPNodeFactory();
   private final ClassPublicVisibilityInserter inserter;

   public MockIdFieldInserter(final ClassPublicVisibilityInserter inserter) {
      this.inserter = inserter;
   }

   public void insert(final boolean hasMockIdField, final boolean hasOnlyStaticMemFuns) {
      if (!hasMockIdField && !hasOnlyStaticMemFuns) {
         final IASTSimpleDeclaration mockIdField = createMockIdField();
         insertMockIdField(mockIdField);
      }
   }

   private static IASTSimpleDeclaration createMockIdField() {
      final ICPPASTNamedTypeSpecifier constIntSpecifier = createConstSizeTSpecifier();
      final IASTSimpleDeclaration simpleDecl = nodeFactory.newSimpleDeclaration(constIntSpecifier);
      final IASTName mockIdName = nodeFactory.newName(MOCK_ID.toCharArray());
      simpleDecl.addDeclarator(nodeFactory.newDeclarator(mockIdName));
      return simpleDecl;
   }

   private static ICPPASTNamedTypeSpecifier createConstSizeTSpecifier() {
      final IASTName name = nodeFactory.newName(SIZE_T.toCharArray());
      final ICPPASTNamedTypeSpecifier sizeT = nodeFactory.newTypedefNameSpecifier(name);
      sizeT.setConst(true);
      return sizeT;
   }

   private void insertMockIdField(final IASTSimpleDeclaration mockIdField) {
      inserter.insert(mockIdField);
   }
}
