package com.openclassrooms.tourguide.configuration;

import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfiguation {

  public final static boolean TEST_MODE = true;

  public final static int INTERNAL_TEST_USER_NUMBER = 100;

  public final static boolean PARALLEL_PROCESSING = true;

  public final static int MAX_THREAD_TRACK = 5000;

  public final static int MAX_THREAD_REWARD = 5000;

  public final static int ATTRACTION_PROXIMITY_RANGE = 200;

  public final static int MAX_ATTRACTION_TO_SEARCH = 5;


}
