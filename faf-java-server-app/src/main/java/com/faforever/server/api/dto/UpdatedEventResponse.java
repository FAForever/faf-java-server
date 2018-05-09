package com.faforever.server.api.dto;

import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Type("updatedAchievement")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdatedEventResponse {

  @Id
  private String id;
  private String eventId;
  private Integer currentCount;
}
