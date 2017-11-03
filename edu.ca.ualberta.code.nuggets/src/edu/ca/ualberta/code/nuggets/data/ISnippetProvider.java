package edu.ca.ualberta.code.nuggets.data;

import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Snippet provider for the plugin, in the future this my be implemented by
 * classes to query platforms such as stack overflow or bitbucket.
 */
public interface ISnippetProvider {

	public GistResultSet getSnippets(String language, String[] keywords, int page, int numPage, IProgressMonitor monitor);
	public GistResultSet getSnippets(String language, String[] keywords, int page, IProgressMonitor monitor);

	public void saveSnippet(String s, ArrayList<String> tags);

	public void deleteSnippet(int id);

}
