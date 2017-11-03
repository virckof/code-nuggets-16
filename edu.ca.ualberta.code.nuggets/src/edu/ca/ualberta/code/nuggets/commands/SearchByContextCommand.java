package edu.ca.ualberta.code.nuggets.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import edu.ca.ualberta.code.nuggets.MainController;

/**
 * This class represents the command pattern for eclipse plugins to launch
 * the search based on AST type analysis
 */
public class SearchByContextCommand extends AbstractHandler{

	private MainController controller;

	public SearchByContextCommand(){
		controller = MainController.getInstance();
	}

	 @Override
	  public Object execute(ExecutionEvent arg0) throws ExecutionException {

		controller.openSearchDialogMethodTypeContext();

		 return null;
	  }

}
