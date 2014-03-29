package ch.hsr.ifs.mockator.plugin.refsupport.utils;

import static ch.hsr.ifs.mockator.plugin.base.maybe.Maybe.maybe;
import static ch.hsr.ifs.mockator.plugin.base.maybe.Maybe.none;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisibilityLabel;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPNodeFactory;

import ch.hsr.ifs.mockator.plugin.base.MockatorException;
import ch.hsr.ifs.mockator.plugin.base.maybe.Maybe;

@SuppressWarnings("restriction")
public class ClassPublicVisibilityInserter {
  private static final CPPNodeFactory nodeFactory = CPPNodeFactory.getDefault();
  private final ICPPASTCompositeTypeSpecifier targetClass;
  private final ASTRewrite rewriter;
  private Maybe<ICPPASTVisibilityLabel> publicVisibilityLabel;

  public ClassPublicVisibilityInserter(ICPPASTCompositeTypeSpecifier targetClass,
      ASTRewrite rewriter) {
    this.targetClass = targetClass;
    this.rewriter = rewriter;
    publicVisibilityLabel = none();
  }

  public void insert(IASTNode classMember) {
    if (isStruct()) {
      insertIntoStruct(classMember);
    } else if (isClass()) {
      insertIntoClass(classMember);
    } else
      throw new MockatorException("Union types are not supported");
  }

  private boolean isClass() {
    return targetClass.getKey() == ICPPASTCompositeTypeSpecifier.k_class;
  }

  private boolean isStruct() {
    return targetClass.getKey() == IASTCompositeTypeSpecifier.k_struct;
  }

  private void insertIntoClass(IASTNode classMember) {
    Maybe<ICPPASTVisibilityLabel> publicLabel = findPublicVisibilityLabel();

    if (isVisibilityLabelNeeded(publicLabel)) {
      publicLabel = createAndInsertPublicVisibilityLabel();
    } else if (publicVisibilityLabel.isSome()) {
      publicLabel = publicVisibilityLabel;
    }

    IASTNode otherLabel = findVisibilityLabelAfterPublic(publicLabel.get());
    rewriter.insertBefore(targetClass, otherLabel, classMember, null);
  }

  private boolean isVisibilityLabelNeeded(Maybe<ICPPASTVisibilityLabel> publicLabel) {
    return publicLabel.isNone() && publicVisibilityLabel.isNone();
  }

  private void insertIntoStruct(IASTNode classMember) {
    Maybe<ICPPASTVisibilityLabel> publicLabel = findPublicVisibilityLabel();

    if (publicLabel.isNone()) {
      IASTNode insertionPoint = null;
      IASTDeclaration[] classDecls = getDeclarationsInClass();

      if (isNonEmptyClass(classDecls)) {
        insertionPoint = classDecls[0];
      }

      rewriter.insertBefore(targetClass, insertionPoint, classMember.copy(), null);
    } else {
      IASTNode otherLabelAfterPublic = findVisibilityLabelAfterPublic(publicLabel.get());
      rewriter.insertBefore(targetClass, otherLabelAfterPublic, classMember.copy(), null);
    }
  }

  private static boolean isNonEmptyClass(IASTDeclaration[] classDecls) {
    return classDecls.length > 0;
  }

  private IASTDeclaration[] getDeclarationsInClass() {
    return AstUtil.getAllDeclarations(targetClass);
  }

  private IASTNode findVisibilityLabelAfterPublic(IASTNode label) {
    boolean found = false;

    for (IASTDeclaration d : getDeclarationsInClass()) {
      if (found)
        return d;

      if (d.equals(label)) {
        found = true;
      }
    }
    return null;
  }

  private Maybe<ICPPASTVisibilityLabel> createAndInsertPublicVisibilityLabel() {
    ICPPASTVisibilityLabel publicLabel =
        nodeFactory.newVisibilityLabel(ICPPASTVisibilityLabel.v_public);
    rewriter.insertBefore(targetClass, null, publicLabel, null);
    publicVisibilityLabel = maybe(publicLabel);
    return maybe(publicLabel);
  }

  private Maybe<ICPPASTVisibilityLabel> findPublicVisibilityLabel() {
    final NodeContainer<ICPPASTVisibilityLabel> container =
        new NodeContainer<ICPPASTVisibilityLabel>();
    targetClass.accept(new ASTVisitor() {
      {
        shouldVisitDeclarations = true;
      }

      @Override
      public int visit(IASTDeclaration declaration) {
        if (declaration instanceof ICPPASTVisibilityLabel) {
          ICPPASTVisibilityLabel label = (ICPPASTVisibilityLabel) declaration;

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
