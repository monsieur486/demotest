package com.openclassrooms.tourguide;

import com.openclassrooms.tourguide.dto.AttractionNearbyUserDto;
import com.openclassrooms.tourguide.service.TourGuideService;
import com.openclassrooms.tourguide.user.User;
import com.openclassrooms.tourguide.user.UserReward;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tripPricer.Provider;

import java.util.List;

@RestController
public class TourGuideController {

  @Autowired
  TourGuideService tourGuideService;

  @RequestMapping("/")
  public String index() {
    return "Greetings from TourGuide!";
  }

  @RequestMapping("/getUsers")
  public List<User> getUsers() {
    return tourGuideService.getAllUsers();
  }

  @RequestMapping("/getLocation")
  public VisitedLocation getLocation(@RequestParam String userName) {
    return tourGuideService.getUserLocation(getUser(userName));
  }

  @RequestMapping("/getNearbyUser")
  public List<AttractionNearbyUserDto> getNearbyAttractions(@RequestParam String userName) {
    User user = getUser(userName);
    VisitedLocation visitedLocation = tourGuideService.getUserLocation(user);
    return tourGuideService.getNearByAttractions(visitedLocation, user);
  }

  @RequestMapping("/getRewards")
  public List<UserReward> getRewards(@RequestParam String userName) {
    return tourGuideService.getUserRewards(getUser(userName));
  }

  @RequestMapping("/getTripDeals")
  public List<Provider> getTripDeals(@RequestParam String userName) {
    return tourGuideService.getTripDeals(getUser(userName));
  }

  private User getUser(String userName) {
    return tourGuideService.getUser(userName);
  }


}