package com.openclassrooms.tourguide.tracker;

import com.openclassrooms.tourguide.configuration.ApplicationConfiguation;
import com.openclassrooms.tourguide.service.TourGuideService;
import com.openclassrooms.tourguide.user.User;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Tracker extends Thread {
  private static final long trackingPollingInterval = TimeUnit.MINUTES.toSeconds(5);
  private final ExecutorService executorService = Executors.newSingleThreadExecutor();
  private final TourGuideService tourGuideService;
  private final Logger logger = LoggerFactory.getLogger(Tracker.class);
  private boolean stop = false;

  public Tracker(TourGuideService tourGuideService) {
    this.tourGuideService = tourGuideService;

    executorService.submit(this);
  }

  /**
   * Assures to shut down the Tracker thread
   */
  public void stopTracking() {
    stop = true;
    executorService.shutdownNow();
  }

  @Override
  public void run() {
    StopWatch stopWatch = new StopWatch();
    while (true) {
      if (Thread.currentThread().isInterrupted() || stop) {
        logger.debug("Tracker stopping");
        break;
      }

      List<User> users = tourGuideService.getAllUsers();
      logger.debug("Begin Tracker. Tracking {} users.", users.size());
      stopWatch.start();
      if (ApplicationConfiguation.PARALLEL_PROCESSING) {
        logger.debug("Tracker is using parallel processing");
        tourGuideService.parallelTrackAllUsersLocation(users);
      } else {
        logger.debug("Tracker is using sequential processing");
        users.forEach(tourGuideService::trackUserLocation);
      }

      stopWatch.stop();
      logger.debug("!!! END !!! Tracker Time Elapsed: {} seconds for {} users.",
              TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()),
              users.size());
      stopWatch.reset();
      try {
        logger.debug("Tracker sleeping");
        TimeUnit.SECONDS.sleep(trackingPollingInterval);
      } catch (InterruptedException e) {
        break;
      }
    }

  }
}
