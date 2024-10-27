package com.openclassrooms.tourguide.helper;

import com.openclassrooms.tourguide.configuration.ApplicationConfiguation;
import lombok.Getter;

public class InternalTestHelper {

  // Set this default up to 100,000 for testing
  @Getter
  private static int internalUserNumber = ApplicationConfiguation.INTERNAL_TEST_USER_NUMBER;

  public static void setInternalUserNumber(int internalUserNumber) {
    InternalTestHelper.internalUserNumber = internalUserNumber;
  }
}
