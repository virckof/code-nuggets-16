package edu.ca.ualberta.code.nuggets.crawl;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;

import edu.ca.ualberta.code.nuggets.data.GistResultSet;
import edu.ca.ualberta.code.nuggets.data.Repository;
import edu.ca.ualberta.code.nuggets.data.Snippet;
import edu.ca.ualberta.code.nuggets.data.User;

/**
 * Main crawler class to extract snippets from gist.
 * if github ever publishes an api for gist serach this will be refactored.
 */
public class GistParser {

	public static final String HEADER_URI_MAIN = "https://gist.github.com";
	public static final String HEADER_URI_SEARCH = "https://gist.github.com/search?";
	public static final String HEADER_URI_USER = "https://github.com/";
	public static final int RESULTS_PER_PAGE = 10;

	private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'");

	private int totalResults = 0;
	/**
	 * Builds a simple query given a language, a set of keywords and a page number
	 *
	 * @param language
	 * @param keywords
	 * @param page
	 * @return A search url query properly formed
	 */
	public String buildURLQuery(String language, String[] keywords, int page) {

		String query = HEADER_URI_SEARCH + "l=" + language + "&p=" + page + "&q=";
		int count = 0;
		for (String key : keywords) {
			if(count==0){
				query += key;
				count++;
			}
			else{
				query += "+" + key;
			}
		}
		System.out.println(query);
		return query;
	}

	/**
	 * Finds the HTML representation of the result page and filters the Gist IDs
	 * of the hits.
	 *
	 * @param url
	 * @param monitor Progress monitor (Use null for testing outside the plugin)
	 */
	public ArrayList<String> parseGistQuery(String url, IProgressMonitor monitor) {
		Document doc;
		ArrayList<String> hits = new ArrayList<String>();
		try {
			doc = Jsoup.connect(url).get();

			String resultsHeader = doc.getElementsByClass("sort-bar").get(0).text();
			Pattern pattern = Pattern.compile(" found ([0-9,]+) gist results");
			Matcher matcher = pattern.matcher(resultsHeader);
			matcher.find();
			totalResults = Integer.valueOf(matcher.group(1).replace(",", ""));

			Iterator<Element> it = doc.getElementsByClass("gist-snippet").iterator();

			while (it.hasNext()) {
				Element e = it.next();
				String resultId = e.getElementsByAttribute("href").attr("href");
				hits.add(resultId);

				if (monitor != null) {
					monitor.worked(5);
				}
			}

		} catch (IOException e) {

			e.printStackTrace();
		}
		return hits;

	}

