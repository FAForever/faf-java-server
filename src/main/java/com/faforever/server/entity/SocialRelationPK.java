package com.faforever.server.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;

@Data
public class SocialRelationPK implements Serializable {
  @Id
  @Column(name = "user_id")
  private int userId;

  @Id
  @Column(name = "subject_id")
  private int subjectId;
}
