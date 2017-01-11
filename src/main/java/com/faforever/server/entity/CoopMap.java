package com.faforever.server.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "coop_map")
@Data
@NoArgsConstructor
public class CoopMap {

  @Id
  @Column(name = "id")
  @GeneratedValue
  private int id;

  @Column(name = "type")
  private byte type;

  @Column(name = "name")
  private String name;

  @Column(name = "description")
  private String description;

  @Column(name = "version")
  private Integer version;

  @Column(name = "filename")
  private String filename;
}
