package com.openclassrooms.tourguide.service;

import com.openclassrooms.tourguide.configuration.ApplicationConfiguation;
import com.openclassrooms.tourguide.user.User;
import com.openclassrooms.tourguide.user.UserReward;
import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import rewardCentral.RewardCentral;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ForkJoinPool;

@Service
@Slf4j
public class RewardsService {
  private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;
  private final GpsUtil gpsUtil;
  private final RewardCentral rewardsCentral;
  // proximity in miles
  private final int defaultProximityBuffer = 10;
  @Setter
  private int proximityBuffer = defaultProximityBuffer;

  public RewardsService(GpsUtil gpsUtil, RewardCentral rewardCentral) {
    this.gpsUtil = gpsUtil;
    this.rewardsCentral = rewardCentral;
  }

  public void setDefaultProximityBuffer() {
    proximityBuffer = defaultProximityBuffer;
  }

  public void calculateRewards(User user) {

    CopyOnWriteArrayList<VisitedLocation> userLocations = new CopyOnWriteArrayList<>(user.getVisitedLocations());
    List<Attraction> attractions = new ArrayList<>();
    attractions = gpsUtil.getAttractions();

    List<UserReward> rewards = Collections.synchronizedList(new ArrayList<>());

    for (VisitedLocation visitedLocation : userLocations) {
      for (Attraction attraction : attractions) {
        if(rewards.stream().noneMatch(r -> r.attraction.attractionName.equals(attraction.attractionName))){
          if (nearAttraction(visitedLocation, attraction)) {
            UserReward userReward = new UserReward(visitedLocation, attraction, getRewardPoints(attraction, user));
            rewards.add(userReward);
          }
        }
      }
    }

    user.setUserRewards(rewards);

  }

  public boolean isWithinAttractionProximity(Attraction attraction, Location location) {
    int attractionProximityRange = ApplicationConfiguation.ATTRACTION_PROXIMITY_RANGE;
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
    return STATUTE_MILES_PER_NAUTICAL_MILE * nauticalMiles;
  }

  public void parallelCalculateRewardsUsersList(List<User> allUsers) {
    ForkJoinPool customThreadPool = new ForkJoinPool(ApplicationConfiguation.MAX_THREAD_REWARD);

    customThreadPool.submit(() -> allUsers.stream().parallel().forEach(this::calculateRewards)).join();

    customThreadPool.shutdown();
  }

}
