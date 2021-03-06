import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.incredibles.reclib.Recommender;
import com.incredibles.storage.RecommenderDbService;
import com.incredibles.storage.RecommenderDbServiceCreator;



public class CalculateIALSprobe {


	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	public void startAutomaticRecommending() {
		final Runnable beeper = new Runnable() {
			public void run() {
				RecommenderDbService dbService = null;
				String info = "ialsforus";
				try {
					dbService = RecommenderDbServiceCreator.createCloud();
					Recommender valami = new Recommender();
					valami.newRecFunction();
					dbService.insertRecommendationLog(info, 0);
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
		final ScheduledFuture<?> beeperHandle = scheduler.scheduleAtFixedRate(beeper, 0, 3, TimeUnit.HOURS);
//		scheduler.schedule(new Runnable() {
//			public void run() {
//				beeperHandle.cancel(true);
//			}
//		}, 0, TimeUnit.MINUTES);
	}

}
