import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import com.incredibles.storage.ClientDbService;
import com.incredibles.storage.ClientDbServiceCreator;


public class ShowsJsonObserver {

	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	public void startShowsJsonObserving() {
		final Runnable beeper = new Runnable() {
			public void run() {				
				
				ClientDbService dbService = null;
				try {
					dbService = ClientDbServiceCreator.createCloud();
					//dbService.checkUpdateableShowsJson();
					dbService.uploadShowsToShowJson();
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
		final ScheduledFuture<?> beeperHandle = scheduler.scheduleAtFixedRate(beeper, 0, 5, TimeUnit.MINUTES);
//		scheduler.schedule(new Runnable() {
//			public void run() {
//				beeperHandle.cancel(true);
//			}
//		}, 0, TimeUnit.MINUTES);
	}

}
