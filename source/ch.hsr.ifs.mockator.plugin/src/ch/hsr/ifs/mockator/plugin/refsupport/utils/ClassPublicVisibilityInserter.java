package ch.hsr.ifs.mockator.plugin.refsupport.utils;

import java.util.Optional;

import org.eclipse.cdt.core.dom.ast.ASTNodeFactoryFactory;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisibilityLabel;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNodeFactory;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;

import ch.hsr.ifs.iltis.core.exception.ILTISException;


public class ClassPublicVisibilityInserter {

   private static final ICPPNodeFactory        nodeFactory = ASTNodeFactoryFactory.getDefaultCPPNodeFactory();
   private final ICPPASTCompositeTypeSpecifier targetClass;
   private final ASTRewrite                    rewriter;
   private Optional<ICPPASTVisibilityLabel>    publicVisibilityLabel;

   public ClassPublicVisibilityInserter(final ICPPASTCompositeTypeSpecifier targetClass, final ASTRewrite rewriter) {
      this.targetClass = targetClass;
      this.rewriter = rewriter;
      publicVisibilityLabel = Optional.empty();
   }

   public void insert(final IASTNode classMember) {
      if (isStruct()) {
         insertIntoStruct(classMember);
      } else if (isClass()) {
         insertIntoClass(classMember);
      } else {
         throw new ILTISException("Union types are not supported").rethrowUnchecked();
      }
   }

   private boolean isClass() {
      return targetClass.getKey() == ICPPASTCompositeTypeSpecifier.k_class;
   }

   private boolean isStruct() {
      return targetClass.getKey() == IASTCompositeTypeSpecifier.k_struct;
   }

   private void insertIntoClass(final IASTNode classMember) {
      Optional<ICPPASTVisibilityLabel> publicLabel = findPublicVisibilityLabel();

      if (isVisibilityLabelNeeded(publicLabel)) {
         publicLabel = createAndInsertPublicVisibilityLabel();
      } else if (publicVisibilityLabel.isPresent()) {
         publicLabel = publicVisibilityLabel;
      }

      final IASTNode otherLabel = findVisibilityLabelAfterPublic(publicLabel.get());
      rewriter.insertBefore(targetClass, otherLabel, classMember, null);
   }

   private boolean isVisibilityLabelNeeded(final Optional<ICPPASTVisibilityLabel> publicLabel) {
      return !publicLabel.isPresent() && !publicVisibilityLabel.isPresent();
   }

   private void insertIntoStruct(final IASTNode classMember) {
      final Optional<ICPPASTVisibilityLabel> publicLabel = findPublicVisibilityLabel();

      if (!publicLabel.isPresent()) {
         IASTNode insertionPoint = null;
         final IASTDeclaration[] classDecls = getDeclarationsInClass();

         if (isNonEmptyClass(classDecls)) {
            insertionPoint = classDecls[0];
         }

         rewriter.insertBefore(targetClass, insertionPoint, classMember.copy(), null);
      } else {
         final IASTNode otherLabelAfterPublic = findVisibilityLabelAfterPublic(publicLabel.get());
         rewriter.insertBefore(targetClass, otherLabelAfterPublic, classMember.copy(), null);
      }
   }

   private static boolean isNonEmptyClass(final IASTDeclaration[] classDecls) {
      return classDecls.length > 0;
   }

   private IASTDeclaration[] getDeclarationsInClass() {
      return AstUtil.getAllDeclarations(targetClass);
   }

   private IASTNode findVisibilityLabelAfterPublic(final IASTNode label) {
      boolean found = false;

      for (final IASTDeclaration d : getDeclarationsInClass()) {
         if (found) { return d; }

         if (d.equals(label)) {
            found = true;
         }
      }
      return null;
   }

   private Optional<ICPPASTVisibilityLabel> createAndInsertPublicVisibilityLabel() {
      final ICPPASTVisibilityLabel publicLabel = nodeFactory.newVisibilityLabel(ICPPASTVisibilityLabel.v_public);
      rewriter.insertBefore(targetClass, null, publicLabel, null);
      return publicVisibilityLabel = Optional.of(publicLabel);
   }

   private Optional<ICPPASTVisibilityLabel> findPublicVisibilityLabel() {
      final NodeContainer<ICPPASTVisibilityLabel> container = new NodeContainer<>();
      targetClass.accept(new ASTVisitor() {

         {
            shouldVisitDeclarations = true;
         }

         @Override
         public int visit(final IASTDeclaration declaration) {
            if (declaration instanceof ICPPASTVisibilityLabel) {
               final ICPPASTVisibilityLabel label = (ICPPASTVisibilityLabel) declaration;

               if (label.getVisibility() == ICPPASTVisibilityLabel.v_public) {
                  container.setNode(label);
                  return PROCESS_ABORT;
               }
            }
            return PROCESS_CONTINUE;
         }
      });

      return container.getNode();
   }
}
