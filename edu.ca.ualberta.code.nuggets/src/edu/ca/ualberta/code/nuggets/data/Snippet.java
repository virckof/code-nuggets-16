package edu.ca.ualberta.code.nuggets.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

/**
 * Code snippet Value Object
 */
public class Snippet implements Serializable, Comparable<Snippet> {

	private static final long serialVersionUID = 1L;

	/**
	 * List of tags
	 */
	private ArrayList<String> tags;

	/**
	 * Captured code
	 */
	private String snippet;

	/**
	 * ID of the snippet, unique, auto-increment.
	 */
	private int id;

	/**
	 * ID of the snippet, from the source.
	 */
	private String sourceId;

	/**
	 * Creation date.
	 */
	private Date date;

	/**
	 * User information.
	 */
	private User user;

	public ArrayList<String> getTags() {
		return tags;
	}

	public void setTags(ArrayList<String> tags) {
		this.tags = tags;
	}

	public String getSnippet() {
		return snippet;
	}

	public void setSnippet(String snippet) {
		this.snippet = snippet;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String toString() {
		return id + " | " + sourceId + "\n" + user.getUsername() + " | " + date
				+ "\n" + snippet;
	}

	public String getSourceId() {
		return sourceId;
	}

	public void setSourceId(String id) {
		this.sourceId = id;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	@Override
	public int compareTo(Snippet otherSnippet) {
		int score = this.user.getFollowers() * 10 + this.user.getTotalRepoStars();
		int otherScore = otherSnippet.getUser().getFollowers() * 10	+ otherSnippet.getUser().getTotalRepoStars();

		if (score > otherScore) {
			return 1;
		} else if (score < otherScore) {
			return -1;
		}

		return 0;
	}
}
