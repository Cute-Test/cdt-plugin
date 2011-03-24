/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.ui.sourceactions;

import java.util.ArrayList;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;

public class FunctionFinder extends ASTVisitor {
	private ArrayList<IASTDeclaration> al=new ArrayList<IASTDeclaration>();
	private ArrayList<IASTSimpleDeclaration> alSimpleDeclarationOnly=null;
	private ArrayList<IASTSimpleDeclaration> alClassStructOnly=null;
	private ArrayList<IASTSimpleDeclaration> alVariables=null;

	
	{
		shouldVisitDeclarations=true;//Visbility, SimpleDeclaration,TemplateDeclaration, Function Defn
	}
	
	@Override
	public int leave(IASTDeclaration declaration) {
		al.add(declaration);
		return super.leave(declaration);		
	}
	
	public ArrayList<IASTSimpleDeclaration> getSimpleDeclaration(){
		if(alSimpleDeclarationOnly == null){
			alSimpleDeclarationOnly=new ArrayList<IASTSimpleDeclaration>();
			for(IASTDeclaration i:al){
				if(i instanceof IASTSimpleDeclaration)alSimpleDeclarationOnly.add((IASTSimpleDeclaration)i);
			}
		}
		return alSimpleDeclarationOnly;
	}
	
	/**
	 * @since 4.0
	 */
	public ArrayList<IASTFunctionDefinition> getMemberFuntions(){
		ArrayList<IASTFunctionDefinition> defs = new ArrayList<IASTFunctionDefinition>();
		for (IASTDeclaration decl : al) {
			if (decl instanceof IASTFunctionDefinition) {
				IASTFunctionDefinition funcDef = (IASTFunctionDefinition) decl;
				IBinding funcBind = funcDef.getDeclarator().getName().resolveBinding();
				if(funcBind instanceof ICPPMethod) {
					defs.add(funcDef);
				}
			}
		}
		return defs;
	}
	
	//template class are also returned
	public ArrayList<IASTSimpleDeclaration> getClassStruct(){
		if(alClassStructOnly == null){
			alClassStructOnly=new ArrayList<IASTSimpleDeclaration>();
			alVariables=new ArrayList<IASTSimpleDeclaration>();
			ArrayList<IASTSimpleDeclaration> altmp=getSimpleDeclaration();
			
			for(IASTSimpleDeclaration i:altmp){
				IASTDeclSpecifier declspecifier=i.getDeclSpecifier();
				if(declspecifier != null && declspecifier instanceof ICPPASTCompositeTypeSpecifier){
					alClassStructOnly.add(i);
				}else
					alVariables.add(i);
			}
		}
		return alClassStructOnly;
	}
	public ArrayList<IASTSimpleDeclaration> getVariables(){
		getClassStruct();
		return alVariables;
	}
}
