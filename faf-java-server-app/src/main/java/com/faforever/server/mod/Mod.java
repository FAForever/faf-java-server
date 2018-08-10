package com.faforever.server.mod;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Immutable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;

@Entity
@Table(name = "\"mod\"")
@Immutable
@Data
@NoArgsConstructor
@ToString(of = {"id", "displayName"}, includeFieldNames = false)
@EqualsAndHashCode(of = "id")
public class Mod {
  @Id
  @Column(name = "id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "display_name")
  private String displayName;

  @Column(name = "author")
  private String author;

  @Column(name = "uploader")
  private int uploader;

  @Column(name = "create_time")
  private Timestamp createTime;

  @Column(name = "update_time")
  private Timestamp updateTime;

}
