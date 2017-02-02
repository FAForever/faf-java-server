package com.faforever.server.security;

import com.faforever.server.entity.BanDetails;
import com.faforever.server.entity.User;
import com.faforever.server.entity.UserGroup;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class FafUserDetailsTest {
  @Before
  public void setUp() throws Exception {

  }

  @Test
  public void userWithGroup1IsAdmin() throws Exception {
    User user = (User) new User()
      .setUserGroup(new UserGroup().setGroup(1))
      .setPassword("pwd")
      .setLogin("login");

    FafUserDetails fafUserDetails = new FafUserDetails(user);

    assertThat(fafUserDetails.getAuthorities(), CoreMatchers.hasItems(
      new SimpleGrantedAuthority("ROLE_USER"),
      new SimpleGrantedAuthority("ROLE_ADMIN")
    ));
  }

  @Test
  public void userWithoutGroupIsNotAdmin() throws Exception {
    User user = (User) new User()
      .setUserGroup(null)
      .setPassword("pwd")
      .setLogin("login");

    FafUserDetails fafUserDetails = new FafUserDetails(user);

    assertThat(fafUserDetails.getAuthorities(), hasSize(1));
    assertThat(fafUserDetails.getAuthorities().iterator().next().getAuthority(), is("ROLE_USER"));
  }

  @Test
  public void userWithoutGroup1IsNotAdmin() throws Exception {
    User user = (User) new User()
      .setUserGroup(new UserGroup().setGroup(2))
      .setPassword("pwd")
      .setLogin("login");

    FafUserDetails fafUserDetails = new FafUserDetails(user);

    assertThat(fafUserDetails.getAuthorities(), hasSize(1));
    assertThat(fafUserDetails.getAuthorities().iterator().next().getAuthority(), is("ROLE_USER"));
  }

  @Test
  public void userWithoutBanDetailsIsNonLocked() throws Exception {
    User user = (User) new User()
      .setPassword("pwd")
      .setBanDetails(null)
      .setLogin("login");

    FafUserDetails fafUserDetails = new FafUserDetails(user);

    assertThat(fafUserDetails.isAccountNonLocked(), is(true));
  }

  @Test
  public void userWithValidBanDetailsIsLocked() throws Exception {
    User user = (User) new User()
      .setPassword("pwd")
      .setBanDetails(new BanDetails().setExpiresAt(Timestamp.from(Instant.now().plus(1, ChronoUnit.HOURS))))
      .setLogin("login");

    FafUserDetails fafUserDetails = new FafUserDetails(user);

    assertThat(fafUserDetails.isAccountNonLocked(), is(false));
  }

  @Test
  public void userWithExpiredBanDetailsIsNonLocked() throws Exception {
    User user = (User) new User()
      .setPassword("pwd")
      .setBanDetails(new BanDetails().setExpiresAt(Timestamp.from(Instant.now().minusSeconds(1))))
      .setLogin("login");

    FafUserDetails fafUserDetails = new FafUserDetails(user);

    assertThat(fafUserDetails.isAccountNonLocked(), is(true));
  }
}
