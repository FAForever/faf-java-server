package com.faforever.server.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

@Entity
@Table(name = "friends_and_foes")
@IdClass(SocialRelationPK.class)
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SocialRelation {

  @Id
  @Column(name = "user_id")
  private int userId;

  @Id
  @Column(name = "subject_id")
  private int subjectId;

  @Column(name = "status")
  @Enumerated(EnumType.STRING)
  private SocialRelationStatus status;
}
