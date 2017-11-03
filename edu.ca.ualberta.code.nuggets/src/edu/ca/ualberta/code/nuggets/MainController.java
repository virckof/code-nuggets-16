package edu.ca.ualberta.code.nuggets;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import edu.ca.ualberta.code.nuggets.data.GistResultSet;
import edu.ca.ualberta.code.nuggets.data.GistSnippetProvider;
import edu.ca.ualberta.code.nuggets.data.ISnippetProvider;
import edu.ca.ualberta.code.nuggets.data.Snippet;
import edu.ca.ualberta.code.nuggets.dialogs.FindSnippetDialog;
import edu.ca.ualberta.code.nuggets.interactions.InteractionsServer;

/**
 * Main entry point of the plugin
 */
public class MainController {

	/**
	 * Controls if the plugin sends or not feedback
	 */
	public static final boolean FEEDBACK_ACTIVE = true;

	/**
	 * Main controller of the plugin
	 */
	private static MainController instance = null;

	/**
	 * Snippet provider implementation
	 */
	private ISnippetProvider provider;

	/**
	 * GUI main context
	 */
	private IWorkbenchWindow window;

	private InteractionsServer server;

	/**
	 * Constructor (Singleton)
	 */
	private MainController (){
		provider = new GistSnippetProvider();
		window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

	}

	public void launchInteractionsServer(){
		server = new InteractionsServer();
		new Thread(server).start();
	}

	public static synchronized MainController getInstance() {
        if (instance == null) {
            instance = new MainController();
        }
        return instance;
    }

	/**
	 * Returns the snippet of code selected in the current active editor.
	 * @return
	 */
	private String getSelectedText(){
		String snippet = "";
		try{
			EditorPart part = (EditorPart) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();

			if ( part instanceof ITextEditor ) {
		        final ITextEditor editor = (ITextEditor)part;
		        ISelection sel = editor.getSelectionProvider().getSelection();
		        if ( sel instanceof TextSelection ) {

		            snippet = ((TextSelection) sel).getText();

		        }
		    }
		}
		catch(Exception e){
			//Silent
		}

		return snippet;
	}




	/**
	 * Inserts a snippet in the current position of the caret
	 * @param snippet
	 */
	public void insertSnippet(Snippet snippet){

		if(!snippet.getSnippet().isEmpty()){
			try{
				EditorPart part = (EditorPart) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();

				if ( part instanceof ITextEditor ) {
			        final ITextEditor editor = (ITextEditor)part;
			        IDocumentProvider prov = editor.getDocumentProvider();
			        IDocument doc = prov.getDocument( editor.getEditorInput() );
			        ISelection sel = editor.getSelectionProvider().getSelection();
			        if ( sel instanceof TextSelection ) {
			            final TextSelection textSel = (TextSelection)sel;
			            SimpleDateFormat ft = new SimpleDateFormat ("yyyy-MM-dd");
			            String insertion =
			            "\n"+
			            "// Snippet by " + snippet.getUser().getUsername() + " ("+ ft.format(snippet.getDate())+")" + "\n"			            + snippet.getSnippet()+ "\n" +
			            "// -- \n"+
			            "\n";

			            doc.replace( textSel.getOffset(), textSel.getLength(), insertion );
			        }
			    }
			}
			catch(Exception e){
				//Silent
			}
		}

	}

	/**
	 * Opens the save dialog, user inputs the tags and confirms the creation of a snippet.
	 */
	public void openSaveSnippetDialog(){

		//TODO OLD REMOVE
		/*SaveSnippetDialog dialog = new SaveSnippetDialog(window.getShell(), getSelectedSnippet());
		dialog.create();
		if (dialog.open() == Window.OK) {

		  saveSnippet(dialog.getCode(), dialog.getTags());
		} */
	}

	/**
	 * Opens an empty snippet search dialog
	 */
	public void openEmptySearchDialog(){

		FindSnippetDialog dialog = new FindSnippetDialog(window.getShell(), this);
		dialog.create();
		dialog.open();
	}


	/**
	 * Opens a snippet search dialog and loads the results of a query built using the
	 * complex types of the current method (if the file is a .java file)
	 */
	public void openSearchDialogMethodTypeContext(){
		FindSnippetDialog dialog = new FindSnippetDialog(window.getShell(), this);
		dialog.create();
		boolean anyResults = dialog.addResultsByMethodLocalTypesContext();
		if(anyResults){
			dialog.open();
		}
		else{
			dialog.close();
		}

	}



	public GistResultSet getSnippets(String languaje, String[] keywords, int page, IProgressMonitor monitor) {

		return provider.getSnippets(languaje, keywords, page, 1, monitor);
	}

	public GistResultSet getSnippets(String languaje, String[] keywords, int page, int numPages, IProgressMonitor monitor) {

		return provider.getSnippets(languaje, keywords, page, numPages, monitor);
	}

}
