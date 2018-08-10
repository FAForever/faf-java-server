package com.faforever.server.avatar;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AvatarAssociationRepository extends JpaRepository<AvatarAssociation, Integer> {

  /**
   * @deprecated the avatar's URL shouldn't be in the database, nor should it be used to select an avatar. This is only
   * for backwards compatibility and will be removed in future.
   */
  @Modifying
  @Query("update AvatarAssociation set selected = case when (avatar.url = :avatarUrl) then 1 else 0 end where player.id = :playerId ")
  @Deprecated
  void selectAvatar(@Param("playerId") int playerId, @Param("avatarUrl") String avatarUrl);

  @Modifying
  @Query("update AvatarAssociation set selected = case when (avatar.id = :avatarId) then 1 else 0 end where player.id = :playerId ")
  void selectAvatar(@Param("playerId") int playerId, @Param("avatarId") int avatarId);
}
