package edu.ca.ualberta.code.nuggets.data;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.runtime.IProgressMonitor;
import edu.ca.ualberta.code.nuggets.crawl.GistParser;

public class GistSnippetProvider implements ISnippetProvider{

	private GistParser gist;

	public GistSnippetProvider(){
		gist = new GistParser();
	}

	@Override
	public void saveSnippet(String s, ArrayList<String> tags) {

	}

	@Override
	public void deleteSnippet(int id) {

	}

	@Override
	public GistResultSet getSnippets(String language, String[] keywords, int page, IProgressMonitor monitor) {

		return getSnippets(language, keywords, page, 1, monitor);

	}

	@Override
	public GistResultSet getSnippets(String language, String[] keywords, int page, int numPages, IProgressMonitor monitor) {
		List<Snippet> snippets = new ArrayList<Snippet>();
		int totalResults = 0;

		for (int i = page; i < page + numPages; i++) {
			String query= gist.buildURLQuery(language, keywords, i);
			ArrayList<String> hits = gist.parseGistQuery(query, monitor);
			GistResultSet pageResults = gist.getResultSnippets(hits, monitor);

			if (pageResults == null || pageResults.getTotalResults() == 0) {
				monitor.worked((page + numPages - i) * (10 + 5));
				break;
			}

			snippets.addAll(pageResults.getSnippets());

			if (numPages == 1) {
				totalResults = pageResults.getTotalResults();
			} else {
				totalResults += pageResults.getSnippets().size();
			}
		}

		GistResultSet result = new GistResultSet(totalResults, snippets);

		return result;
	}

}
