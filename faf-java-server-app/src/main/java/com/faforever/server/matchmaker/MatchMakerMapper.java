package com.faforever.server.matchmaker;

import com.faforever.server.game.GameParticipant;
import com.faforever.server.matchmaker.CreateMatchRequest.Participant;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MatchMakerMapper {
  List<GameParticipant> map(List<Participant> participants);

  GameParticipant map(Participant participants);
}
