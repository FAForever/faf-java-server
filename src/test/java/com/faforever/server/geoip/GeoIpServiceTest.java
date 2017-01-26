package com.faforever.server.geoip;

import com.faforever.server.config.ServerProperties;
import org.junit.Before;
import org.junit.Test;

import java.net.InetAddress;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class GeoIpServiceTest {

  private GeoIpService instance;

  @Before
  public void setUp() throws Exception {
    ServerProperties properties = new ServerProperties();
    properties.getGeoIp().setDatabaseUrl(getClass().getResource("/geoip/GeoLite2-Country.mmdb.gz").toURI().toString());

    instance = new GeoIpService(properties);
    instance.reloadDatabase();
  }

  @Test
  public void lookupCountry() throws Exception {
    String country = instance.lookupCountry(InetAddress.getByName("192.203.230.10"));
    assertThat(country, is("US"));
  }
}
