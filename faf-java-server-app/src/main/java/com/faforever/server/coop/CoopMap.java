package com.faforever.server.coop;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "coop_map")
@Data
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class CoopMap {

  @Id
  @Column(name = "id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "type")
  @Enumerated(EnumType.ORDINAL)
  private CoopMissionType type;

  @Column(name = "name")
  private String name;

  @Column(name = "description")
  private String description;

  @Column(name = "version")
  private Integer version;

  @Column(name = "filename")
  private String filename;
}
