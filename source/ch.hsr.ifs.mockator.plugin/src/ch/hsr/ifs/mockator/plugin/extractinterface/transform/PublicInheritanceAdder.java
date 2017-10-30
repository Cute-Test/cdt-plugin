package ch.hsr.ifs.mockator.plugin.extractinterface.transform;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPNodeFactory;

import ch.hsr.ifs.mockator.plugin.base.functional.F1V;
import ch.hsr.ifs.mockator.plugin.extractinterface.context.ExtractInterfaceContext;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;


@SuppressWarnings("restriction")
public class PublicInheritanceAdder implements F1V<ExtractInterfaceContext> {

   private static final CPPNodeFactory nodeFactory = CPPNodeFactory.getDefault();

   @Override
   public void apply(ExtractInterfaceContext context) {
      IASTName newInterfaceName = createNewInterfaceName(context);
      ICPPASTCompositeTypeSpecifier klass = context.getChosenClass();
      ICPPASTBaseSpecifier baseSpecifier = createPublicBase(klass, newInterfaceName);
      ICPPASTCompositeTypeSpecifier newClass = addInterfaceAsBaseClass(klass, baseSpecifier);
      replaceOldClassWithNew(context, klass, newClass);
   }

   private static IASTName createNewInterfaceName(ExtractInterfaceContext context) {
      return nodeFactory.newName(context.getNewInterfaceName().toCharArray());
   }

   private static ICPPASTBaseSpecifier createPublicBase(ICPPASTCompositeTypeSpecifier klass, IASTName newInterfaceName) {
      final boolean nonVirtual = false;
      int visibility = getInheritanceVisibility(klass);
      return nodeFactory.newBaseSpecifier(newInterfaceName, visibility, nonVirtual);
   }

   private static int getInheritanceVisibility(ICPPASTCompositeTypeSpecifier klass) {
      final int noBaseSpecifier = 0;
      int visibility = AstUtil.isStructType(klass) ? noBaseSpecifier : ICPPASTBaseSpecifier.v_public;
      return visibility;
   }

   private static ICPPASTCompositeTypeSpecifier addInterfaceAsBaseClass(ICPPASTCompositeTypeSpecifier klass, ICPPASTBaseSpecifier base) {
      ICPPASTCompositeTypeSpecifier copy = klass.copy();
      copy.addBaseSpecifier(base);
      return copy;
   }

   private static void replaceOldClassWithNew(ExtractInterfaceContext context, ICPPASTCompositeTypeSpecifier oldClass,
         ICPPASTCompositeTypeSpecifier newClass) {
      IASTTranslationUnit tuOfChosenClass = context.getTuOfChosenClass();
      ASTRewrite rewriter = context.getRewriterFor(tuOfChosenClass);
      rewriter.replace(oldClass, newClass, null);
   }
}
