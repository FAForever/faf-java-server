package com.faforever.server.apt;

import lombok.Value;

import java.util.List;

@Value
public class MessageCategory {
  public String title;
  public List<Type> messageTypes;
}
