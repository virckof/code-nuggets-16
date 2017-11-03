package edu.ca.ualberta.code.nuggets.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Gist result set Value Object
 */
public class GistResultSet {

	private int totalResults;
	private List<Snippet> snippets;

	public GistResultSet(int total, List<Snippet> snippets) {
		this.totalResults = total;
		this.snippets = snippets;
	}

	public int getTotalResults() {
		return totalResults;
	}

	public void setTotalResults(int totalResults) {
		this.totalResults = totalResults;
	}

	public List<Snippet> getSnippets() {
		return snippets;
	}

	public void setSnippets(ArrayList<Snippet> snippets) {
		this.snippets = snippets;
	}

}
