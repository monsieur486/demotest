package com.openclassrooms.tourguide.service;

import com.openclassrooms.tourguide.configuration.ApplicationConfiguation;
import com.openclassrooms.tourguide.dto.AttractionNearbyUserDto;
import com.openclassrooms.tourguide.helper.InternalTestHelper;
import com.openclassrooms.tourguide.tracker.Tracker;
import com.openclassrooms.tourguide.user.User;
import com.openclassrooms.tourguide.user.UserReward;
import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tripPricer.Provider;
import tripPricer.TripPricer;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.IntStream;

/**
 * Service class for TourGuide application.
 * Provides methods to manage users, track their locations, and calculate rewards.
 */
@Service
public class TourGuideService {
  /**********************************************************************************
   *
   * Methods Below: For Internal Testing
   *
   **********************************************************************************/
  private static final String tripPricerApiKey = "test-server-api-key";
  public final Tracker tracker;
  private final GpsUtil gpsUtil;
  private final RewardsService rewardsService;
  private final TripPricer tripPricer = new TripPricer();
  // Database connection will be used for external users, but for testing purposes
  // internal users are provided and stored in memory
  private final Map<String, User> internalUserMap = new HashMap<>();
  private final Logger logger = LoggerFactory.getLogger(TourGuideService.class);
  boolean testMode = ApplicationConfiguation.TEST_MODE;

  /**
   * Constructor for TourGuideService.
   *
   * @param gpsUtil the GPS utility service
   * @param rewardsService the rewards service
   */
  public TourGuideService(GpsUtil gpsUtil, RewardsService rewardsService) {
    this.gpsUtil = gpsUtil;
    this.rewardsService = rewardsService;

    Locale.setDefault(Locale.US);

    if (testMode) {
      logger.info("TestMode enabled");
      logger.debug("Initializing users");
      initializeInternalUsers();
      logger.debug("Finished initializing users");
    }
    tracker = new Tracker(this);
    addShutDownHook();
  }

  /**
   * Gets the rewards for a user.
   *
   * @param user the user
   * @return the list of user rewards
   */
  public List<UserReward> getUserRewards(User user) {
    return user.getUserRewards();
  }

  /**
   * Gets the location of a user.
   *
   * @param user the user
   * @return the visited location
   */
  public VisitedLocation getUserLocation(User user) {
    return (!user.getVisitedLocations().isEmpty()) ? user.getLastVisitedLocation()
            : trackUserLocation(user);
  }

  /**
   * Gets a user by username.
   *
   * @param userName the username
   * @return the user
   */
  public User getUser(String userName) {
    return internalUserMap.get(userName);
  }

  /**
   * Gets a user by user ID.
   *
   * @param userId the user ID
   * @return the user
   */
  public User getUserById(UUID userId) {
    return internalUserMap.values().stream().filter(user -> user.getUserId().equals(userId)).findFirst().orElse(null);
  }

  /**
   * Gets all users.
   *
   * @return the list of all users
   */
  public List<User> getAllUsers() {
    return new ArrayList<>(internalUserMap.values());
  }

  /**
   * Adds a user.
   *
   * @param user the user
   */
  public void addUser(User user) {
    if (!internalUserMap.containsKey(user.getUserName())) {
      internalUserMap.put(user.getUserName(), user);
    }
  }

  /**
   * Gets trip deals for a user.
   *
   * @param user the user
   * @return the list of providers
   */
  public List<Provider> getTripDeals(User user) {
    int cumulatativeRewardPoints = user.getUserRewards().stream().mapToInt(UserReward::getRewardPoints).sum();
    List<Provider> providers = tripPricer.getPrice(tripPricerApiKey, user.getUserId(),
            user.getUserPreferences().getNumberOfAdults(), user.getUserPreferences().getNumberOfChildren(),
            user.getUserPreferences().getTripDuration(), cumulatativeRewardPoints);
    user.setTripDeals(providers);
    return providers;
  }

