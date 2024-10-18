package com.openclassrooms.tourguide.service;

import com.openclassrooms.tourguide.configuration.ApplicationConfiguation;
import com.openclassrooms.tourguide.user.User;
import com.openclassrooms.tourguide.user.UserReward;
import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import org.springframework.stereotype.Service;
import rewardCentral.RewardCentral;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ForkJoinPool;

@Service
public class RewardsService {
  private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;
  private final GpsUtil gpsUtil;
  private final RewardCentral rewardsCentral;
  // proximity in miles
  private final int defaultProximityBuffer = 10;
  private final int attractionProximityRange = 200;
  private int proximityBuffer = defaultProximityBuffer;

  public RewardsService(GpsUtil gpsUtil, RewardCentral rewardCentral) {
    this.gpsUtil = gpsUtil;
    this.rewardsCentral = rewardCentral;
  }

  public void setProximityBuffer(int proximityBuffer) {
    this.proximityBuffer = proximityBuffer;
  }

  public void setDefaultProximityBuffer() {
    proximityBuffer = defaultProximityBuffer;
  }

  public void calculateRewards(User user) {
    List<Attraction> attractions = gpsUtil.getAttractions();
    CopyOnWriteArrayList<VisitedLocation> userLocations = new CopyOnWriteArrayList<>(user.getVisitedLocations());

    for (VisitedLocation visitedLocation : userLocations) {
      for (Attraction attraction : attractions) {
        if (user.getUserRewards().stream().filter(r -> r.attraction.attractionName.equals(attraction.attractionName)).count() == 0) {
          if (nearAttraction(visitedLocation, attraction)) {
            user.addUserReward(new UserReward(visitedLocation, attraction, getRewardPoints(attraction, user)));
          }
        }
      }
    }
  }

  public boolean isWithinAttractionProximity(Attraction attraction, Location location) {
    return !(getDistance(attraction, location) > attractionProximityRange);
  }

  private boolean nearAttraction(VisitedLocation visitedLocation, Attraction attraction) {
    return !(getDistance(attraction, visitedLocation.location) > proximityBuffer);
  }

  public int getRewardPoints(Attraction attraction, User user) {
    return rewardsCentral.getAttractionRewardPoints(attraction.attractionId, user.getUserId());
  }

  public double getDistance(Location loc1, Location loc2) {
    double lat1 = Math.toRadians(loc1.latitude);
    double lon1 = Math.toRadians(loc1.longitude);
    double lat2 = Math.toRadians(loc2.latitude);
    double lon2 = Math.toRadians(loc2.longitude);

    double angle = Math.acos(Math.sin(lat1) * Math.sin(lat2)
            + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));

    double nauticalMiles = 60 * Math.toDegrees(angle);
    double statuteMiles = STATUTE_MILES_PER_NAUTICAL_MILE * nauticalMiles;
    return statuteMiles;
  }

  public void parallelListRewards(List<User> allUsers) {
    ForkJoinPool customThreadPool = new ForkJoinPool(ApplicationConfiguation.MAX_THREAD_REWARD);

    customThreadPool.submit(() -> {
      allUsers.stream().parallel().forEach(this::calculateRewards);
    }).join();

    customThreadPool.shutdown();
  }

}
