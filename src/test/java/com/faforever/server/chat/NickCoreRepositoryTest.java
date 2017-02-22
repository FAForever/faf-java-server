package com.faforever.server.chat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.contains;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class NickCoreRepositoryTest {
  private NickCoreRepository instance;
  @Mock
  private NamedParameterJdbcTemplate jdbcTemplate;

  @Before
  public void setUp() throws Exception {
    instance = new NickCoreRepository(jdbcTemplate);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void updatePassword() throws Exception {
    instance.updatePassword("junit", "1234");

    verify(jdbcTemplate).update(contains("NickCore"), anyMap());
  }
}
