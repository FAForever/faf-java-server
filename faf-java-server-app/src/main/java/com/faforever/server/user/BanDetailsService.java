package com.faforever.server.user;

import com.faforever.server.entity.BanDetails;
import com.faforever.server.entity.BanDetails.BanScope;
import com.faforever.server.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class BanDetailsService {
  private final BanDetailsRepository banDetailsRepository;

  public BanDetailsService(BanDetailsRepository banDetailsRepository) {
    this.banDetailsRepository = banDetailsRepository;
  }

  @Transactional
  public void banUser(int userId, int authorId, BanScope scope, String reason) {
    BanDetails banDetails = new BanDetails()
      .setUser((User) new User().setId(userId))
      .setAuthor((User) new User().setId(authorId))
      .setReason(reason)
      .setScope(scope);

    banDetailsRepository.save(banDetails);
  }
}
