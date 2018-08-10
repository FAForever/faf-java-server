package com.faforever.server.security;

import com.faforever.server.security.GroupAssociation.Group;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class FafUserDetailsTest {

  private static final String TEST_PASSWORD = "pwd";
  private static final String TEST_USERNAME = "login";

  @Test
  public void userWithGroup1IsAdmin() throws Exception {
    User user = (User) new User()
      .setGroupAssociation(new GroupAssociation().setGroup(Group.ADMIN))
      .setPassword(TEST_PASSWORD)
      .setLogin(TEST_USERNAME);

    FafUserDetails fafUserDetails = new FafUserDetails(user);

    assertThat(fafUserDetails.getAuthorities(), CoreMatchers.hasItems(
      new SimpleGrantedAuthority("ROLE_USER"),
      new SimpleGrantedAuthority("ROLE_ADMIN")
    ));
  }

  @Test
  public void userWithoutGroupIsNotAdmin() throws Exception {
    User user = (User) new User()
      .setGroupAssociation(null)
      .setPassword(TEST_PASSWORD)
      .setLogin(TEST_USERNAME);

    FafUserDetails fafUserDetails = new FafUserDetails(user);

    assertThat(fafUserDetails.getAuthorities(), hasSize(1));
    assertThat(fafUserDetails.getAuthorities().iterator().next().getAuthority(), is("ROLE_USER"));
  }

  @Test
  public void userWithoutGroup1IsNotAdmin() throws Exception {
    User user = (User) new User()
      .setGroupAssociation(new GroupAssociation().setGroup(Group.MODERATOR))
      .setPassword(TEST_PASSWORD)
      .setLogin(TEST_USERNAME);

    FafUserDetails fafUserDetails = new FafUserDetails(user);

    assertThat(fafUserDetails.getAuthorities(), hasSize(1));
    assertThat(fafUserDetails.getAuthorities().iterator().next().getAuthority(), is("ROLE_USER"));
  }

  @Test
  public void userWithoutBanDetailsIsNonLocked() throws Exception {
    User user = (User) new User()
      .setPassword(TEST_PASSWORD)
      .setLogin(TEST_USERNAME);

    FafUserDetails fafUserDetails = new FafUserDetails(user);

    assertThat(fafUserDetails.isAccountNonLocked(), is(true));
  }

  @Test
  public void userWithValidBanDetailsIsLockedWithExpiry() throws Exception {
    User user = (User) new User()
      .setPassword(TEST_PASSWORD)
      .setLogin(TEST_USERNAME);
    user.getBanDetails().add(new BanDetails().setExpiresAt(Timestamp.from(Instant.now().plus(1, ChronoUnit.HOURS))));

    FafUserDetails fafUserDetails = new FafUserDetails(user);

    assertThat(fafUserDetails.isAccountNonLocked(), is(false));
  }

  @Test
  public void userWithValidBanDetailsIsLockedWithoutExpiry() throws Exception {
    User user = (User) new User()
      .setPassword(TEST_PASSWORD)
      .setLogin(TEST_USERNAME);
    user.getBanDetails().add(new BanDetails().setExpiresAt(null));

    FafUserDetails fafUserDetails = new FafUserDetails(user);

    assertThat(fafUserDetails.isAccountNonLocked(), is(false));
  }

  @Test
  public void userWithExpiredBanDetailsIsNonLocked() throws Exception {
    User user = (User) new User()
      .setPassword(TEST_PASSWORD)
      .setLogin(TEST_USERNAME);
    user.getBanDetails().add(new BanDetails().setExpiresAt(Timestamp.from(Instant.now().minusSeconds(1))));

    FafUserDetails fafUserDetails = new FafUserDetails(user);

    assertThat(fafUserDetails.isAccountNonLocked(), is(true));
  }
}
