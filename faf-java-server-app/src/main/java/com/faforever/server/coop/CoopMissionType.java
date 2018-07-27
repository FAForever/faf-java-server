package com.faforever.server.coop;

public enum CoopMissionType {
  // Order is crucial
  FA_CAMPAIGN("FA Campaign"),
  AEON_CAMPAIGN("Aeon Vanilla Campaign"),
  CYBRAN_CAMPAIGN("Cybran Vanilla Campaign"),
  UEF_CAMPAIGN("UEF Vanilla Campaign"),
  CUSTOM_MISSIONS("Custom Missions");

  /**
   * @deprecated bad for internationalization. We should send a key or translated string instead. But it's used by the
   * legacy client.
   */
  @Deprecated
  private final String title;

  CoopMissionType(String title) {
    this.title = title;
  }

  public String getTitle() {
    return title;
  }
}
