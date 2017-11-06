package ch.hsr.ifs.mockator.plugin.extractinterface.transform;

import java.util.function.Consumer;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPNodeFactory;

import ch.hsr.ifs.mockator.plugin.extractinterface.context.ExtractInterfaceContext;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;

@SuppressWarnings("restriction")
public class PublicInheritanceAdder implements Consumer<ExtractInterfaceContext> {

  private static final CPPNodeFactory nodeFactory = CPPNodeFactory.getDefault();

  @Override
  public void accept(final ExtractInterfaceContext context) {
    final IASTName newInterfaceName = createNewInterfaceName(context);
    final ICPPASTCompositeTypeSpecifier klass = context.getChosenClass();
    final ICPPASTBaseSpecifier baseSpecifier = createPublicBase(klass, newInterfaceName);
    final ICPPASTCompositeTypeSpecifier newClass = addInterfaceAsBaseClass(klass, baseSpecifier);
    replaceOldClassWithNew(context, klass, newClass);
  }

  private static IASTName createNewInterfaceName(final ExtractInterfaceContext context) {
    return nodeFactory.newName(context.getNewInterfaceName().toCharArray());
  }

  private static ICPPASTBaseSpecifier createPublicBase(final ICPPASTCompositeTypeSpecifier klass, final IASTName newInterfaceName) {
    final boolean nonVirtual = false;
    final int visibility = getInheritanceVisibility(klass);
    return nodeFactory.newBaseSpecifier(newInterfaceName, visibility, nonVirtual);
  }

  private static int getInheritanceVisibility(final ICPPASTCompositeTypeSpecifier klass) {
    final int noBaseSpecifier = 0;
    final int visibility = AstUtil.isStructType(klass) ? noBaseSpecifier : ICPPASTBaseSpecifier.v_public;
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
