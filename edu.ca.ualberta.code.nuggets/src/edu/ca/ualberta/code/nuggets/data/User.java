package edu.ca.ualberta.code.nuggets.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Gist user / dev Value Object
 */
public class User {

	private String username;
	private String picUrl;
	private int followers;
	private int following;
	private int starred;
	private List<Repository> repositories;

	public User() {
		followers = 0;
		following = 0;
		starred = 0;
		repositories = new ArrayList<Repository>();
	}

	public List<Repository> getRepositories() {
		return repositories;
	}

	public void setRepositories(List<Repository> repositories) {
		this.repositories = repositories;
	}

	public int getStarred() {
		return starred;
	}

	public int getTotalRepoStars() {
		int total = 0;
		for (Repository r : repositories) {
			total += r.getStars();
		}

		return total;
	}

	public void setStarred(int starred) {
		this.starred = starred;
	}

	public int getFollowers() {
		return followers;
	}

	public void setFollowers(int followers) {
		this.followers = followers;
	}

	public int getFollowing() {
		return following;
	}

	public void setFollowing(int following) {
		this.following = following;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPicUrl() {
		return picUrl;
	}

	public void setPicUrl(String picUrl) {
		this.picUrl = picUrl;
	}

	@Override
	public String toString() {
		return username + " (Fe: " + followers + " | Fi: " + following + " | *: " + starred + " | Repos: " + repositories.size() + ")";
	}


}
