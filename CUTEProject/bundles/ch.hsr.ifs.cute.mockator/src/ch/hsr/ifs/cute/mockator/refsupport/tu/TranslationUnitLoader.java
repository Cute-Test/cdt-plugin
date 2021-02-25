package ch.hsr.ifs.cute.mockator.refsupport.tu;

import static org.eclipse.cdt.core.model.ITranslationUnit.AST_CONFIGURE_USING_SOURCE_CONTEXT;
import static org.eclipse.cdt.core.model.ITranslationUnit.AST_SKIP_INDEXED_HEADERS;

import java.net.URI;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.CoreModelUtil;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import ch.hsr.ifs.iltis.core.exception.ILTISException;

import ch.hsr.ifs.iltis.cpp.core.wrappers.CRefactoringContext;


public class TranslationUnitLoader {

    private static final int       AST_FLAGS = AST_CONFIGURE_USING_SOURCE_CONTEXT | AST_SKIP_INDEXED_HEADERS;
    private final ICProject        cProject;
    private final IProgressMonitor pm;
    private CRefactoringContext    context;
    private IIndex                 index;

    public TranslationUnitLoader(final ICProject cProject, final CRefactoringContext context, final IProgressMonitor pm) {
        this.cProject = cProject;
        this.context = context;
        this.pm = pm;
    }

    public TranslationUnitLoader(final ICProject cProject, final IIndex index, final IProgressMonitor pm) {
        this.cProject = cProject;
        this.index = index;
        this.pm = pm;
    }

    public IASTTranslationUnit loadAst(final IFile file) throws CoreException {
        return loadAst(file.getLocationURI());
    }

    public IASTTranslationUnit loadAst(final IIndexName iName) throws CoreException {
        return loadAst(getURI(iName));
    }

    private static URI getURI(final IIndexName iName) throws CoreException {
        return iName.getFile().getLocation().getURI();
    }

    private IASTTranslationUnit loadAst(final URI uri) throws CoreException, CModelException {
        // findTranslationUnitForLocation is also able to deliver translation
        // units for external header files like time.h
        ITranslationUnit tu = CoreModelUtil.findTranslationUnitForLocation(uri, cProject);

        if (tu == null) {
            tu = CoreModel.getDefault().createTranslationUnitFrom(cProject, uri);
        }
        final Object object = tu;

        ILTISException.Unless.notNull("Was not able to determine translation unit for " + uri.getPath(), object);
        return loadAst(tu);
    }

    private IASTTranslationUnit loadAst(final ITranslationUnit tu) throws CoreException {
        if (context != null) {
            return context.getAST(tu, pm);
        }

        return loadAstFromTu(tu);
    }

    private IASTTranslationUnit loadAstFromTu(final ITranslationUnit tu) {
        try {
            return tu.getAST(index, AST_FLAGS);
        } catch (final CoreException e) {
            throw new ILTISException(e).rethrowUnchecked();
        }
    }
}
