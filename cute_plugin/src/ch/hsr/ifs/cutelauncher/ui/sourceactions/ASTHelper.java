package ch.hsr.ifs.cutelauncher.ui.sourceactions;

import java.util.ArrayList;

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisiblityLabel;

public class ASTHelper {
	public static String getClassStructName(IASTSimpleDeclaration simpleDeclaration){
		IASTDeclSpecifier declspecifier=simpleDeclaration.getDeclSpecifier();
		if(declspecifier != null && declspecifier instanceof ICPPASTCompositeTypeSpecifier){
			return ((ICPPASTCompositeTypeSpecifier)declspecifier).getName().toString();
		}
		return "";
	}
	public static ArrayList<IASTDeclaration> getConstructors(IASTSimpleDeclaration simpleDeclaration){
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
	public static boolean haveParameters(ArrayList<IASTDeclaration> al){
		
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
	
	public static ArrayList<IASTDeclaration> getPublicMethods(IASTSimpleDeclaration cppClass){
		ArrayList<IASTDeclaration> result=new ArrayList<IASTDeclaration>();
		
		IASTDeclSpecifier declspecifier=cppClass.getDeclSpecifier();
		if(declspecifier != null && declspecifier instanceof ICPPASTCompositeTypeSpecifier){
			ICPPASTCompositeTypeSpecifier cts=(ICPPASTCompositeTypeSpecifier)declspecifier;
			String className=cts.getName().toString();
			IASTDeclaration members[]=cts.getMembers();
			
			boolean ispublicVisibility=false;
			if(cts.getKey()==ICPPASTCompositeTypeSpecifier.k_struct)ispublicVisibility=true;
			else if(cts.getKey()==ICPPASTCompositeTypeSpecifier.k_class)ispublicVisibility=false;
			else{
				//TODO consider error handing
				return result;
			}
			
			for(int i=0;i<members.length;i++){
				if(members[i] instanceof ICPPASTVisiblityLabel){
					int visbility=((ICPPASTVisiblityLabel)members[i]).getVisibility();
					if(visbility==ICPPASTVisiblityLabel.v_public)ispublicVisibility=true;
					if(visbility==ICPPASTVisiblityLabel.v_private || visbility==ICPPASTVisiblityLabel.v_protected)
						ispublicVisibility=false;
					continue;
				}
				
				String methodName="";
				if(members[i] instanceof IASTSimpleDeclaration){
					IASTSimpleDeclaration simpleDeclaration1=(IASTSimpleDeclaration)members[i];
					IASTDeclarator declarator[]=simpleDeclaration1.getDeclarators();
					methodName=declarator[0].getName().toString();
					
				}else if(members[i] instanceof IASTFunctionDefinition){
					IASTFunctionDefinition funcdef=(IASTFunctionDefinition)members[i];
					IASTFunctionDeclarator funcdeclarator=funcdef.getDeclarator();
					methodName=funcdeclarator.getName().toString();
				}
				if(className.equals(methodName))continue;
				
				if(ispublicVisibility){
					result.add(members[i]);
				}
			}
		}
		return result;
	}
}
