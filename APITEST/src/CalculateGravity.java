import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;






import com.incredibles.reclib.EventReplacerV2;
import com.incredibles.storage.RecommenderDbService;
import com.incredibles.storage.RecommenderDbServiceCreator;


public class CalculateGravity {

	
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	public void startAutomaticGravity() {
		final Runnable beeper = new Runnable() {
			public void run() {	
				
				RecommenderDbService dbService2=null;
				try {
					dbService2 = RecommenderDbServiceCreator.createCloud();
					dbService2.insertRecommendationLog("GravityStart", 0);
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
				
				
				EventReplacerV2 er = new EventReplacerV2();
				er.executeEventReplace();
				
				RecommenderDbService dbService = null;
				try {
					dbService = RecommenderDbServiceCreator.createCloud();
					dbService.insertRecommendationLog("GravityDone", 0);
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
		final ScheduledFuture<?> beeperHandle = scheduler.scheduleAtFixedRate(beeper, 0, 4, TimeUnit.HOURS);
	}	
}
