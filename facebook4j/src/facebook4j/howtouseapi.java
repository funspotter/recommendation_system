package facebook4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;







import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.restfb.Connection;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.FacebookClient.AccessToken;
import com.restfb.Parameter;
import com.restfb.types.Checkin;
import com.restfb.types.Event;
import com.restfb.types.FacebookType;
import com.restfb.types.Page;
import com.restfb.types.Place;
import com.restfb.types.Post;
import com.restfb.types.User;
import com.restfb.types.Venue;



public class howtouseapi {

	public static void main(String[] args) {
		

		String MY_APP_SECRET = "add4434d3f3f754d29d567d59f285be5";
		String MY_APP_ID = "513927361994826";
		AccessToken accessToken =
		  new DefaultFacebookClient().obtainAppAccessToken(MY_APP_ID, MY_APP_SECRET);
		
		FacebookClient publicOnlyFacebookClient = new DefaultFacebookClient();

		// Get added security by using your app secret:

		String myaccesToken = "CAACEdEose0cBAOW0Njgemp3kRvpZAPvFOs2m6VZCb8bPPChzx3IPacanP9HJesRwHR2lwvDNMlidhmIaSTOjC4gZCzq7eSyDoep6nOQg57SfnCQYaX3M8Hj1H1hAAFdBRVg8jE5or3iq97ByDofzkuSi889DZAv1fPV5rETEsO5RqQRP6PIDZAznL4MpmJPN8wFgHHVQ0qQZDZD";
		
		FacebookClient facebookClient = new DefaultFacebookClient(accessToken.getAccessToken());
		
		
		Event event = facebookClient.fetchObject("184645428413244", Event.class, Parameter.with("metadata", 1));
		System.out.println(event.getVenue().getId());
		Place place  = facebookClient.fetchObject(event.getVenue().getId(), Place.class, Parameter.with("metadata", 1));
		//Page page = facebookClient.fetchObject("Funspotter", Page.class);

		//System.out.println("User name: " + user.getName());
		//System.out.println("Page likes: " + page.getLikes());
		System.out.println(place.getMetadata().getConnections().getTagged().toString());
		String caregory = null;
	
		try {
			JSONObject valami = readJsonFromUrl(place.getMetadata().getConnections().getTagged().toString());
			JSONArray tomb = valami.getJSONArray("data");
			System.out.println(tomb);
			JSONObject valami2 = tomb.getJSONObject(0);
			JSONObject valami3 = valami2.getJSONObject("to");
			JSONArray tomb2 = valami3.getJSONArray("data");
			JSONObject faszom = tomb2.getJSONObject(0);
			faszom.get("category");
			JSONArray categoryList = faszom.getJSONArray("category_list");
			for(int i=0; i< categoryList.length(); i++){
				System.out.println(categoryList.getJSONObject(i).get("id"));
				System.out.println(categoryList.getJSONObject(i).get("name"));
			}
			System.out.println(faszom.get("category").toString());
		} catch (IOException | JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
	}
	
	private static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
		InputStream is = new URL(url).openStream();
		
		BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
		String jsonText = readAll(rd);
		
		//System.out.println(jsonText);
		
		try {
			
			JSONObject json = new JSONObject(jsonText);
			return json;
		}finally {
			is.close();
		}
	}
	
	private static String readAll(Reader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1) {
			sb.append((char) cp);
		}
		return sb.toString();
	}
	

}
