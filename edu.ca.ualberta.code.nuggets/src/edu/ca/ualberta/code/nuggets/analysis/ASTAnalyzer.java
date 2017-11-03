package edu.ca.ualberta.code.nuggets.analysis;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;

/**
 * This class encapsulates a group of methods that use a javaparser
 * in order to create visitors that query the AST of a given Java Class.
 * @author Victor
 */
public class ASTAnalyzer {

	//*****************************************************************************************
	// Visitor Controllers
	//*****************************************************************************************
	
	/**
	 * Extracts the list of methods names corresponding to the java class
	 * currently open on the editor. It uses the ClassMethodNameVisitor to
	 * visit the AST of a class, and getClassContex to find the current class context.
	 * @return List of context class method names.
	 */
	public ArrayList<String> extractClassMethodsInformation(){

		ArrayList<String> methodNames = new ArrayList<>();
		String filepath= getClassContex();
		if(filepath!=null){
			try {
				FileInputStream in = new FileInputStream(filepath);
				CompilationUnit cu = JavaParser.parse(in);
				ClassMethodNameVisitor mv = new ClassMethodNameVisitor();
				mv.visit(cu, methodNames);
				in.close();
			} catch(Exception e){
				e.printStackTrace();
			}
		}
		return methodNames;
	}

	/**
	 * Extracts the list of local variable types names corresponding to the
	 * method where the cursor is standing inside a class currently open on 
	 * the editor. It uses the MethodByLineTypesVisitor to visit the AST of 
	 * a class, and the getLOCContex to obtain the context LOC.
	 * @return Set of context method local variable types.
	 */
	public Set<String> extractMethodLocalTypes(){

		Set<String> localTypes = new HashSet<String>();
		String filepath= getClassContex();
		if(filepath!=null && !filepath.isEmpty()){
			try {
				FileInputStream in = new FileInputStream(filepath);
				CompilationUnit cu = JavaParser.parse(in);
				MethodByLineTypesVisitor mv = new MethodByLineTypesVisitor();
				mv.setContextLOC(getLOCContex());
				mv.visit(cu, localTypes);
				in.close();
			} catch(Exception e){
				e.printStackTrace();
			}
		}
		return localTypes;
	}


	//*****************************************************************************************
	// Context Obtainer Methods
	//*****************************************************************************************
	
	/**
	 * Uses the workbench to find the path of the currently open file on the editor.
	 * @return a qualified path of the currently open file, empty string if none is selected.
	 */
	public static String getClassContex(){
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow window =  workbench == null ? null : workbench.getActiveWorkbenchWindow();
		IWorkbenchPage activePage =  window == null ? null : window.getActivePage();
		IEditorPart editor =  activePage == null ? null : activePage.getActiveEditor();
		IEditorInput input = editor == null ? null : editor.getEditorInput();
		IPath path = input instanceof FileEditorInput  ? ((FileEditorInput)input).getPath()	: null;

		if (path != null)
		{	
			return path.toString();
		}
		return "None";

	}

	/**
	 * Uses the workbench to find the line of code (LOC) where the cursor is standing on the editor.
	 * @return the line of code where the cursor is standing (in an open file), 0 if none is open.
	 */
	public int getLOCContex(){

		IWorkbench wb = PlatformUI.getWorkbench();
		IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
		IWorkbenchPage page = win.getActivePage();
		IEditorPart editor = page.getActiveEditor();

		if(editor instanceof ITextEditor){
			ISelectionProvider selectionProvider = ((ITextEditor)editor).getSelectionProvider();
			ISelection selection = selectionProvider.getSelection();
			if (selection instanceof ITextSelection) {
				ITextSelection textSelection = (ITextSelection)selection;
				return textSelection.getStartLine();
			}
		}
		return 0;
	}
}

