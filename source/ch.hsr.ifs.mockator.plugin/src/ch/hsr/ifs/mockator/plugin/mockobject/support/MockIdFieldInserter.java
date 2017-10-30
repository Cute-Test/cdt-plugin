package ch.hsr.ifs.mockator.plugin.mockobject.support;

import static ch.hsr.ifs.mockator.plugin.MockatorConstants.MOCK_ID;
import static ch.hsr.ifs.mockator.plugin.MockatorConstants.SIZE_T;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPNodeFactory;

import ch.hsr.ifs.mockator.plugin.refsupport.utils.ClassPublicVisibilityInserter;


// Inserts the mock_id member variable to the mock object:
// struct Mock {
// const size_t mock_id;
// };
@SuppressWarnings("restriction")
class MockIdFieldInserter {

   private static final CPPNodeFactory         nodeFactory = CPPNodeFactory.getDefault();
   private final ClassPublicVisibilityInserter inserter;

   public MockIdFieldInserter(ClassPublicVisibilityInserter inserter) {
      this.inserter = inserter;
   }

   public void insert(boolean hasMockIdField, boolean hasOnlyStaticMemFuns) {
      if (!hasMockIdField && !hasOnlyStaticMemFuns) {
         IASTSimpleDeclaration mockIdField = createMockIdField();
         insertMockIdField(mockIdField);
      }
   }

   private static IASTSimpleDeclaration createMockIdField() {
      ICPPASTNamedTypeSpecifier constIntSpecifier = createConstSizeTSpecifier();
      IASTSimpleDeclaration simpleDecl = nodeFactory.newSimpleDeclaration(constIntSpecifier);
      IASTName mockIdName = nodeFactory.newName(MOCK_ID.toCharArray());
      simpleDecl.addDeclarator(nodeFactory.newDeclarator(mockIdName));
      return simpleDecl;
   }

   private static ICPPASTNamedTypeSpecifier createConstSizeTSpecifier() {
      IASTName name = nodeFactory.newName(SIZE_T.toCharArray());
      ICPPASTNamedTypeSpecifier sizeT = nodeFactory.newTypedefNameSpecifier(name);
      sizeT.setConst(true);
      return sizeT;
   }

   private void insertMockIdField(IASTSimpleDeclaration mockIdField) {
      inserter.insert(mockIdField);
   }
}
