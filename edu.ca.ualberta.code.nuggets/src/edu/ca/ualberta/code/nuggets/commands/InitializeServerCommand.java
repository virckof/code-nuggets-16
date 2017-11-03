package edu.ca.ualberta.code.nuggets.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import edu.ca.ualberta.code.nuggets.MainController;

/**
 * This class represents the command pattern for eclipse plugins to activate the
 * interaction server to push user behaviours to the backed.
 */
public class InitializeServerCommand extends AbstractHandler{

	private MainController controller;

	public InitializeServerCommand(){
		controller = MainController.getInstance();
	}

	 @Override
	  public Object execute(ExecutionEvent arg0) throws ExecutionException {
		 controller.launchInteractionsServer();
		 return null;
	  }

}
