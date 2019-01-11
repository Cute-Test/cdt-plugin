/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.ui.sourceactions;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.CoreModelUtil;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.model.ASTCache;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoring;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoringContext;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.text.edits.MultiTextEdit;


public abstract class AbstractFunctionAction extends CRefactoring {

    public AbstractFunctionAction(ICElement element, ISelection selection, ICProject project) {
        super(element, selection, project);
    }

    public AbstractFunctionAction() {
        this(null, null, null);
    }

    /**
     * @since 4.0
     */
    public abstract MultiTextEdit createEdit(IFile file, IDocument doc, ISelection selection) throws CoreException;

    protected IASTTranslationUnit acquireAST(IFile editorFile) throws CoreException, InterruptedException {
        ITranslationUnit tu = CoreModelUtil.findTranslationUnit(editorFile);
        IASTTranslationUnit ast = refactoringContext.getAST(tu, new NullProgressMonitor());
        return ast;
    }

    protected void initContext() {
        new CRefactoringContext(this);
    }

    protected void disposeContext() {
        refactoringContext.dispose();
    }

    protected IASTTranslationUnit getASTTranslationUnit(IFile editorFile) throws CoreException {
        final ASTCache astCache = new ASTCache();
        ITranslationUnit tu = CoreModelUtil.findTranslationUnit(editorFile);
        IIndex index = CCorePlugin.getIndexManager().getIndex(tu.getCProject());
        try {
            index.acquireReadLock();
            return astCache.acquireSharedAST(tu, index, true, new NullProgressMonitor());
        } catch (InterruptedException e) {
            return null;
        } finally {
            astCache.disposeAST();
            index.releaseReadLock();
        }
    }

    public void createProblemMarker(IFile file, String message, int lineNo) {

        try {
            IMarker marker = file.createMarker("org.eclipse.cdt.core.problem");
            marker.setAttribute(IMarker.MESSAGE, "cute:" + message);
            marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
            marker.setAttribute(IMarker.TRANSIENT, true);
            if (lineNo != 0) {
                marker.setAttribute(IMarker.LINE_NUMBER, lineNo);
            }
            marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
        } catch (CoreException e) {
            // You need to handle the cases where attribute value is rejected
        }
    }

    @Override
    protected RefactoringDescriptor getRefactoringDescriptor() {
        return null;
    }

    @Override
    protected void collectModifications(IProgressMonitor pm, ModificationCollector collector) throws CoreException, OperationCanceledException {

    }

}
