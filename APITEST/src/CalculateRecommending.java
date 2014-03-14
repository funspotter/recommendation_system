import java.io.IOException;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.incredibles.data.LogTableTypes;

import com.incredibles.reclib.UploadFiltersResultV2;
import com.incredibles.storage.RecommenderDbService;
import com.incredibles.storage.RecommenderDbServiceCreator;


public class CalculateRecommending {

	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	public void startAutomaticRecommending() {
		final Runnable beeper = new Runnable() {
			public void run() {							
				RecommenderDbService dbService = null;
				try {
					dbService = RecommenderDbServiceCreator.createCloud();
					HashMap<Integer, Long> hm = dbService.getAllFaceAndUserIdV2();
					String info = "SecondStepDone";
					for(Entry<Integer, Long> entry: hm.entrySet()){
						Integer userID = entry.getKey();
						Long fbID = entry.getValue();
						UploadFiltersResultV2.filterExecute(userID);
						dbService.insertRecommendationLog(info, userID);
					}
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}finally {
					if (dbService != null) {
						try {
							dbService.close();
						} catch (SQLException | IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		};
		final ScheduledFuture<?> beeperHandle = scheduler.scheduleAtFixedRate(beeper, 0, 168, TimeUnit.HOURS);
//		scheduler.schedule(new Runnable() {
//			public void run() {
//				beeperHandle.cancel(true);
//			}
//		}, 0, TimeUnit.MINUTES);
	}
}
