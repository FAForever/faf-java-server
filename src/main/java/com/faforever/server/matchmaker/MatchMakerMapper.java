package com.faforever.server.matchmaker;

import com.faforever.server.matchmaker.CreateMatchRequest.Participant;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MatchMakerMapper {
  List<MatchParticipant> map(List<Participant> participants);

  MatchParticipant map(Participant participants);
}
