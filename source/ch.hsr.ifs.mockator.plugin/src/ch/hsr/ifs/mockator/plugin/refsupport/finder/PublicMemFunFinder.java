package ch.hsr.ifs.mockator.plugin.refsupport.finder;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;

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

import ch.hsr.ifs.mockator.plugin.base.MockatorException;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;

public class PublicMemFunFinder {
  private final ICPPASTCompositeTypeSpecifier klass;
  private final Set<Types> typesToConsider;
  private boolean publicVisibility;

  public enum Types {
    withCtors, withDtors, withStatics
  }

  public static final EnumSet<Types> ALL_TYPES = EnumSet.allOf(Types.class);

  public PublicMemFunFinder(ICPPASTCompositeTypeSpecifier klass, Set<Types> typesToConsider) {
    this.klass = klass;
    this.typesToConsider = typesToConsider;
    initVisibility();
  }

  private void initVisibility() {
    if (AstUtil.isStructType(klass)) {
      publicVisibility = true;
    } else if (AstUtil.isClassType(klass)) {
      publicVisibility = false;
    } else
      throw new MockatorException("Union types not supported");
  }

  public List<IASTDeclaration> getPublicMemFuns() {
    List<IASTDeclaration> publicMemFuns = list();

    for (IASTDeclaration classMember : klass.getMembers()) {
      if (classMember instanceof ICPPASTVisibilityLabel) {
        publicVisibility = isPublic((ICPPASTVisibilityLabel) classMember);
        continue;
      }

      if (!publicVisibility) {
        continue;
      }

      IASTDeclarator declarator = AstUtil.getDeclaratorForNode(classMember);

      if (!(declarator instanceof ICPPASTFunctionDeclarator)) {
        continue;
      }

      ICPPASTFunctionDeclarator funDecl = (ICPPASTFunctionDeclarator) declarator;
      IASTName memFunName = funDecl.getName();
      ICPPASTDeclSpecifier funSpec = AstUtil.getDeclSpec(funDecl);

      if (funSpec == null || isFriend(funSpec) || ignoreStatic(funSpec) || ignoreCtor(memFunName)
          || ignoreDtor(memFunName)) {
        continue;
      }

      publicMemFuns.add(classMember);
    }

    return publicMemFuns;
  }

  private static boolean isFriend(ICPPASTDeclSpecifier funDeclSpec) {
    return funDeclSpec.isFriend();
  }

  private boolean ignoreStatic(ICPPASTDeclSpecifier funDeclSpec) {
    return AstUtil.isStatic(funDeclSpec) && !typesToConsider.contains(Types.withStatics);
  }

  private boolean ignoreDtor(IASTName memFunName) {
    return isDtor(memFunName) && !typesToConsider.contains(Types.withDtors);
  }

  private static boolean isDtor(IASTName memFunName) {
    IBinding binding = memFunName.resolveBinding();
    return binding instanceof ICPPMethod && ((ICPPMethod) binding).isDestructor();
  }

  private boolean ignoreCtor(IASTName memFunName) {
    return isCtor(memFunName) && !typesToConsider.contains(Types.withCtors);
  }

  private static boolean isCtor(IASTName memFunName) {
    IBinding binding = memFunName.resolveBinding();
    return binding instanceof ICPPConstructor;
  }

  private static boolean isPublic(ICPPASTVisibilityLabel node) {
    return node.getVisibility() == ICPPASTVisibilityLabel.v_public;
  }
}
