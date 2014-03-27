import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.incredibles.reclib.DiscriminatorCategorization;
import com.incredibles.reclib.RecMaintenance;
import com.incredibles.reclib.UploadFiltersResultV2;
import com.incredibles.storage.RecommenderDbService;
import com.incredibles.storage.RecommenderDbServiceCreator;

/**
 * Servlet implementation class API
 */
public class API extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private final Gson gson;
	
	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public API() {
		super();
		GsonBuilder builder = new GsonBuilder();
		JsonDeserializer<Date> deserializer = new JsonDeserializer<Date>() {

			public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
				return new Date(json.getAsJsonPrimitive().getAsLong());
			}
		};
		builder.registerTypeAdapter(Date.class, deserializer);
		gson = builder.create();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		processRequest(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		processRequest(request, response);
	}

	/**Upload into RecommendationLogs table informations from API calls
	 * if UserId == null, uses 0.*/
	private void uploadRecommendationInfo(String info, Integer UserId){
		RecommenderDbService dbService2=null;
		try {
			dbService2 = RecommenderDbServiceCreator.createCloud();
			if(UserId!=null){
				dbService2.insertRecommendationLog(info, UserId);
			}else{
				dbService2.insertRecommendationLog(info, 0);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			if (dbService2 != null) {
				try {
					dbService2.close();
				} catch (SQLException | IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**Returns all user funspotter/facebook id info*/
	private HashMap<Integer, Long> getAllUserId(){
		HashMap<Integer, Long> UserAndFaceId = null;
		RecommenderDbService dbService1 = null;
		try {
			dbService1 = RecommenderDbServiceCreator.createCloud();
			UserAndFaceId = dbService1.getAllFaceAndUserIdV2();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			if (dbService1 != null) {
				try {
					dbService1.close();
				} catch (SQLException | IOException e) {
					e.printStackTrace();
				}
			}
		}
		return UserAndFaceId;
	}
	
	/**Call commands to execute different recommendation based algorithms*/
	private void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String ret = "CallDone";
			if(request.getParameter("cmd").equals("newUser")){
				final int UserID = Integer.parseInt(request.getParameter("UserId"));
				Runnable r = new Runnable(){
					@Override
					public void run() {
						uploadRecommendationInfo("FirstTimeSecondStepStart",UserID);
						UploadFiltersResultV2.filterExecute(UserID);
						uploadRecommendationInfo("FirstTimeSecondStepDone",UserID);	
					}
				};
				Thread t = new Thread(r);
				t.start();
			}else if(request.getParameter("cmd").equals("maintainRecTable")){
				Runnable r = new Runnable(){
					@Override
					public void run() {	/*logs info uploading in the function*/
						RunRecMaintenance maintain = new RunRecMaintenance();
						maintain.startAutomaticMaintain();
					}
				};
				Thread t = new Thread(r);
				t.start();
			}else if(request.getParameter("cmd").equals("maintainRecTableOnce")){
				Runnable r = new Runnable(){
					@Override
					public void run() {
						uploadRecommendationInfo("RecMaintainStart",null);
						RecMaintenance maintain = new RecMaintenance();
						maintain.maintainRecTable();
						uploadRecommendationInfo("RecMaintainDone",null);
					}
				};
				Thread t = new Thread(r);
				t.start();
			}else if(request.getParameter("cmd").equals("startGravity")){
				Runnable r = new Runnable(){
					@Override
					public void run() {	/*logs info uploading in the function*/
						CalculateGravity gravity = new CalculateGravity();
						gravity.startAutomaticGravity();
					}
				};
				Thread t = new Thread(r);
				t.start();
			}else if(request.getParameter("cmd").equals("refreshFirstStep")){
				Runnable r = new Runnable(){
					@Override
					public void run() {
						uploadRecommendationInfo("RefreshFirstStepStart",null);
						CalculateFirstStepV2 firstStep = new CalculateFirstStepV2();
						firstStep.startAutomaticFirstStepRefresh();
					}
				};
				Thread t = new Thread(r);
				t.start();
			}else if(request.getParameter("cmd").equals("runSecondStepForAll")){
				Runnable r = new Runnable(){
					@Override
					public void run() {
						HashMap<Integer, Long> UserAndFaceId = getAllUserId();
						for(Entry<Integer, Long>entry: UserAndFaceId.entrySet()){
							Integer UserId = entry.getKey();
							UploadFiltersResultV2.filterExecute(UserId);
						}
					}
				};
				Thread t = new Thread(r);
				t.start();				
			}else if(request.getParameter("cmd").equals("categorizeFacebookEvents")){
				Runnable r = new Runnable(){
					@Override
					public void run() {			
						RunFacebookEventCategorization categorization = new RunFacebookEventCategorization();
						categorization.startAutomaticCategorization();
					}
				};
				Thread t = new Thread(r);
				t.start();
			}else if(request.getParameter("cmd").equals("categorizeFacebookEventsOnce")){
				Runnable r = new Runnable(){
					@Override
					public void run() {
						DiscriminatorCategorization.categorizing();
					}
				};
				Thread t = new Thread(r);
				t.start();
			}
			response.setContentType("text/html; charset=utf-8");
			String responseStr = request.getParameter("callback") + "(" + ret + ")";//""=ret
			response.getOutputStream().write(responseStr.getBytes(Charset.forName("UTF-8")));
	}
}
