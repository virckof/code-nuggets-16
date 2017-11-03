package edu.ca.ualberta.code.nuggets.analysis;

import java.util.ArrayList;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public class ClassMethodNameVisitor extends VoidVisitorAdapter {

	/**
	 * This method is called recursively traversing the AST nodes.
	 * It uses accumulation of parameters using a empty list as
	 * initial input to collect the name of the nodes that are of
	 * MethodDeclaration type.
	 */
    @Override
    public void visit(MethodDeclaration n, Object arg) {
    	ArrayList<String> methodNames  = (ArrayList<String>) arg;
    	methodNames.add(n.getName());
    }
}