  /**
   * Tracks the location of a user.
   *
   * @param user the user
   * @return the visited location
   */
  public VisitedLocation trackUserLocation(User user) {
    VisitedLocation visitedLocation = gpsUtil.getUserLocation(user.getUserId());
    user.addToVisitedLocations(visitedLocation);
    rewardsService.calculateRewards(user);
    return visitedLocation;
  }

  /**
   * Gets nearby attractions for a user.
   *
   * @param visitedLocation the visited location
   * @param user the user
   * @return the list of nearby attractions
   */
  public List<AttractionNearbyUserDto> getNearByAttractions(VisitedLocation visitedLocation, User user) {
    List<AttractionNearbyUserDto> nearbyAttractions = new ArrayList<>();
    for (Attraction attraction : gpsUtil.getAttractions()) {
      int rewardPoints = rewardsService.getRewardPoints(attraction, user);
      double distance = rewardsService.getDistance(attraction, visitedLocation.location);
      AttractionNearbyUserDto attractionNearbyUserDto = new AttractionNearbyUserDto(attraction, visitedLocation, rewardPoints, distance);
      nearbyAttractions.add(attractionNearbyUserDto);
    }

    nearbyAttractions.sort(Comparator.comparing(AttractionNearbyUserDto::getDistance));

    if (nearbyAttractions.size() > 5) {
      nearbyAttractions.sort(Comparator.comparing(AttractionNearbyUserDto::getDistance));
      nearbyAttractions = nearbyAttractions.subList(0, 5);
    }

    return nearbyAttractions;
  }

  /**
   * Adds a shutdown hook to stop tracking.
   */
  private void addShutDownHook() {
    Runtime.getRuntime().addShutdownHook(new Thread() {
      public void run() {
        tracker.stopTracking();
      }
    });
  }

  /**
   * Initializes internal users for testing.
   */
  private void initializeInternalUsers() {
    IntStream.range(0, InternalTestHelper.getInternalUserNumber()).forEach(i -> {
      String userName = "internalUser" + i;
      String phone = "000";
      String email = userName + "@tourGuide.com";
      User user = new User(UUID.randomUUID(), userName, phone, email);
      generateUserLocationHistory(user);

      internalUserMap.put(userName, user);
    });
    logger.debug("Created " + InternalTestHelper.getInternalUserNumber() + " internal test users.");
  }

  /**
   * Generates location history for a user.
   *
   * @param user the user
   */
  private void generateUserLocationHistory(User user) {
    IntStream.range(0, 3).forEach(i -> {
      user.addToVisitedLocations(new VisitedLocation(user.getUserId(),
              new Location(generateRandomLatitude(), generateRandomLongitude()), getRandomTime()));
    });
  }

  /**
   * Generates a random longitude.
   *
   * @return the random longitude
   */
  private double generateRandomLongitude() {
    double leftLimit = -180;
    double rightLimit = 180;
    return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
  }

  /**
   * Generates a random latitude.
   *
   * @return the random latitude
   */
  private double generateRandomLatitude() {
    double leftLimit = -85.05112878;
    double rightLimit = 85.05112878;
    return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
  }

  /**
   * Gets a random time within the last 30 days.
   *
   * @return the random time
   */
  private Date getRandomTime() {
    LocalDateTime localDateTime = LocalDateTime.now().minusDays(new Random().nextInt(30));
    return Date.from(localDateTime.toInstant(ZoneOffset.UTC));
  }

  /**
   * Tracks the location of all users in parallel.
   *
   * @param allUsers the list of all users
   */
  public void parallelTrackAllUsersLocation(List<User> allUsers) {
    ForkJoinPool customThreadPool = new ForkJoinPool(ApplicationConfiguation.MAX_THREAD_TRACK);

    customThreadPool.submit(() -> {
      allUsers.stream().parallel().forEach(this::trackUserLocation);
    }).join();

    customThreadPool.shutdown();
  }

}