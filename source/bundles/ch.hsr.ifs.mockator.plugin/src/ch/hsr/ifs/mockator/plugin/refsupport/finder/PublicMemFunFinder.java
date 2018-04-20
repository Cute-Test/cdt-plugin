package ch.hsr.ifs.mockator.plugin.refsupport.finder;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisibilityLabel;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;

import ch.hsr.ifs.iltis.core.core.exception.ILTISException;
import ch.hsr.ifs.iltis.cpp.core.ast.ASTUtil;


public class PublicMemFunFinder {

   private final ICPPASTCompositeTypeSpecifier clazz;
   private final Set<Types>                    typesToConsider;
   private boolean                             publicVisibility;

   public enum Types {
      withCtors, withDtors, withStatics
   }

   public static final EnumSet<Types> ALL_TYPES = EnumSet.allOf(Types.class);

   public PublicMemFunFinder(final ICPPASTCompositeTypeSpecifier clazz, final Set<Types> typesToConsider) {
      this.clazz = clazz;
      this.typesToConsider = typesToConsider;
      initVisibility();
   }

   private void initVisibility() {
      if (ASTUtil.isStructType(clazz)) {
         publicVisibility = true;
      } else if (ASTUtil.isClassType(clazz)) {
         publicVisibility = false;
      } else throw new ILTISException("Union types not supported").rethrowUnchecked();
   }

   public List<IASTDeclaration> getPublicMemFuns() {
      final List<IASTDeclaration> publicMemFuns = new ArrayList<>();

      for (final IASTDeclaration classMember : clazz.getMembers()) {
         if (classMember instanceof ICPPASTVisibilityLabel) {
            publicVisibility = isPublic((ICPPASTVisibilityLabel) classMember);
            continue;
         }

         if (!publicVisibility) {
            continue;
         }

         final IASTDeclarator declarator = ASTUtil.getDeclaratorForNode(classMember);

         if (!(declarator instanceof ICPPASTFunctionDeclarator)) {
            continue;
         }

         final ICPPASTFunctionDeclarator funDecl = (ICPPASTFunctionDeclarator) declarator;
         final IASTName memFunName = funDecl.getName();
         final ICPPASTDeclSpecifier funSpec = ASTUtil.getDeclSpec(funDecl);

         if (funSpec == null || isFriend(funSpec) || ignoreStatic(funSpec) || ignoreCtor(memFunName) || ignoreDtor(memFunName)) {
            continue;
         }

         publicMemFuns.add(classMember);
      }

      return publicMemFuns;
   }

   private static boolean isFriend(final ICPPASTDeclSpecifier funDeclSpec) {
      return funDeclSpec.isFriend();
   }

   private boolean ignoreStatic(final ICPPASTDeclSpecifier funDeclSpec) {
      return ASTUtil.isStatic(funDeclSpec) && !typesToConsider.contains(Types.withStatics);
   }

   private boolean ignoreDtor(final IASTName memFunName) {
      return isDtor(memFunName) && !typesToConsider.contains(Types.withDtors);
   }

   private static boolean isDtor(final IASTName memFunName) {
      final IBinding binding = memFunName.resolveBinding();
      return binding instanceof ICPPMethod && ((ICPPMethod) binding).isDestructor();
   }

   private boolean ignoreCtor(final IASTName memFunName) {
      return isCtor(memFunName) && !typesToConsider.contains(Types.withCtors);
   }

   private static boolean isCtor(final IASTName memFunName) {
      final IBinding binding = memFunName.resolveBinding();
      return binding instanceof ICPPConstructor;
   }

   private static boolean isPublic(final ICPPASTVisibilityLabel node) {
      return node.getVisibility() == ICPPASTVisibilityLabel.v_public;
   }
}
