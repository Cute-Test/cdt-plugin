package ch.hsr.ifs.cutelauncher.ui.sourceactions;

import java.util.ArrayList;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeNodeContentProvider;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
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
				
				MultiTextEdit mEdit =Dialog(astTu, editorFile,doc,ff, withoutTemplate, classStructInstances);
				return mEdit;
		
								
			}
		}

		return new MultiTextEdit();
	}

	//filter for just public non static without parameters class
	public MultiTextEdit Dialog(IASTTranslationUnit astTu,IFile editorFile,IDocument doc,
			FunctionFinder ff, ArrayList<IASTSimpleDeclaration> classStruct, ArrayList<IASTSimpleDeclaration> classStructInstances){
		
		LabelProvider lp=new LabelProvider(); 
		myTree wcp=new myTree(ff, classStruct, classStructInstances);
				
		ElementTreeSelectionDialog etsd=new myETSD(new Shell(CuteLauncherPlugin.getDisplay()),lp,wcp);
		etsd.setTitle("Select Method to add to suite");
		etsd.setAllowMultiple(false);
		etsd.setBlockOnOpen(true);
		etsd.setInput(wcp.root);
		
		etsd.create();
		Button button=etsd.getOkButton();
		button.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				MessageConsoleStream stream=EclipseConsole.getConsole();
				stream.println("selected");
				
			}
		});
		
		boolean allowToClose=false;
		while(allowToClose==false){
			int status=etsd.open();
			if(status==ElementTreeSelectionDialog.OK){
				Object selectedObject=etsd.getFirstResult();
				
				if(selectedObject instanceof Container)continue;
				allowToClose=true;
				break;
			}
			if(status==ElementTreeSelectionDialog.CANCEL){
				allowToClose=true;
				break;
			}
		}
		
		Object selectedObject=etsd.getFirstResult();
		Method child=(Method)selectedObject;
		Container parent=child.getParent();
		
		MessageConsoleStream stream=EclipseConsole.getConsole();
		stream.println("selected:"+parent.toString()+"."+child.toString()+"()");
		
		//parent.toString()+"."+child.toString()+"()";
		
		

		SuitePushBackFinder suitPushBackFinder = new SuitePushBackFinder();
		astTu.accept(suitPushBackFinder);
		
		//TODO modify checkNameExist for detecting name with classes
			MultiTextEdit mEdit = new MultiTextEdit();
			
			String newLine = TextUtilities.getDefaultLineDelimiter(doc);
			StringBuilder builder = new StringBuilder();
			builder.append(newLine);
			builder.append("\t");
			IASTName name = suitPushBackFinder.getSuiteDeclName();//XXX
			builder.append(name.toString());
			builder.append(".push_back(");
			
			if(parent.isInstance==Container.InstanceType){
				builder.append("CUTE_MEMFUN("+parent.toString()+","+parent.classTypeName+","+child.toString()+")");
			}
			if(parent.isInstance==Container.ClassType){
				builder.append("CUTE_SMEMFUN("+parent.toString()+","+child.toString()+")");
			}
			builder.append(");");
			
			mEdit.addChild(createPushBackEdit(editorFile, doc, astTu,
					suitPushBackFinder,builder));
			return mEdit;
		
		/*	s.push_back(CUTE_SMEMFUN(aStruct,helo));
	s.push_back(CUTE_MEMFUN(thatStruct,aStruct,helo));*/
		
	}
}

class myETSD extends ElementTreeSelectionDialog{
    
	public myETSD(Shell parent,
            ILabelProvider labelProvider, ITreeContentProvider contentProvider){
		super(parent,labelProvider,contentProvider);
	}
		
	// @see SelectionStatusDialog#updateButtonsEnableState
    @Override
	protected void updateButtonsEnableState(IStatus status) {
        Button okButton = getOkButton();
        Object selectedObject=getFirstResult();
        
        if (okButton != null && !okButton.isDisposed() && !(selectedObject instanceof Container)) {
			okButton.setEnabled(!status.matches(IStatus.ERROR));
		}
        if(selectedObject instanceof Container){
        	okButton.setEnabled(false);
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
			ArrayList<IASTDeclaration> nonStaticMethods=ASTHelper.getNonStaticMethods(publicMethods);
			ArrayList<IASTDeclaration> removedParameters=ASTHelper.getParameterlessMethods(nonStaticMethods);
			ArrayList<IASTDeclaration> removedVoid=ASTHelper.getVoidMethods(removedParameters);
			ArrayList<IASTDeclaration> removedUnion=ASTHelper.removeUnion(removedVoid);
			
			Container c=new Container(i,Container.ClassType);
			for(IASTDeclaration j:removedUnion){
				Method method=new Method(c,j);
				c.add(method);
			}
			if(!ASTHelper.isUnion(i))
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
							
				ArrayList<IASTDeclaration> publicMethods=ASTHelper.getVoidMethods(ASTHelper.getParameterlessMethods(ASTHelper.getNonStaticMethods(ASTHelper.getPublicMethods(targetType))));
				
				Container c=new Container(i,Container.InstanceType,ASTHelper.getClassStructName(targetType));
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
	public static final boolean InstanceType=true; 
	public static final boolean ClassType=false;
	
	public IASTSimpleDeclaration simpleDeclaration; 
	public ArrayList<Method> methods=new ArrayList<Method>();
	public final boolean isInstance;
	public String classTypeName="";
	
	public Container(IASTSimpleDeclaration i,boolean isInstance){
		simpleDeclaration = i;
		this.isInstance=isInstance;
	}
	public Container(IASTSimpleDeclaration i,boolean isInstance, String classTypeName){
		this(i,isInstance);
		this.classTypeName=classTypeName;
	}
	
	
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

/*
 * foo operator() int d 
shouldnt be shown

remove operator()

duplicate entry detection
 * 
 * */
