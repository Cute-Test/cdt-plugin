package ch.hsr.ifs.mockator.plugin.extractinterface.transform;

import java.util.function.Consumer;

import org.eclipse.cdt.core.dom.ast.ASTNodeFactoryFactory;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNameSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNodeFactory;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;

import ch.hsr.ifs.iltis.cpp.ast.ASTUtil;

import ch.hsr.ifs.mockator.plugin.extractinterface.context.ExtractInterfaceContext;


public class PublicInheritanceAdder implements Consumer<ExtractInterfaceContext> {

   private static final ICPPNodeFactory nodeFactory = ASTNodeFactoryFactory.getDefaultCPPNodeFactory();

   @Override
   public void accept(final ExtractInterfaceContext context) {
      final ICPPASTName newInterfaceName = createNewInterfaceName(context);
      final ICPPASTCompositeTypeSpecifier clazz = context.getChosenClass();
      final ICPPASTBaseSpecifier baseSpecifier = createPublicBase(clazz, newInterfaceName);
      final ICPPASTCompositeTypeSpecifier newClass = addInterfaceAsBaseClass(clazz, baseSpecifier);
      replaceOldClassWithNew(context, clazz, newClass);
   }

   private static ICPPASTName createNewInterfaceName(final ExtractInterfaceContext context) {
      return nodeFactory.newName(context.getNewInterfaceName().toCharArray());
   }

   private static ICPPASTBaseSpecifier createPublicBase(final ICPPASTCompositeTypeSpecifier clazz, final ICPPASTName newInterfaceName) {
      final boolean nonVirtual = false;
      final int visibility = getInheritanceVisibility(clazz);
      return nodeFactory.newBaseSpecifier((ICPPASTNameSpecifier) newInterfaceName, visibility, nonVirtual);
   }

   private static int getInheritanceVisibility(final ICPPASTCompositeTypeSpecifier clazz) {
      final int noBaseSpecifier = 0;
      final int visibility = ASTUtil.isStructType(clazz) ? noBaseSpecifier : ICPPASTBaseSpecifier.v_public;
      return visibility;
   }

   private static ICPPASTCompositeTypeSpecifier addInterfaceAsBaseClass(final ICPPASTCompositeTypeSpecifier clazz, final ICPPASTBaseSpecifier base) {
      final ICPPASTCompositeTypeSpecifier copy = clazz.copy();
      copy.addBaseSpecifier(base);
      return copy;
   }

   private static void replaceOldClassWithNew(final ExtractInterfaceContext context, final ICPPASTCompositeTypeSpecifier oldClass,
            final ICPPASTCompositeTypeSpecifier newClass) {
      final IASTTranslationUnit tuOfChosenClass = context.getTuOfChosenClass();
      final ASTRewrite rewriter = context.getRewriterFor(tuOfChosenClass);
      rewriter.replace(oldClass, newClass, null);
   }
}
