package edu.ca.ualberta.code.nuggets.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import edu.ca.ualberta.code.nuggets.MainController;

/**
 * This class represents the command pattern for eclipse plugins to launch
 * the search dialog for query based snippet insertion.
 */
public class OpenSearchDialogCommand extends AbstractHandler{

	private MainController controller;

	public OpenSearchDialogCommand(){
		controller = MainController.getInstance();
	}

	 @Override
	  public Object execute(ExecutionEvent arg0) throws ExecutionException {
		 controller.openEmptySearchDialog();
		 return null;
	  }

}