	/**
	 * Gets the raw representation of a snippet given an Gist url
	 *
	 * @param url
	 */
	public Snippet parseGistRaw(String url) {
		Document doc;
		String snippetText = "";
		String userPicUrl = "";

		String[] urlParts = url.split("/");
		String username = urlParts[urlParts.length - 2];
		String id = urlParts[urlParts.length - 1];
		Date creationDate = null;

		try {
			doc = Jsoup.connect(url).get();
			// extract avatar
			userPicUrl = doc.getElementsByClass("repohead-details-container").get(0).getElementsByTag("img").get(0).attr("src");
			userPicUrl = userPicUrl.substring(0, userPicUrl.indexOf("?")) + "?s=20";  // change thumbnail size

			// extract snippet text

			snippetText = doc.getElementsByClass("js-file-line").toString();
			snippetText = br2nl(snippetText);
			snippetText = formatJavaCode(snippetText);

			// extract creation date
			try {
				String strDate = doc.getElementsByClass("gist-timestamp")
						.get(0).getElementsByAttribute("datetime")
						.attr("datetime");
				creationDate = dateFormatter.parse(strDate);
			} catch (ParseException e) {
				e.printStackTrace();
			}

			Snippet s = new Snippet();
			s.setSourceId(id);
			s.setSnippet(snippetText);
			s.setDate(creationDate);

			User u = loadUserInfo(username);
			//User u = new User();
			u.setUsername(username);
			u.setPicUrl(userPicUrl);
			s.setUser(u);

			return s;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Returns a string that is formatted according to java coding conventions
	 * @param code - the string that represents the java code that needs to be formatted
	 * @return a String formatted to java coding conventions.
	 */
	private String formatJavaCode(String code){
		String str1 = "";
		String sIndent = "";
		 //goes through each character and adds/removes indentation if encounters "{" or "}"
		for(int iCount = 0; iCount < code.length(); iCount++){
			char ch = code.charAt(iCount);
			if(ch == '{'){
				sIndent += "  ";
				str1 += ch;
			}
			else if(ch == '}'){
				if(sIndent.length() >= 2 && str1.length() >= 2){
					sIndent = sIndent.substring(0, sIndent.length() - 2);
					str1 = str1.substring(0, str1.length() - 2);
					str1 += ch;
				}
				else{ str1 += ch;
				}
			}
			else if(ch == '\n'){
			    str1 += ch + sIndent;
			}
			else{
				str1 += ch;
			}
		}
		return str1;
 	}

	/**
	 * Returns a string that is cleaned up of hidden html characters
	 * @param html - The html that needs to be cleaned up of characters
	 * @return A string without hidden characters.
	 */
	public static String br2nl(String html) {
	    if(html==null)
	        return html;
	    Document document = Jsoup.parse(html);
	    document.outputSettings(new Document.OutputSettings().prettyPrint(false));//makes html() preserve linebreaks and spacing
	    String text =  Jsoup.clean(document.html(), "", Whitelist.none(), new Document.OutputSettings().prettyPrint(false));
	    text = text.replace("&lt;", "<");
	    text = text.replace("&gt;", ">");
	    return text;
	}


	/**
	 * Loads user information from the profile webpage.
	 * @param username
	 * @return
	 */
	public User loadUserInfo(String username) {

		User u = new User();
		u.setUsername(username);

		try {
			Document doc = Jsoup.connect(HEADER_URI_USER + username + "?tab=repositories").get();
			Element vcard = doc.getElementsByClass("vcard-stats").get(0);

			int followers = parseStatNumber(vcard.getElementsByClass("vcard-stat-count").get(0).text());
			int starred = parseStatNumber(vcard.getElementsByClass("vcard-stat-count").get(1).text());
			int following = parseStatNumber(vcard.getElementsByClass("vcard-stat-count").get(2).text());
			u.setFollowers(followers);
			u.setStarred(starred);
			u.setFollowing(following);

			List<Repository> repos = new ArrayList<Repository>();
			Iterator<Element> it = doc.getElementsByClass("repo-list-item").iterator();

			while (it.hasNext()) {
				Element repo = it.next();

				int stars = parseStatNumber(repo.getElementsByClass("repo-list-stat-item").get(0).text());
				int forks = parseStatNumber(repo.getElementsByClass("repo-list-stat-item").get(1).text());

				String name = repo.getElementsByClass("repo-list-name").get(0).text();

				repos.add(new Repository(name, stars, forks));
			}

			u.setRepositories(repos);

		} catch (IOException e1) {
			e1.printStackTrace();
		}

		return u;

	}

	private int parseStatNumber(String value) {
		value = value.replace(".", "");
		value = value.replace(",", "");
		value = value.replace("k", "000");
		value = value.replace("m", "000000");
		//return (1);
		return Integer.parseInt(value);
	}

	/**
	 * Creates the Snippet VOs to be used by upper layers
	 *
	 * @param gistIds
	 * @param monitor Progress monitor (Use null for testing outside the plugin)
	 * @return
	 */
	public GistResultSet getResultSnippets(ArrayList<String> gistIds, IProgressMonitor monitor) {

		ArrayList<Snippet> result = new ArrayList<>();
		int i = 0;

		for (String id : gistIds) {

			Snippet s = parseGistRaw(HEADER_URI_MAIN + id);
			s.setId(i);
			result.add(s);
			i++;

			if (monitor != null) {
				monitor.worked(10);
			}
		}

		return new GistResultSet(totalResults, result);

	}

	/**
	 * Get total results of the current search
	 * @return
	 */
	public int getTotalResults() {
		return totalResults;
	}

	/**
	 * Simple test function to be executed as a standalone (see main below)
	 */
	private void test() {
		String[] params = { "Bundle" };
		String query = buildURLQuery("Java", params, 1);

		ArrayList<String> hits = parseGistQuery(query, null);
		List<Snippet> snippets = getResultSnippets(hits, null).getSnippets();

		for (Snippet s : snippets) {
			System.out.println(s);
		}
	}

	public static void main(String... args) {
		GistParser gp = new GistParser();
		gp.test();
	}

}
