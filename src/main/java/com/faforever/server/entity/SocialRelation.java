package com.faforever.server.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "friends_and_foes")
@IdClass(SocialRelationPK.class)
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SocialRelation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "user_id")
  private Integer playerId;

  @ManyToOne
  @JoinColumn(name = "user_id", updatable = false, insertable = false)
  private Player player;

  @Id
  @Column(name = "subject_id")
  private Integer subjectId;

  @Column(name = "status")
  @Enumerated(EnumType.STRING)
  private SocialRelationStatus status;
}
