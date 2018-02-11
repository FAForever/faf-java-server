package com.faforever.server.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "map_version")
@Immutable
@Data
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class MapVersion {
  @Id
  @Column(name = "id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "filename")
  private String filename;

  @Column(name = "ranked")
  private boolean ranked;

  @ManyToOne(optional = false)
  @JoinColumn(name = "map_id")
  private Map map;
}
