package com.openclassrooms.tourguide.service;

import com.openclassrooms.tourguide.user.User;
import com.openclassrooms.tourguide.user.UserReward;
import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rewardCentral.RewardCentral;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RewardsServiceTest {

  private RewardsService rewardsService;
  private GpsUtil gpsUtil;

  @BeforeEach
  void setUp() {
    gpsUtil = new GpsUtil();
    RewardCentral rewardCentral = new RewardCentral();
    rewardsService = new RewardsService(gpsUtil, rewardCentral);
  }

  @Test
  void setDefaultProximityBuffer() {
    rewardsService.setProximityBuffer(100);
    rewardsService.setDefaultProximityBuffer();
    assertEquals(10, rewardsService.getProximityBuffer());
  }

  @Test
  void calculateRewards() {
    User user = new User(UUID.randomUUID(), "testUser", "000", "test@tourGuide.com");
    Attraction attraction = gpsUtil.getAttractions().get(0);
    user.addToVisitedLocations(new VisitedLocation(user.getUserId(), attraction, new Date()));

    rewardsService.calculateRewards(user);
    List<UserReward> userRewards = user.getUserRewards();

    assertEquals(1, userRewards.size());
    assertEquals(attraction.attractionName, userRewards.get(0).attraction.attractionName);
  }

  @Test
  void isWithinAttractionProximity() {
    Attraction attraction = gpsUtil.getAttractions().get(0);
    assertTrue(rewardsService.isWithinAttractionProximity(attraction, attraction));
  }

  @Test
  void getRewardPoints() {
    User user = new User(UUID.randomUUID(), "testUser", "000", "test@tourGuide.com");
    Attraction attraction = gpsUtil.getAttractions().get(0);

    int rewardPoints = rewardsService.getRewardPoints(attraction, user);
    assertTrue(rewardPoints > 0);
  }

  @Test
  void getDistance() {
    Location loc1 = new Location(33.817595, -117.922008);
    Location loc2 = new Location(34.052235, -118.243683);

    double distance = rewardsService.getDistance(loc1, loc2);
    assertTrue(distance > 0);
  }

  @Test
  void parallelCalculateRewardsUsersList() {
    User user = new User(UUID.randomUUID(), "testUser", "000", "test@tourGuide.com");
    Attraction attraction = gpsUtil.getAttractions().get(0);
    user.addToVisitedLocations(new VisitedLocation(user.getUserId(), attraction, new Date()));

    List<User> allUsers = List.of(user);
    rewardsService.parallelCalculateRewardsUsersList(allUsers);

    List<UserReward> userRewards = user.getUserRewards();
    assertEquals(1, userRewards.size());
    assertEquals(attraction.attractionName, userRewards.get(0).attraction.attractionName);
  }

  @Test
  void setProximityBuffer() {
    rewardsService.setProximityBuffer(100);
    assertEquals(100, rewardsService.getProximityBuffer());
  }
}