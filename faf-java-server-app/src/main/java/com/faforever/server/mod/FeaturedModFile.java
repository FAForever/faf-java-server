package com.faforever.server.mod;

import lombok.Setter;
import org.hibernate.annotations.Immutable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Setter
@Immutable
@Entity
// Thanks to the dynamic table nature of the legacy updater, this class is not mapped to an underlying table but
// a native query instead. This is why the columns here can't be found in any table.
public class FeaturedModFile {

  private short fileId;
  private int version;

  // This is not actually the primary key, but JPA requires one.
  @Id
  @Column(name = "fileId")
  public short getFileId() {
    return fileId;
  }

  @Column(name = "version")
  public int getVersion() {
    return version;
  }
}
