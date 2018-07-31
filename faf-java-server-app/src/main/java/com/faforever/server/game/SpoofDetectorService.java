package com.faforever.server.game;

import com.faforever.server.client.ClientService;
import com.faforever.server.entity.Avatar;
import com.faforever.server.entity.AvatarAssociation;
import com.faforever.server.entity.Game;
import com.faforever.server.entity.Player;
import com.faforever.server.entity.Rating;
import com.faforever.server.error.ErrorCode;
import com.faforever.server.error.Requests;
import com.faforever.server.player.PlayerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * When players joins a game, they send their own information (like name and rating) to other players. This information
 * can, therefore, not be trusted. Players can send the information they received from others to this service in order
 * to have it verified. If this service receives multiple reports of incorrect data, it will tell all peers to
 * disconnect the player who spoofs its data.
 */
@Service
@Slf4j
public class SpoofDetectorService {

  private final PlayerService playerService;
  private final ClientService clientService;

  public SpoofDetectorService(PlayerService playerService, ClientService clientService) {
    this.playerService = playerService;
    this.clientService = clientService;
  }

  public void verifyPlayer(Player reporter, int reporteeId, String name, float mean, float deviation, String country, String avatarUrl, String avatarDescription) {
    Game reporterGame = reporter.getCurrentGame();
    Requests.verify(reporterGame != null, ErrorCode.THIS_PLAYER_NOT_IN_GAME);

    Optional<Player> reporteeOptional = playerService.getOnlinePlayer(reporteeId);
    Requests.verify(reporteeOptional.isPresent(), ErrorCode.PLAYER_NOT_ONLINE, reporteeId);
    Player reportee = reporteeOptional.get();

    Game reporteeGame = reportee.getCurrentGame();
    Requests.verify(reporteeGame != null, ErrorCode.OTHER_PLAYER_NOT_IN_GAME, reporteeId);

    Requests.verify(reporterGame.equals(reporteeGame), ErrorCode.NOT_SAME_GAME, reportee);

    boolean passesValidation = isNameCorrect(reporter, name, reportee)
      && isRatingCorrect(reporter, mean, deviation, reportee)
      && isCountryCorrect(reporter, country, reportee)
      && isAvatarDataCorrect(reporter, avatarUrl, avatarDescription, reportee);

    if (!passesValidation) {
      reportee.getFraudReporterIds().add(reporter.getId());

      int reportedFrauds = reportee.getFraudReporterIds().size();
      if (reportedFrauds > 1 && reportedFrauds >= reporterGame.getConnectedPlayers().size() / 2) {
        Collection<Player> peers = reporteeGame.getConnectedPlayers().values().stream()
          .filter(player -> !Objects.equals(player.getId(), reporteeId))
          .collect(Collectors.toList());

        clientService.disconnectPlayerFromGame(reporteeId, peers);
      }
    }
  }

  private boolean isAvatarDataCorrect(Player reporter, String avatarUrl, String avatarDescription, Player reportee) {
    Optional<Avatar> optionalAvatar = reportee.getAvailableAvatars().stream()
      .filter(AvatarAssociation::isSelected)
      .map(AvatarAssociation::getAvatar)
      .findFirst();
    if (optionalAvatar.isPresent()) {
      Avatar avatar = optionalAvatar.get();
      if (!Objects.equals(avatarUrl, avatar.getUrl())) {
        log.debug("Avatar URL '{}' of player '{}' does not match in-game URL '{}' as reported by player '{}'",
          avatarUrl, reportee, avatar.getUrl(), reporter);
        return false;
      }
      if (!Objects.equals(avatarDescription, avatar.getDescription())) {
        log.debug("Avatar description '{}' of player '{}' does not match in-game description '{}' as reported by player '{}'",
          avatarDescription, reportee, avatar.getDescription(), reporter);
        return false;
      }
    }
    return true;
  }

  private boolean isCountryCorrect(Player reporter, String country, Player reportee) {
    if (!Objects.equals(country, reportee.getCountry())) {
      log.debug("Country '{}' of player '{}' does not match in-game country '{}' as reported by player '{}'",
        reportee.getCountry(), reportee, country, reporter);
      return false;
    }
    return true;
  }

  private boolean isRatingCorrect(Player reporter, float mean, float deviation, Player reportee) {
    Rating reporteeRating = reportee.getRatingWithinCurrentGame();

    if (!Objects.equals(mean, (float) reporteeRating.getMean()) || !Objects.equals(deviation, (float) reporteeRating.getDeviation())) {
      log.debug("Rating '{}/{}' of player '{}' does not match in-game rating '{}/{}' as reported by player '{}'",
        reporteeRating.getMean(), reporteeRating.getDeviation(), reportee, mean, deviation, reporter);
      return false;
    }
    return true;
  }

  private boolean isNameCorrect(Player reporter, String name, Player reportee) {
    if (!Objects.equals(name, reportee.getLogin())) {
      log.debug("Name of player '{}' does not match in-game name '{}' as reported by player '{}'", reportee, name, reporter);
      return false;
    }
    return true;
  }
}
