package com.faforever.server.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.Set;

@Entity
@Table(name = "uniqueid")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class HardwareInformation {

  // TODO make this @Id as soon as the DB relations have been fixed
  @Column(name = "id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  // TODO This isn't actually the ID, but it's used as the join column by the shitty DB schema
  @Id
  @Column(name = "hash")
  private String hash;

  @Column(name = "uuid")
  private String uuid;

  @Column(name = "mem_SerialNumber")
  private String memSerialNumber;

  @Column(name = "deviceID")
  private String deviceId;

  @Column(name = "manufacturer")
  private String manufacturer;

  @Column(name = "name")
  private String name;

  @Column(name = "processorId")
  private String processorId;

  @Column(name = "SMBIOSBIOSVersion")
  private String smbiosbiosVersion;

  @Column(name = "serialNumber")
  private String serialNumber;

  @Column(name = "volumeSerialNumber")
  private String volumeSerialNumber;

  @ManyToMany
  @JoinTable(name = "unique_id_users",
    // TODO make DB tables use "id" as a foreign key instead of "hash"
    joinColumns = @JoinColumn(name = "uniqueid_hash", referencedColumnName = "hash"),
    inverseJoinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"))
  private Set<Player> players;
}
