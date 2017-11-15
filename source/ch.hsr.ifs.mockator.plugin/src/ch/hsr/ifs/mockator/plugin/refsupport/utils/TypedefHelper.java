package ch.hsr.ifs.mockator.plugin.refsupport.utils;

import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.ExpansionOverlapsBoundaryException;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTNode;
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

import ch.hsr.ifs.iltis.core.functional.OptHelper;
import ch.hsr.ifs.iltis.cpp.ast.ASTUtil;


// Taken and adapted from ds8
public class TypedefHelper {

   private static final IContentType HEADER_CONTENT_TYPE = CCorePlugin.getContentType("header.h");
   private IType                     type;

   public TypedefHelper(final IType type) {
      if (type instanceof ITypedef) {
         this.type = ((ITypedef) type).getType();
      } else {
         this.type = type;
      }
   }

   private String filterFilePartOfName(final String typename) {
      return typename.replaceFirst("\\{.*\\}::", "");
   }

   public String findShortestType() throws CoreException {
      final Set<IType> typedefs = findTypedefs(type);
      final IType candidate = getTypeCandidate(typedefs, type);

      if (candidate instanceof ITypedef && candidate instanceof ICPPBinding) { return ASTUtil.getQfName((ICPPBinding) candidate); }

      return filterFilePartOfName(ASTTypeUtil.getType(candidate));
   }

   private static IType getTypeCandidate(final Set<IType> typedefs, final IType type) {
      IType candidate = type;

      for (final IType each : typedefs) {
         if (getTypeLength(candidate) > getTypeLength(each)) {
            candidate = each;
         }
      }

      return candidate;
   }

   private static int getTypeLength(final IType type) {
      return ASTTypeUtil.getType(type, false).length();
   }

   private Set<IType> findTypedefs(final IType type) throws CoreException {
      if (!(type instanceof IBinding)) { return Collections.emptySet(); }

      final IIndex index = getIndex();
      try {
         index.acquireReadLock();
         final IIndexName[] indexNames = index.findNames((IBinding) type, IIndex.FIND_REFERENCES | IIndex.SEARCH_ACROSS_LANGUAGE_BOUNDARIES);
         return resolveTypedefs(index, indexNames);
      }
      catch (final InterruptedException e) {
         Thread.currentThread().interrupt();
      }
      finally {
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

   private Set<IType> resolveTypedefs(final IIndex index, final IIndexName[] indexNames) throws CoreException {
      final Set<IType> typedefs = new HashSet<>();

      for (final IIndexName indexName : indexNames) {
         final IASTNode occurrence = findNode(index, indexName);
         final IASTSimpleDeclaration typedef = findContainingTypedef(occurrence);

         if (typedef != null) {
            for (final IASTDeclarator each : typedef.getDeclarators()) {
               final IType candidate = (IType) each.getName().resolveBinding();
               typedefs.add(candidate);
            }
         }
      }
      return typedefs;
   }

   private IASTSimpleDeclaration findContainingTypedef(final IASTNode node) {
      return findContainingTypedef(node, 2);
   }

   private IASTSimpleDeclaration findContainingTypedef(final IASTNode node, final int recursionsLeft) {
      if (node == null) { return null; }

      final IToken syntax = getSyntax(node);

      if (syntax != null && syntax.getImage().equals("typedef")) { return (IASTSimpleDeclaration) node.getParent(); }

      return recursionsLeft > 0 ? findContainingTypedef(node.getParent(), recursionsLeft - 1) : null;
   }

   private static IToken getSyntax(final IASTNode node) {
      try {
         return node.getSyntax();
      }
      catch (final ExpansionOverlapsBoundaryException e) {
         return null;
      }
   }

   private static IASTNode findNode(final IIndex index, final IIndexName name) throws CoreException {
      final IASTFileLocation location = name.getFileLocation();
      final IContentType contentType = getContentType(location.getFileName());

      if (isHeader(contentType)) {
         final String contentId = contentType.getId();
         final URI uri = URIUtil.toURI(location.getFileName());
         return OptHelper.returnIfPresentElseNull(getAst(index, contentId, uri), (ast) -> ast.getNodeSelector(null).findFirstContainedNode(location
               .getNodeOffset(), location.getNodeLength()));
      }

      return null;
   }

   private static Optional<IASTTranslationUnit> getAst(final IIndex index, final String contentId, final URI uri) throws CModelException,
   CoreException {
      final ICProject cProject = getProjects()[0];
      final ITranslationUnit tu = new ExternalTranslationUnit(cProject, uri, contentId);
      return Optional.of(tu.getAST(index, ITranslationUnit.AST_SKIP_INDEXED_HEADERS));
   }

   private static IContentType getContentType(final String fileName) {
      final IContentType contentType = CCorePlugin.getContentType(fileName);
      // use header type as default because getContentType does not know
      // content types of certain includes like iosfwd and *.tcc
      return contentType != null ? contentType : HEADER_CONTENT_TYPE;
   }

   private static boolean isHeader(final IContentType contentType) {
      return contentType.getId().equals(HEADER_CONTENT_TYPE.getId());
   }
}
