package com.faforever.server.entity;

import lombok.Setter;
import org.hibernate.annotations.Immutable;
import org.jetbrains.annotations.NotNull;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "clan")
@Setter
@Immutable
public class Clan {

  private int id;
  private OffsetDateTime createTime;
  private OffsetDateTime updateTime;
  private String name;
  private String tag;
  private Player founder;
  private Player leader;
  private String description;
  private String tagColor;
  private List<ClanMembership> memberships;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  public int getId() {
    return id;
  }

  @Column(name = "create_time")
  public OffsetDateTime getCreateTime() {
    return createTime;
  }

  @Column(name = "update_time")
  public OffsetDateTime getUpdateTime() {
    return updateTime;
  }

  @Column(name = "name")
  @NotNull
  public String getName() {
    return name;
  }

  @Column(name = "tag")
  @Size(max = 3)
  @NotNull
  public String getTag() {
    return tag;
  }

  @ManyToOne
  @JoinColumn(name = "founder_id")
  public Player getFounder() {
    return founder;
  }

  @ManyToOne
  @JoinColumn(name = "leader_id")
  public Player getLeader() {
    return leader;
  }

  @Column(name = "description")
  public String getDescription() {
    return description;
  }

  @Column(name = "tag_color")
  public String getTagColor() {
    return tagColor;
  }

  @OneToMany(mappedBy = "clan")
  public List<ClanMembership> getMemberships() {
    return this.memberships;
  }
}
