package com.openclassrooms.tourguide.user;

import gpsUtil.location.VisitedLocation;
import lombok.Getter;
import lombok.Setter;
import tripPricer.Provider;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class User {
  private final UUID userId;
  private final String userName;
  private final List<VisitedLocation> visitedLocations = new ArrayList<>();
  @Setter
  private List<UserReward> userRewards = new ArrayList<>();
  private String phoneNumber;
  private String emailAddress;
  private Date latestLocationTimestamp;
  private UserPreferences userPreferences = new UserPreferences();
  private List<Provider> tripDeals = new ArrayList<>();

  public User(UUID userId, String userName, String phoneNumber, String emailAddress) {
    this.userId = userId;
    this.userName = userName;
    this.phoneNumber = phoneNumber;
    this.emailAddress = emailAddress;
  }

  public void addToVisitedLocations(VisitedLocation visitedLocation) {
    visitedLocations.add(visitedLocation);
  }

  public void clearVisitedLocations() {
    visitedLocations.clear();
  }

  public void addUserReward(UserReward userReward) {
    if (userRewards.stream().noneMatch(r -> true)) {
      userRewards.add(userReward);
    }
  }

  public VisitedLocation getLastVisitedLocation() {
    return visitedLocations.get(visitedLocations.size() - 1);
  }

}
