package ch.hsr.ifs.cutelauncher.ui.sourceactions;

import java.util.ArrayList;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;

public class FunctionFinder extends ASTVisitor {
	ArrayList<IASTDeclaration> al=new ArrayList<IASTDeclaration>();
	ArrayList<IASTSimpleDeclaration> alSimpleDeclarationOnly=new ArrayList<IASTSimpleDeclaration>();
	boolean parseForSimpleDeclaration=false;
	ArrayList<IASTSimpleDeclaration> alClassStructOnly=new ArrayList<IASTSimpleDeclaration>();
	boolean parseClassStructOnly=false;
	
	{
		shouldVisitDeclarations=true;//Visbility, SimpleDeclaration,TemplateDeclaration, Function Defn
	}
	
	@Override
	public int leave(IASTDeclaration declaration) {
		al.add(declaration);
		return super.leave(declaration);		
	}
	public ArrayList getAL(){return al;}
	
	public ArrayList<IASTSimpleDeclaration> getSimpleDeclaration(){
		if(!parseForSimpleDeclaration){
			for(IASTDeclaration i:al){
				if(i instanceof IASTSimpleDeclaration)alSimpleDeclarationOnly.add((IASTSimpleDeclaration)i);
			}
			parseForSimpleDeclaration=true;
		}
		return alSimpleDeclarationOnly;
	}
	
	public ArrayList<IASTSimpleDeclaration> getClassStruct(){
		if(!parseClassStructOnly){
			ArrayList<IASTSimpleDeclaration> altmp=getSimpleDeclaration();
			
			for(IASTSimpleDeclaration i:altmp){
				IASTDeclSpecifier declspecifier=i.getDeclSpecifier();
				if(declspecifier != null && declspecifier instanceof ICPPASTCompositeTypeSpecifier){
					alClassStructOnly.add(i);
				}
			}
			
			parseClassStructOnly=true;
		}
		return alClassStructOnly;
	}
	
	//FIXME extract this method out
	public String getSimpleDeclarationNodeName(IASTSimpleDeclaration simpleDeclaration){
		IASTDeclSpecifier declspecifier=simpleDeclaration.getDeclSpecifier();
		if(declspecifier != null && declspecifier instanceof ICPPASTCompositeTypeSpecifier){
			return ((ICPPASTCompositeTypeSpecifier)declspecifier).getName().toString();
		}
		return "";
	}
	public ArrayList<IASTDeclaration> getConstructors(IASTSimpleDeclaration simpleDeclaration){
		ArrayList<IASTDeclaration> result=new ArrayList<IASTDeclaration>();
		
		IASTDeclSpecifier declspecifier=simpleDeclaration.getDeclSpecifier();
		if(declspecifier != null && declspecifier instanceof ICPPASTCompositeTypeSpecifier){
			ICPPASTCompositeTypeSpecifier cts=(ICPPASTCompositeTypeSpecifier)declspecifier;
			String className=cts.getName().toString();
			IASTDeclaration members[]=cts.getMembers();
			for(int i=0;i<members.length;i++){
				if(members[i] instanceof IASTFunctionDefinition){
					IASTFunctionDefinition fd=(IASTFunctionDefinition)members[i];
					IASTFunctionDeclarator fdd=fd.getDeclarator();
					String fname=fdd.getName().toString();
					if(fname.equals(className))result.add(fd);
				}else if(members[i] instanceof IASTSimpleDeclaration){
					IASTSimpleDeclaration sd=(IASTSimpleDeclaration)members[i];
					IASTDeclarator sdd[]=sd.getDeclarators();
					if(sdd.length==0)continue;
					String sname=sdd[0].getName().toString();
					if(sname.equals(className))result.add(sd);
				}
			}
		}
		return result;
	}
	public boolean haveParameters(ArrayList<IASTDeclaration> al){
		
		for(IASTDeclaration i:al){
			if(i instanceof IASTFunctionDefinition){
				IASTFunctionDefinition fd=(IASTFunctionDefinition)i;
				ICPPASTFunctionDeclarator fdd=(ICPPASTFunctionDeclarator)fd.getDeclarator();
				IASTParameterDeclaration fpara[]=fdd.getParameters();
				if(fdd.takesVarArgs() ||fpara.length>0) return true;				
			}else if(i instanceof IASTSimpleDeclaration){
				IASTSimpleDeclaration sd=(IASTSimpleDeclaration)i;
				IASTDeclarator sdd[]=sd.getDeclarators();
				
				for(int j=0;j<sdd.length;j++){
					ICPPASTFunctionDeclarator fd=(ICPPASTFunctionDeclarator)sdd[j];
					IASTParameterDeclaration fpara[]=fd.getParameters();
					if(fd.takesVarArgs() || fpara!=null && fpara.length>0) return true;
				}
			}
		}
		return false;
	}
}
