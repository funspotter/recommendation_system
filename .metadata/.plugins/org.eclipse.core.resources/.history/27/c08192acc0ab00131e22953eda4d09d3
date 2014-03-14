import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.incredibles.reclib.EventReplacerV1;


public class CalculateEventReplace {

	
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	public void startAutomaticEventReplace() {
		final Runnable beeper = new Runnable() {
			public void run() {						
				EventReplacerV1 er = new EventReplacerV1();
				er.executeEventReplace();				
			}
		};
		final ScheduledFuture<?> beeperHandle = scheduler.scheduleAtFixedRate(beeper, 3, 4, TimeUnit.HOURS);
//		scheduler.schedule(new Runnable() {
//			public void run() {
//				beeperHandle.cancel(true);
//			}
//		}, 180, TimeUnit.MINUTES);
	}

	
}
