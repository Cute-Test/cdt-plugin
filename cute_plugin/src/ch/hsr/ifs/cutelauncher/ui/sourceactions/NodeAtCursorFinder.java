package ch.hsr.ifs.cutelauncher.ui.sourceactions;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

public class NodeAtCursorFinder extends ASTVisitor {
	int selOffset,dist=Integer.MAX_VALUE;
	boolean bounded=false;
	private IASTNode node;
	int spread=Integer.MAX_VALUE;
	{
		shouldVisitDeclarations=true;//simple declaration, template declaration
		shouldVisitStatements=true;//expressionstmt
	}
	public NodeAtCursorFinder(int offset){
		selOffset=offset;
		
		MessageConsole console = new MessageConsole("My Console", null);
		console.activate();
		ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[]{ console });
		stream = console.newMessageStream();
		
	}
	MessageConsoleStream stream;
	@Override
	public int leave(IASTDeclaration declaration) {
		int nodeOffset = declaration.getFileLocation().getNodeOffset();
		int nodeLength = declaration.getFileLocation().asFileLocation().getNodeLength();
		if(selOffset > nodeOffset && selOffset < (nodeOffset+ nodeLength) && dist>selOffset-nodeOffset) {
			bounded=true;
			setNode(declaration);
			dist=selOffset-nodeOffset;
		}
			
		return super.leave(declaration);
	}
	@Override
	public int leave(IASTStatement statement) {
		int nodeOffset = statement.getFileLocation().getNodeOffset();
		int nodeLength = statement.getFileLocation().asFileLocation().getNodeLength();
		if(selOffset > nodeOffset && selOffset < (nodeOffset+ nodeLength) && dist>selOffset-nodeOffset) {
			bounded=true;
			setNode(statement);
			dist=selOffset-nodeOffset;
		}
		
		return super.leave(statement);
	}
	public boolean getBounded(){return bounded;}
	//retrieve the node at the current cursor location
	int count=0;
	void setNode(IASTNode node) {
		this.node = node;
		count+=1;
		
		stream.println(node.toString());
		
	}
	public int get(){return count;}
	public IASTNode getNode() {
		return node;
	}
	
	}
/*
select any class with operator()

or any functioncallexp and resolve it 

or any line with it 
*/