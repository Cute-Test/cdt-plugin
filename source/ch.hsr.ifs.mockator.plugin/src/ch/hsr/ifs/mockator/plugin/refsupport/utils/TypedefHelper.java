package ch.hsr.ifs.mockator.plugin.refsupport.utils;

import static ch.hsr.ifs.mockator.plugin.base.maybe.Maybe.maybe;

import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.ExpansionOverlapsBoundaryException;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeSelector;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.internal.core.model.ExternalTranslationUnit;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.content.IContentType;

import ch.hsr.ifs.mockator.plugin.base.maybe.Maybe;

// Taken and adapted from ds8
@SuppressWarnings("restriction")
public class TypedefHelper {
  private static final IContentType HEADER_CONTENT_TYPE = CCorePlugin.getContentType("header.h");
  private IType type;

  public TypedefHelper(IType type) {
    if (type instanceof ITypedef) {
      this.type = ((ITypedef) type).getType();
    } else {
      this.type = type;
    }
  }

  private String filterFilePartOfName(String typename) {
	  return typename.replaceFirst("\\{.*\\}::", "");
  }
  
  public String findShortestType() throws CoreException {
    Set<IType> typedefs = findTypedefs(type);
    IType candidate = getTypeCandidate(typedefs, type);

    if (candidate instanceof ITypedef && candidate instanceof ICPPBinding)
      return AstUtil.getQfName((ICPPBinding) candidate);

    return filterFilePartOfName(ASTTypeUtil.getType(candidate));
  }

  private static IType getTypeCandidate(Set<IType> typedefs, IType type) {
    IType candidate = type;

    for (IType each : typedefs) {
      if (getTypeLength(candidate) > getTypeLength(each)) {
        candidate = each;
      }
    }

    return candidate;
  }

  private static int getTypeLength(IType type) {
    return ASTTypeUtil.getType(type, false).length();
  }

  private Set<IType> findTypedefs(IType type) throws CoreException {
    if (!(type instanceof IBinding))
      return Collections.emptySet();

    IIndex index = getIndex();
    try {
      index.acquireReadLock();
      IIndexName[] indexNames =
          index.findNames((IBinding) type, IIndex.FIND_REFERENCES
              | IIndex.SEARCH_ACROSS_LANGUAGE_BOUNDARIES);
      return resolveTypedefs(index, indexNames);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    } finally {
      index.releaseReadLock();
    }

    return Collections.emptySet();
  }

  private static IIndex getIndex() throws CoreException, CModelException {
    return CCorePlugin.getIndexManager().getIndex(getProjects());
  }

  private static ICProject[] getProjects() throws CModelException {
    return CoreModel.getDefault().getCModel().getCProjects();
  }

  private Set<IType> resolveTypedefs(IIndex index, IIndexName[] indexNames) throws CoreException {
    Set<IType> typedefs = new HashSet<IType>();

    for (IIndexName indexName : indexNames) {
      IASTNode occurrence = findNode(index, indexName);
      IASTSimpleDeclaration typedef = findContainingTypedef(occurrence);

      if (typedef != null) {
        for (IASTDeclarator each : typedef.getDeclarators()) {
          IType candidate = (IType) each.getName().resolveBinding();
          typedefs.add(candidate);
        }
      }
    }
    return typedefs;
  }

  private IASTSimpleDeclaration findContainingTypedef(IASTNode node) {
    return findContainingTypedef(node, 2);
  }

  private IASTSimpleDeclaration findContainingTypedef(IASTNode node, int recursionsLeft) {
    if (node == null)
      return null;

    IToken syntax = getSyntax(node);

    if (syntax != null && syntax.getImage().equals("typedef"))
      return (IASTSimpleDeclaration) node.getParent();

    return recursionsLeft > 0 ? findContainingTypedef(node.getParent(), recursionsLeft - 1) : null;
  }

  private static IToken getSyntax(IASTNode node) {
    try {
      return node.getSyntax();
    } catch (ExpansionOverlapsBoundaryException e) {
      return null;
    }
  }

  private static IASTNode findNode(IIndex index, IIndexName name) throws CoreException {
    IASTFileLocation location = name.getFileLocation();
    IContentType contentType = getContentType(location.getFileName());

    if (isHeader(contentType)) {
      String contentId = contentType.getId();
      URI uri = URIUtil.toURI(location.getFileName());
      int offset = location.getNodeOffset();
      int length = location.getNodeLength();
      for (IASTTranslationUnit optAst : getAst(index, contentId, uri)) {
        IASTNodeSelector nodeSelector = optAst.getNodeSelector(null);
        return nodeSelector.findFirstContainedNode(offset, length);
      }
    }

    return null;
  }

  private static Maybe<IASTTranslationUnit> getAst(IIndex index, String contentId, URI uri)
      throws CModelException, CoreException {
    ICProject cProject = getProjects()[0];
    ITranslationUnit tu = new ExternalTranslationUnit(cProject, uri, contentId);
    return maybe(tu.getAST(index, ITranslationUnit.AST_SKIP_INDEXED_HEADERS));
  }

  private static IContentType getContentType(String fileName) {
    IContentType contentType = CCorePlugin.getContentType(fileName);
    // use header type as default because getContentType does not know
    // content types of certain includes like iosfwd and *.tcc
    return contentType != null ? contentType : HEADER_CONTENT_TYPE;
  }

  private static boolean isHeader(IContentType contentType) {
    return contentType.getId().equals(HEADER_CONTENT_TYPE.getId());
  }
}
