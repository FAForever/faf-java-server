package com.faforever.server.chat;

import com.google.common.hash.Hashing;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ChatServiceTest {
  private ChatService instance;

  @Mock
  private NickCoreRepository nickCoreRepository;

  @Before
  public void setUp() throws Exception {
    instance = new ChatService(nickCoreRepository);
  }

  @Test
  public void updateIrcPassword() throws Exception {
    instance.updateIrcPassword("junit", "1234");

    verify(nickCoreRepository).updatePassword("junit", Hashing.md5().hashString("1234", StandardCharsets.UTF_8).toString());
  }
}
