package com.faforever.server.security;

import com.faforever.server.security.BanDetails.BanScope;
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
