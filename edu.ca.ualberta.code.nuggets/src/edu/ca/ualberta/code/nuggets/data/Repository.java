package edu.ca.ualberta.code.nuggets.data;


/**
 * Author repository Value Object
 */
public class Repository {

	private String name;
	private int stars;
	private int forks;

	public Repository(String name, int stars, int forks) {
		super();
		this.name = name;
		this.stars = stars;
		this.forks = forks;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getStars() {
		return stars;
	}

	public void setStars(int stars) {
		this.stars = stars;
	}

	public int getForks() {
		return forks;
	}

	public void setForks(int forks) {
		this.forks = forks;
	}

}
