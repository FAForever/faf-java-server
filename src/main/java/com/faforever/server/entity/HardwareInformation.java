package com.faforever.server.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
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
public class HardwareInformation {

  @Id
  @Column(name = "id")
  @GeneratedValue
  private int id;

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
    joinColumns = @JoinColumn(name = "uniqueid_hash", referencedColumnName = "id"),
    inverseJoinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"))
  private Set<Player> players;
}
