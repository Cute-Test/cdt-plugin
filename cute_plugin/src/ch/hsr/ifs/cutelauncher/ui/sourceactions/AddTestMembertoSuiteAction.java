package ch.hsr.ifs.cutelauncher.ui.sourceactions;

import java.util.ArrayList;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeNodeContentProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.FileEditorInput;

import ch.hsr.ifs.cutelauncher.CuteLauncherPlugin;
import ch.hsr.ifs.cutelauncher.EclipseConsole;

public class AddTestMembertoSuiteAction extends AbstractFunctionAction {

	@Override
	public MultiTextEdit createEdit(TextEditor ceditor,
			IEditorInput editorInput, IDocument doc, String funcName)
			throws CoreException {
		
		ISelection sel = ceditor.getSelectionProvider().getSelection();
		if (sel != null && sel instanceof TextSelection) {
			TextSelection selection = (TextSelection) sel;
			if (editorInput instanceof FileEditorInput) {
				IFile editorFile = ((FileEditorInput) editorInput).getFile();
				IASTTranslationUnit astTu = getASTTranslationUnit(editorFile);
				
				NodeAtCursorFinder n= new NodeAtCursorFinder(selection.getOffset());
				astTu.accept(n);
		
				MessageConsoleStream stream=EclipseConsole.getConsole();
				
				FunctionFinder ff=new FunctionFinder();
				astTu.accept(ff);
				ArrayList<IASTSimpleDeclaration> withoutTemplate =ASTHelper.removeTemplateClasses(ff.getClassStruct());
				ArrayList<IASTSimpleDeclaration> variablesList=ff.getVariables();
				ArrayList<IASTSimpleDeclaration> classStructInstances=ASTHelper.getClassStructVariables(variablesList);
				
				Dialog(ff, withoutTemplate, classStructInstances);
		
			}
		}

		return new MultiTextEdit();
	}

	public void Dialog(FunctionFinder ff, ArrayList<IASTSimpleDeclaration> classStruct, ArrayList<IASTSimpleDeclaration> classStructInstances){
		LabelProvider lp=new LabelProvider(); 
		myTree wcp=new myTree(ff, classStruct, classStructInstances);
				
		ElementTreeSelectionDialog etsd=new ElementTreeSelectionDialog(new Shell(CuteLauncherPlugin.getDisplay()),lp,wcp);
		etsd.setInput(wcp.root);
		etsd.setTitle("Select Method to add to suite");
		
		etsd.setBlockOnOpen(true);
		if(etsd.open()==ElementTreeSelectionDialog.OK){
			
		}
	}
}


class myTree extends TreeNodeContentProvider{
	
	public ArrayList<Container> containers=new ArrayList<Container>();
	public final Container root=new Container(null,true);
	
 	public myTree(	FunctionFinder ff, 
					ArrayList<IASTSimpleDeclaration> classStruct, 
					ArrayList<IASTSimpleDeclaration> classStructInstances){
		
		MessageConsoleStream stream=EclipseConsole.getConsole();
		
		for(IASTSimpleDeclaration i:classStruct){
			stream.println("class:"+ASTHelper.getClassStructName((i))+"");
			ArrayList<IASTDeclaration> publicMethods=ASTHelper.getPublicMethods(i);
			ArrayList<IASTDeclaration> staticMethods=ASTHelper.getStaticMethods(publicMethods);
			
			Container c=new Container(i,false);
			for(IASTDeclaration j:staticMethods){
				Method method=new Method(c,j);
				c.add(method);
			}
			containers.add(c);
		}
		
		for(IASTSimpleDeclaration i:classStructInstances){
			stream.println("instances:"+ASTHelper.getVariableName((i))+"");
			if(i.getDeclSpecifier() instanceof ICPPASTNamedTypeSpecifier){
				ICPPASTNamedTypeSpecifier namedSpecifier=(ICPPASTNamedTypeSpecifier)i.getDeclSpecifier();
				String typename=namedSpecifier.getName().toString();
			
				//resolve to type
				IASTSimpleDeclaration targetType=null;
				for(Container c:containers){
					if(ASTHelper.getClassStructName(c.simpleDeclaration).equals(typename)){
						targetType=c.simpleDeclaration;
					}
				}
				if(targetType==null)continue;
							
				ArrayList<IASTDeclaration> publicMethods=ASTHelper.getPublicMethods(targetType);
				
				Container c=new Container(i,true);
				for(IASTDeclaration j:publicMethods){
					Method method=new Method(c,j);
					c.add(method);
				}
				containers.add(c);
			}
		}
	}
	
	@Override
	public Object[] getElements(Object inputElement){
		return getChildren(inputElement);
	}
	
	@Override
	public Object[] getChildren(Object parentElement){
		Object[] result=new Object[0];
		if(parentElement==root){
			return containers.toArray();
		}else if(parentElement instanceof Container){
			Container container=(Container)parentElement;
			return container.methods.toArray();
		}
		return result;
	}
	@Override
	public boolean hasChildren(Object element){
		if(element instanceof Container){
			if(((Container)element).methods.size()>0)return true;
		}
		return false;
	}
	@Override
	public Object getParent(Object element){
		if(element==root)return root;
		if(element instanceof Container){return root;}
		if(element instanceof Method){return ((Method)element).getParent();}
		return "thisShouldntHappen";
	}
}

class Container {
	public IASTSimpleDeclaration simpleDeclaration; 
	public ArrayList<Method> methods=new ArrayList<Method>();
	public final boolean isInstance;
	
	public Container(IASTSimpleDeclaration i,boolean isInstance){simpleDeclaration = i;this.isInstance=isInstance;}
	public void add(Object element){methods.add((Method)element);}
	@Override
	public String toString(){
		if(isInstance){
			return ASTHelper.getVariableName(simpleDeclaration);
		}else 
			return ASTHelper.getClassStructName(simpleDeclaration);
	}
	
}
class Method{
	public IASTDeclaration declaration;
	Container container;
	
	public Method(Container c, IASTDeclaration i){container=c;declaration=i;}
	public Container getParent(){return container;}
	@Override
	public String toString(){return ASTHelper.getMethodName(declaration);}
}

// nested case of struct/class ?
