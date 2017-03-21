package ch.hsr.ifs.cute.tdd;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.corext.util.CModelUtil;
import org.eclipse.cdt.internal.ui.editor.ASTProvider;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoring;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoringContext;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

public class TddRefactoringContext extends CRefactoringContext {
    //@formatter:off
    private static final int PARSE_MODE =
            ITranslationUnit.AST_SKIP_ALL_HEADERS |
            ITranslationUnit.AST_CONFIGURE_USING_SOURCE_CONTEXT |
            ITranslationUnit.AST_PARSE_INACTIVE_CODE;

    private static final int INDEX_MODE =
            IIndexManager.ADD_EXTENSION_FRAGMENTS_EDITOR |
            IIndexManager.ADD_DEPENDENCIES |
            IIndexManager.ADD_DEPENDENT;
    //@formatter:on

    private final Map<ITranslationUnit, IASTTranslationUnit> fASTCache;
    private IIndex fIndex;
    private IASTTranslationUnit fSharedAST;

    public TddRefactoringContext(CRefactoring refactoring) {
        super(refactoring);
        refactoring.setContext(this);
        fASTCache = new ConcurrentHashMap<>();
    }

    public IASTTranslationUnit getAST(ITranslationUnit translationUnit, IProgressMonitor monitor)
            throws CoreException, OperationCanceledException {
        guardDisposed();
        getIndex();

        if (monitor != null && monitor.isCanceled())
            throw new OperationCanceledException();

        translationUnit = CModelUtil.toWorkingCopy(translationUnit);
        IASTTranslationUnit ast = fASTCache.get(translationUnit);

        if (ast == null) {
            if (fSharedAST != null && translationUnit.equals(fSharedAST.getOriginatingTranslationUnit())) {
                ast = fSharedAST;
            } else {
                ast = ASTProvider.getASTProvider().acquireSharedAST(translationUnit, fIndex,
                        ASTProvider.WAIT_ACTIVE_ONLY, monitor);

                if (ast != null && ast.hasNodesOmitted()) {
                    ASTProvider.getASTProvider().releaseSharedAST(ast);
                    ast = null;
                }

                if (ast == null) {
                    if (monitor != null && monitor.isCanceled()) {
                        throw new OperationCanceledException();
                    }
                    ast = translationUnit.getAST(fIndex, PARSE_MODE);
                    fASTCache.put(translationUnit, ast);
                } else {
                    if (fSharedAST != null) {
                        ASTProvider.getASTProvider().releaseSharedAST(fSharedAST);
                    }
                    fSharedAST = ast;
                }
            }
        }

        if (monitor != null) {
            monitor.done();
        }

        return ast;
    }

    private void guardDisposed() {
        if (isDisposed())
            throw new IllegalStateException("TddRefactoringContext is already disposed.");
    }

    @Override
    public void dispose() {
        guardDisposed();
        if (fSharedAST != null) {
            ASTProvider.getASTProvider().releaseSharedAST(fSharedAST);
        }
        if (fIndex != null) {
            fIndex.releaseReadLock();
        }
        super.dispose();
    }

    private boolean isDisposed() {
        return getRefactoring() == null;
    }

    @Override
    protected void finalize() throws Throwable {
        if (!isDisposed())
            CUIPlugin.logError("TddRefactoringContext was not disposed");
        super.finalize();
    }

    @Override
    public IIndex getIndex() throws CoreException, OperationCanceledException {
        guardDisposed();

        if (fIndex == null) {
            ITranslationUnit translationUnit = ((CRefactoring)getRefactoring()).getTranslationUnit();
            IProject project = translationUnit.getFile().getProject();
            ICProject cproject = CoreModel.getDefault().create(project);
            IIndex index = CCorePlugin.getIndexManager().getIndex(cproject, INDEX_MODE);
            try {
                index.acquireReadLock();
            } catch (InterruptedException e) {
                throw new OperationCanceledException();
            }
            fIndex = index;
        }
        return (IIndex) fIndex;
    }
}