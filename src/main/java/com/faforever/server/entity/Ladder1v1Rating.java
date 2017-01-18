package com.faforever.server.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "ladder1v1_rating")
@Getter
@Setter
@NoArgsConstructor
public class Ladder1v1Rating extends Rating {

  @Column(name = "winGames")
  private short winGames;

}
