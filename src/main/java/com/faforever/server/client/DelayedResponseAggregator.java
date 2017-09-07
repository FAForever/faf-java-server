package com.faforever.server.client;

import com.faforever.server.common.ServerMessage;

import java.util.List;
import java.util.function.Function;

@FunctionalInterface
interface DelayedResponseAggregator<IN extends ServerMessage, OUT extends ServerMessage> extends Function<List<DelayedResponse<IN>>, OUT> {
}
