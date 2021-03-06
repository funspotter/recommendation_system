import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.incredibles.reclib.DiscriminatorCategorization;
import com.incredibles.reclib.RecMaintenance;
import com.incredibles.storage.RecommenderDbService;
import com.incredibles.storage.RecommenderDbServiceCreator;


public class RunFacebookEventCategorization {

private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	
	public void startAutomaticCategorization() {
		final Runnable beeper = new Runnable() {
			public void run() {				
				DiscriminatorCategorization categ = new DiscriminatorCategorization();
				categ.categorizingV2();
			}
		};
		final ScheduledFuture<?> beeperHandle = scheduler.scheduleAtFixedRate(beeper, 2, 24, TimeUnit.HOURS);
	}
	
}
