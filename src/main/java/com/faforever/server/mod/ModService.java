package com.faforever.server.mod;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ModService {
  private final ModRepository modRepository;

  public ModService(ModRepository modRepository) {
    this.modRepository = modRepository;
  }

  public void getMods(List<String> modUids) {

  }

  public void isModRanked(byte modId) {

  }
}
