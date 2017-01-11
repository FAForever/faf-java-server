package com.faforever.server.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "\"mod\"")
@Immutable
@Data
@NoArgsConstructor
@ToString(of = {"id", "displayName"}, includeFieldNames = false)
public class Mod {
  @Id
  @Column(name = "id")
  @GeneratedValue
  private int id;

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
