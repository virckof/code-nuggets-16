package edu.ca.ualberta.code.nuggets.backend;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import com.google.gson.Gson;


/**
 * This class encapsulates the backend communication of the nugget add-on.<br>
 * This class has 4 methods:<br>
 * upvote - upvotes a snippet<br>
 * downvote - downvotes a snippet<br>
 * insertSnippet - inserts a snippet<br>
 * getInfo - gets info about a snippet<br>
 * createQuery - adds a query to the databse
 *
 */
@SuppressWarnings("deprecation")
public class BackendConnector {
		private static Gson gson;
		private static String upvoteURL = "http://162.246.156.63/nuggets/snippets/ranking/upvote/";
		private static String downvoteURL = "http://162.246.156.63/nuggets/snippets/ranking/downvote/";
		private static String insertURL = "http://162.246.156.63/nuggets/snippets/ranking/snippet_inserts/";
		private static String infoURL = "http://162.246.156.63/nuggets/snippets/ranking/snippet_votes/";
		private static String queryURL = "http://162.246.156.63/nuggets/feedback/query";

		/**
		 * This method upvotes a snippet
		 * @param ID - ID of the snippet being upvoted (String)
		 * @param date - Date of last modification (format: mm-dd-yy) (String)
		 * @return The json file returned by the database
		 * @throws Exception
		 */
		public String upvote(String ID, String date) throws Exception{
			HttpClient httpClient = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet(upvoteURL + ID + "/" + date);
			String encodedUser = encodeAuthorization("admin","admin");
			httpGet.setHeader("Authorization","Basic "+encodedUser);
			HttpResponse response;
			String json = null;
			try{
				response = httpClient.execute(httpGet);
				json = getResponseContent(response);
				return json;
			}
			catch (Exception ex){
				throw ex;
			}
		}

		/**
		 * This method downvotes a snippet
		 * @param ID - ID of the snippet being downvoted (String)
		 * @param date - Date of last modification (format: mm-dd-yy) (String)
		 * @return The json file returned by the database
		 * @throws Exception
		 */
		public String downvote(String ID, String date) throws Exception{
			HttpClient httpClient = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet(downvoteURL + ID + "/" + date);
			String encodedUser = encodeAuthorization("admin","admin");
			httpGet.setHeader("Authorization","Basic "+encodedUser);
			HttpResponse response;
			try{
				response = httpClient.execute(httpGet);
				String json = getResponseContent(response);
				return json;
			}
			catch (Exception ex){
				throw ex;
			}
		}

		/**
		 * This method inserts a snippet
		 * @param ID - ID of the snippet being inserted (String)
		 * @param date - Date of last modification (format: mm-dd-yy) (String)
		 * @return The json file returned by the database
		 * @throws Exception
		 */
		public String insertSnippet(String ID, String date) throws Exception{
			HttpClient httpClient = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet(insertURL + ID + "/" + date);
			String encodedUser = encodeAuthorization("admin","admin");
			httpGet.setHeader("Authorization","Basic "+encodedUser);
			HttpResponse response;
			try{
				response = httpClient.execute(httpGet);
				String json = getResponseContent(response);
				return json;
			}
			catch (Exception ex){
				throw ex;
			}
		}

		/**
		 * This method gets the information about a snippet
		 * @param ID - ID of the snippet (String)
		 * @param date - Date of last modification (format: mm-dd-yy) (String)
		 * @return The json file returned by the database
		 * @throws Exception
		 */
		public String getInfo(String ID, String date) throws Exception{
			HttpClient httpClient = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet(infoURL + ID + "/" + date);
			String encodedUser = encodeAuthorization("admin","admin");
			httpGet.setHeader("Authorization","Basic "+encodedUser);
			HttpResponse response;
			try{
				response = httpClient.execute(httpGet);
				String json = getResponseContent(response);
				return json;
			}
			catch (Exception ex){
				throw ex;
			}
		}

		/**
		 * This method saves the queries that users make to the nugget add-on to a database
		 * @param query - The query being made (String)
		 * @param context - The context of the query (String)
		 * @param country - The country of the user (String)
		 * @return The json file returned by the database
		 * @throws Exception
		 */
		public String registerQuery(String query, String context, String country) throws Exception{
			HttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(queryURL);
			String encodedUser = encodeAuthorization("admin","admin");
			httpPost.setHeader("Authorization","Basic "+encodedUser);
			try {
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
				nameValuePairs.add(new BasicNameValuePair("query", query));
				nameValuePairs.add(new BasicNameValuePair("context", context));
				nameValuePairs.add(new BasicNameValuePair("country", country));
				httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				HttpResponse response = httpClient.execute(httpPost);
				String json = getResponseContent(response);
				return json;
			}
			catch (Exception ex){
				throw ex;
			}
		}

		/**
		 * Returns a parsed response
		 * @param response - Response returned after an http request (HttpResponse)
		 * @return The json file returned by the database
		 * @throws IOException
		 */
		public String getResponseContent(HttpResponse response) throws IOException {
			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			StringBuffer result = new StringBuffer();
			String line = "";
			while ((line = rd.readLine()) != null) {
				result.append(line);
			}
			gson = new Gson();
			return gson.toJson(result);
		}

		/**
		 * Returns an encoded user and password to be used for http authentication
		 * @param user - Username of user
		 * @param pass - Password of user
		 * @return String of encoded user and password
		 */
		private String encodeAuthorization(String user, String pass){
			Encoder encoder = Base64.getEncoder();
			String authStr = user+":"+pass;
			String encoding = encoder.encodeToString(authStr.getBytes());
			return encoding;
		}
}
