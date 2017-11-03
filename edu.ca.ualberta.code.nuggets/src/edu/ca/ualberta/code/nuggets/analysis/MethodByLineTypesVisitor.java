package edu.ca.ualberta.code.nuggets.analysis;

import java.util.ArrayList;
import java.util.Set;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public class MethodByLineTypesVisitor  extends VoidVisitorAdapter {

	private int contextLOC;
	private boolean parseTypesInsideMethod;


	/**
	 * This method is called recursively traversing the AST nodes.
	 * It locates the method defined within the boundaries of the given
	 * LOC context. The VariableDeclarationExpr is called to explore the
	 * nodes inside of the MethodDeclaration of interest.
	 */
    public void visit(MethodDeclaration n, Object arg) {
    	Set<String> localTypes  = (Set<String>) arg;

    	if(contextLOC >= n.getBeginLine()-1 && contextLOC <= n.getEndLine()){
    		parseTypesInsideMethod=true;
    		visit(n.getBody(), localTypes);
    		parseTypesInsideMethod=false;
    	}

    }

    /**
	 * This method is called recursively traversing the AST nodes.
	 * It uses accumulation of parameters using a empty list as
	 * initial input to collect the types of the nodes that are of
	 * VariableDeclarationExpr type.
	 */
	public void visit(VariableDeclarationExpr  n, Object arg) {
		Set<String> localTypes  = (Set<String>) arg;
		if(!parseTypesInsideMethod)
            return;

		String type = n.getType().toString();
		if(!isStopType(type)){
			localTypes.add(type);
		}


    }

	/**
	 * Checks if a given type is in the list of stop-type words
	 * @param type
	 * @return
	 */
	private boolean isStopType(String type) {

		//TODO: This needs to be refactored probably using an enum, or something more robust.
		if(type.equals("String")||type.equals("int")||type.equals("double")||type.equals("float")||type.contains("ArrayList")){
			return true;
		}
		return false;
	}

	public int getContextLOC() {
		return contextLOC;
	}

	public void setContextLOC(int contextLOC) {
		this.contextLOC = contextLOC;
	}
}
