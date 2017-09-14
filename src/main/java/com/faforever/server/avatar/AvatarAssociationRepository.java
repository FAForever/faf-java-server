package com.faforever.server.avatar;

import com.faforever.server.entity.AvatarAssociation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AvatarAssociationRepository extends JpaRepository<AvatarAssociation, Integer> {

  @Modifying
  @Query("update AvatarAssociation set selected = case when (avatar.url = :avatarUrl) then 1 else 0 end where player.id = :playerId ")
  void selectAvatar(@Param("playerId") int playerId, @Param("avatarUrl") String avatarUrl);
}
