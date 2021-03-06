import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.incredibles.reclib.RecMaintenance;
import com.incredibles.storage.RecommenderDbService;
import com.incredibles.storage.RecommenderDbServiceCreator;

public class RunRecMaintenance {

private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	
	public void startAutomaticMaintain() {
		final Runnable beeper = new Runnable() {
			public void run() {
				RecommenderDbService dbService2=null;
				try {
					dbService2 = RecommenderDbServiceCreator.createCloud();
					dbService2.insertRecommendationLog("RecMaintainStart", 0);
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
				
				RecMaintenance maintain = new RecMaintenance();
				maintain.maintainRecTable();
				
				try {
					dbService2 = RecommenderDbServiceCreator.createCloud();
					dbService2.insertRecommendationLog("RecMaintainDone", 0);
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
		};
		final ScheduledFuture<?> beeperHandle = scheduler.scheduleAtFixedRate(beeper, 0, 26, TimeUnit.HOURS);
	}
}
