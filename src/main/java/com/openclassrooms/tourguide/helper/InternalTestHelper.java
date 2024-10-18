package com.openclassrooms.tourguide.helper;

import com.openclassrooms.tourguide.configuration.ApplicationConfiguation;

public class InternalTestHelper {

  // Set this default up to 100,000 for testing
  private static int internalUserNumber = ApplicationConfiguation.INTERNAL_TEST_USER_NUMBER;

  public static int getInternalUserNumber() {
    return internalUserNumber;
  }

  public static void setInternalUserNumber(int internalUserNumber) {
    InternalTestHelper.internalUserNumber = internalUserNumber;
  }
}
