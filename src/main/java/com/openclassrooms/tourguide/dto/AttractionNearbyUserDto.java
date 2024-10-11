package com.openclassrooms.tourguide.dto;

import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Data
@Component
@NoArgsConstructor
@AllArgsConstructor
public class AttractionNearbyUserDto {

  private String attractionName;
  private double attractionLatitude;
  private double attractionLongitude;
  private double userLatitude;
  private double userLongitude;
  private double distance;
  private int rewardPoints;

  public AttractionNearbyUserDto(Attraction attraction, VisitedLocation visitedLocation, int rewardPoints, double distance) {
    this.attractionName = attraction.attractionName;
    this.attractionLatitude = attraction.latitude;
    this.attractionLongitude = attraction.longitude;
    this.userLatitude = visitedLocation.location.latitude;
    this.userLongitude = visitedLocation.location.longitude;
    this.distance = distance;
    this.rewardPoints = rewardPoints;
  }

}
