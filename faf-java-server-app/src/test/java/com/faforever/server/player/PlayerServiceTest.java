package com.faforever.server.player;

import com.faforever.server.client.ClientConnection;
import com.faforever.server.client.ClientService;
import com.faforever.server.geoip.GeoIpService;
import com.faforever.server.integration.Protocol;
import com.faforever.server.security.FafUserDetails;
import com.faforever.server.security.User;
import io.micrometer.core.instrument.MeterRegistry;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationEventPublisher;

import java.net.InetAddress;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PlayerServiceTest {
  private PlayerService instance;
  private Player player;

  @Mock
  private ClientService clientService;
  @Mock
  private MeterRegistry meterRegistry;
  @Mock
  private ApplicationEventPublisher eventPublisher;
  @Mock
  private GeoIpService geoIpService;

  @Before
  public void setUp() throws Exception {
    OnlinePlayerRepository onlinePlayerRepository = new FakeOnlinePlayerRepository();
    player = (Player) new Player().setId(1);
    player.setLogin("JUnit");
    instance = new PlayerService(clientService, meterRegistry, onlinePlayerRepository, eventPublisher, geoIpService);
  }

  @Test
  public void onClientDisconnectRemovesPlayerAndUnsetsGameAndRemovesGameIfLastPlayer() {
    FafUserDetails fafUserDetails = createFafUserDetails();

    when(geoIpService.lookupTimezone(any())).thenReturn(Optional.of(TimeZone.getDefault()));
    when(geoIpService.lookupCountryCode(any())).thenReturn(Optional.of("CH"));

    instance.setPlayerOnline(fafUserDetails.getPlayer());
    assertThat(instance.getOnlinePlayer(player.getId()).isPresent(), is(true));
    assertThat(player.getTimeZone(), is(TimeZone.getDefault()));
    assertThat(player.getCountry(), is("CH"));

    instance.removePlayer(fafUserDetails.getPlayer());

    assertThat(instance.getOnlinePlayer(player.getId()).isPresent(), is(false));
  }

  @Test
  public void isPlayerOnline() {
    FafUserDetails fafUserDetails = createFafUserDetails();

    assertThat(instance.isPlayerOnline(fafUserDetails.getPlayer().getLogin()), is(false));
    instance.setPlayerOnline(fafUserDetails.getPlayer());
    assertThat(instance.isPlayerOnline(fafUserDetails.getPlayer().getLogin()), is(true));
  }

  private FafUserDetails createFafUserDetails() {
    User user = new User();
    user.setPassword("pw");
    user.setLogin(player.getLogin());
    user.setCountry("CH");
    user.setPlayer(player);

    player.setClientConnection(new ClientConnection("1", Protocol.V1_LEGACY_UTF_16, mock(InetAddress.class)));

    return new FafUserDetails(user);
  }

  private class FakeOnlinePlayerRepository implements OnlinePlayerRepository {
    private final Map<Integer, Player> players;

    private FakeOnlinePlayerRepository() {
      players = new HashMap<>();
    }

    @NotNull
    @Override
    public <S extends Player> S save(@NotNull S entity) {
      players.put(entity.getId(), entity);
      return entity;
    }

    @NotNull
    @Override
    public <S extends Player> Iterable<S> saveAll(@NotNull Iterable<S> entities) {
      entities.forEach(s -> players.put(s.getId(), s));
      return entities;
    }

    @NotNull
    @Override
    public Optional<Player> findById(@NotNull Integer integer) {
      return Optional.ofNullable(players.get(integer));
    }

    @Override
    public boolean existsById(@NotNull Integer integer) {
      return players.containsKey(integer);
    }

    @NotNull
    @Override
    public Collection<Player> findAll() {
      return players.values();
    }

    @NotNull
    @Override
    public Iterable<Player> findAllById(@NotNull Iterable<Integer> integers) {
      Set<Integer> ids = StreamSupport.stream(integers.spliterator(), false)
        .collect(Collectors.toSet());

      return players.values().stream()
        .filter(player -> ids.contains(player.getId()))
        .collect(Collectors.toList());
    }

    @Override
    public long count() {
      return players.size();
    }

    @Override
    public void deleteById(@NotNull Integer integer) {
      players.remove(integer);
    }

    @Override
    public void delete(@NotNull Player entity) {
      players.remove(entity.getId());
    }

    @Override
    public void deleteAll(@NotNull Iterable<? extends Player> entities) {
      for (Player entity : entities) {
        players.remove(entity.getId());
      }
    }

    @Override
    public void deleteAll() {
      players.clear();
    }

    @Override
    public Optional<Player> findByLogin(String login) {
      return players.values().stream()
        .filter(player1 -> player1.getLogin().equals(login))
        .findFirst();
    }

    @Override
    public List<Player> findAllByCountry(String country) {
      return players.values().stream().filter(player -> player.getCountry() != null).collect(Collectors.toList());
    }
  }

}
