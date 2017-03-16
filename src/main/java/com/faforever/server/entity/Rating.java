package com.faforever.server.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;

@MappedSuperclass
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public abstract class Rating {

  @Id
  @Column(name = "id")
  private Integer id;

  @Column(name = "numGames")
  private int numGames;

  @Column(name = "is_active")
  private boolean isActive;

  @Column(name = "mean")
  private double mean;

  @Column(name = "deviation")
  private double deviation;

  @OneToOne
  @JoinColumn(name = "id", updatable = false, insertable = false)
  private Player player;
}
