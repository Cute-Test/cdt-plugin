package ch.hsr.ifs.cute.ui.sourceactions;

import java.util.ArrayList;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IDocument;

public abstract class AddMemberFunctionBaseStrategy extends AddPushbackStatementStrategy {

	protected static final String PARAMETERS_REQUIRED = "pArAmEtRs_ReQuIrEd";
	protected final IFile editorFile;

	public AddMemberFunctionBaseStrategy(IDocument doc, IASTTranslationUnit astTu, IFile editorFile, SuitePushBackFinder finder) {
		super(doc, astTu, finder);
		this.editorFile = editorFile;
	}

	protected boolean checkForConstructorWithParameters(IASTTranslationUnit astTu, IASTNode methodNode) {
		FunctionFinder functionFinder = new FunctionFinder();
		astTu.accept(functionFinder);
		for (IASTNode classStructNode : functionFinder.getClassStruct()) {
			if (classStructNode.contains(methodNode)) {
				ArrayList<IASTDeclaration> constructors = ASTHelper.getConstructors((IASTSimpleDeclaration) classStructNode);
				return ASTHelper.haveParameters(constructors);
			}
		}
		return false;
	}
}
