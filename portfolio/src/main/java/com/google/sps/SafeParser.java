package com.google.sps;

public class SafeParser {
  public static int parseInt(String string, int fallback) {
    if (string == null) {
      return fallback;
    }

    try {
      return Integer.parseInt(string);
    } catch (NumberFormatException e) {
      return fallback;
    }
  }
}