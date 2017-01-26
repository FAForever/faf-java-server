package com.faforever.server.geoip;

import com.faforever.server.config.ServerProperties;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.DatabaseReader.Builder;
import com.maxmind.geoip2.exception.AddressNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.zip.GZIPInputStream;

import static com.github.nocatch.NoCatch.noCatch;
import static java.nio.file.Files.delete;

@Service
@Slf4j
public class GeoIpService {

  private final ServerProperties properties;
  private final Object DATABASE_LOCK = new Object();
  private DatabaseReader databaseReader;
  private Path geoIpFile;

  public GeoIpService(ServerProperties properties) throws IOException {
    this.properties = properties;
  }

  @PostConstruct
  @Scheduled(cron = "0 0 * * * WED")
  public void reloadDatabase() throws IOException {
    Optional.ofNullable(geoIpFile).ifPresent(path -> noCatch(() -> delete(path)));
    geoIpFile = Files.createTempFile("geoip", ".tmp");

    synchronized (DATABASE_LOCK) {
      String databaseUrl = properties.getGeoIp().getDatabaseUrl();
      URL url = new URL(databaseUrl);
      try (InputStream inputStream = new BufferedInputStream(new GZIPInputStream(url.openStream()))) {
        databaseReader = new Builder(inputStream).build();
      }
    }
  }

  public Optional<String> lookupCountryCode(InetAddress inetAddress) {
    return noCatch(() -> {
      try {
        return Optional.of(databaseReader.country(inetAddress).getCountry().getIsoCode());
      } catch (AddressNotFoundException e) {
        log.warn("Address lookup failed", e);
        return Optional.empty();
      }
    });
  }
}
