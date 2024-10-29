package com.openclassrooms.tourguide.service;

import com.openclassrooms.tourguide.configuration.ApplicationConfiguation;
import com.openclassrooms.tourguide.user.User;
import com.openclassrooms.tourguide.user.UserReward;
import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import rewardCentral.RewardCentral;
import tripPricer.Provider;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TourGuideServiceTest {

  private TourGuideService tourGuideService;
  private GpsUtil gpsUtil;

  @BeforeEach
  void setUp() {
    gpsUtil = Mockito.mock(GpsUtil.class);
    RewardCentral rewardCentral = Mockito.mock(RewardCentral.class);
    RewardsService rewardsService = new RewardsService(gpsUtil, rewardCentral);
    tourGuideService = new TourGuideService(gpsUtil, rewardsService);
  }

  @Test
  void getUserRewards() {
    User user = new User(UUID.randomUUID(), "testUser", "000", "test@tourGuide.com");
    UserReward userReward = new UserReward(new VisitedLocation(user.getUserId(), new gpsUtil.location.Location(0, 0), null), new Attraction("Disneyland", "Anaheim", "CA", 33.817595, -117.922008), 100);
    user.addUserReward(userReward);

    List<UserReward> rewards = tourGuideService.getUserRewards(user);

    assertNotNull(rewards);
    assertEquals(1, rewards.size());
    assertEquals("Disneyland", rewards.get(0).attraction.attractionName);
  }

  @Test
  void getUserLocation() {
    User user = new User(UUID.randomUUID(), "testUser", "000", "test@tourGuide.com");
    VisitedLocation visitedLocation = new VisitedLocation(user.getUserId(), new gpsUtil.location.Location(0, 0), null);
    user.addToVisitedLocations(visitedLocation);

    VisitedLocation location = tourGuideService.getUserLocation(user);

    assertNotNull(location);
    assertEquals(visitedLocation, location);
  }

  @Test
  void getUser() {
    User user = new User(UUID.randomUUID(), "testUser", "000", "test@tourGuide.com");
    tourGuideService.addUser(user);

    User retrievedUser = tourGuideService.getUser("testUser");

    assertNotNull(retrievedUser);
    assertEquals("testUser", retrievedUser.getUserName());
  }

  @Test
  void getUserById() {
    UUID userId = UUID.randomUUID();
    User user = new User(userId, "testUser", "000", "test@tourGuide.com");
    tourGuideService.addUser(user);

    User retrievedUser = tourGuideService.getUserById(userId);

    assertNotNull(retrievedUser);
    assertEquals(userId, retrievedUser.getUserId());
  }

  @Test
  void getAllUsers() {
    User user1 = new User(UUID.randomUUID(), "testUser1", "000", "test1@tourGuide.com");
    User user2 = new User(UUID.randomUUID(), "testUser2", "000", "test2@tourGuide.com");
    tourGuideService.addUser(user1);
    tourGuideService.addUser(user2);

    List<User> allUsers = tourGuideService.getAllUsers();

    assertNotNull(allUsers);
    assertEquals(ApplicationConfiguation.INTERNAL_TEST_USER_NUMBER + 2, allUsers.size());
  }

  @Test
  void addUser() {
    User user = new User(UUID.randomUUID(), "testUser", "000", "test@tourGuide.com");
    tourGuideService.addUser(user);

    User retrievedUser = tourGuideService.getUser("testUser");

    assertNotNull(retrievedUser);
    assertEquals("testUser", retrievedUser.getUserName());
  }

  @Test
  void getTripDeals() {
    User user = new User(UUID.randomUUID(), "testUser", "000", "test@tourGuide.com");
    List<Provider> providers = tourGuideService.getTripDeals(user);

    assertNotNull(providers);
    assertFalse(providers.isEmpty());
  }

  @Test
  void trackUserLocation() {
    User user = new User(UUID.randomUUID(), "testUser", "000", "test@tourGuide.com");
    VisitedLocation visitedLocation = new VisitedLocation(user.getUserId(), new gpsUtil.location.Location(0, 0), null);
    Mockito.when(gpsUtil.getUserLocation(user.getUserId())).thenReturn(visitedLocation);

    VisitedLocation location = tourGuideService.trackUserLocation(user);

    assertNotNull(location);
    assertEquals(visitedLocation, location);
  }

  @Test
  void parallelTrackAllUsersLocation() {
    User user = new User(UUID.randomUUID(), "testUser", "000", "test@tourGuide.com");
    tourGuideService.addUser(user);

    tourGuideService.parallelTrackAllUsersLocation(List.of(user));

    assertNotNull(user.getVisitedLocations());
    assertFalse(user.getVisitedLocations().isEmpty());
  }
}