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

import ch.hsr.ifs.mockator.plugin.extractinterface.context.ExtractInterfaceContext;
import ch.hsr.ifs.iltis.cpp.ast.ASTUtil;

public class PublicInheritanceAdder implements Consumer<ExtractInterfaceContext> {

   private static final ICPPNodeFactory nodeFactory = ASTNodeFactoryFactory.getDefaultCPPNodeFactory();

   @Override
   public void accept(final ExtractInterfaceContext context) {
      final ICPPASTName newInterfaceName = createNewInterfaceName(context);
      final ICPPASTCompositeTypeSpecifier klass = context.getChosenClass();
      final ICPPASTBaseSpecifier baseSpecifier = createPublicBase(klass, newInterfaceName);
      final ICPPASTCompositeTypeSpecifier newClass = addInterfaceAsBaseClass(klass, baseSpecifier);
      replaceOldClassWithNew(context, klass, newClass);
   }

   private static ICPPASTName createNewInterfaceName(final ExtractInterfaceContext context) {
      return nodeFactory.newName(context.getNewInterfaceName().toCharArray());
   }

   private static ICPPASTBaseSpecifier createPublicBase(final ICPPASTCompositeTypeSpecifier klass, final ICPPASTName newInterfaceName) {
      final boolean nonVirtual = false;
      final int visibility = getInheritanceVisibility(klass);
      return nodeFactory.newBaseSpecifier((ICPPASTNameSpecifier) newInterfaceName, visibility, nonVirtual);
   }

   private static int getInheritanceVisibility(final ICPPASTCompositeTypeSpecifier klass) {
      final int noBaseSpecifier = 0;
      final int visibility = ASTUtil.isStructType(klass) ? noBaseSpecifier : ICPPASTBaseSpecifier.v_public;
      return visibility;
   }

   private static ICPPASTCompositeTypeSpecifier addInterfaceAsBaseClass(final ICPPASTCompositeTypeSpecifier klass, final ICPPASTBaseSpecifier base) {
      final ICPPASTCompositeTypeSpecifier copy = klass.copy();
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
