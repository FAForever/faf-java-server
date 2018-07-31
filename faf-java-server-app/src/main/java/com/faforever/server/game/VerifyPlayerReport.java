package com.faforever.server.game;

import com.faforever.server.common.ClientMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VerifyPlayerReport implements ClientMessage {
  private int id;
  private String name;
  private float mean;
  private float deviation;
  private String country;
  private String avatarUrl;
  private String avatarDescription;
}
