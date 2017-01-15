package com.faforever.server.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "uniqueid_exempt")
@Data
@NoArgsConstructor
public class UniqueIdExempt {

  @Id
  @Column(name = "user_id")
  private int id;

  @OneToOne
  @JoinColumn(name = "user_id", updatable = false)
  private Player player;

  @Column(name = "reason")
  private String reason;

}
