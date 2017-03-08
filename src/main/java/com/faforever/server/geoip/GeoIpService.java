package com.faforever.server.geoip;

import com.faforever.server.config.ServerProperties;
import com.faforever.server.config.ServerProperties.GeoIp;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.DatabaseReader.Builder;
import com.maxmind.geoip2.exception.AddressNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.zip.GZIPInputStream;

import static com.github.nocatch.NoCatch.noCatch;

@Service
@Slf4j
public class GeoIpService {

  private final ServerProperties properties;
  private final Object DATABASE_LOCK = new Object();
  private DatabaseReader databaseReader;

  public GeoIpService(ServerProperties properties) throws IOException {
    this.properties = properties;
  }

  @PostConstruct
  public void postConstruct() throws IOException {
    Path databaseFile = properties.getGeoIp().getDatabaseFile();
    if (Files.notExists(databaseFile)) {
      updateDatabaseFile();
    }
    readDatabase(databaseFile);
  }

  @Scheduled(cron = "0 0 0 * * WED")
  public void updateDatabaseFile() throws IOException {
    GeoIp geoIp = properties.getGeoIp();
    Path geoIpFile = geoIp.getDatabaseFile();
    String databaseUrl = geoIp.getDatabaseUrl();
    log.debug("Downloading GeoIp database from '{}' to '{}'", databaseUrl, geoIpFile.toAbsolutePath());

    synchronized (DATABASE_LOCK) {
      URL url = new URL(databaseUrl);
      try (InputStream inputStream = new BufferedInputStream(new GZIPInputStream(url.openStream()))) {
        Files.createDirectories(geoIpFile.getParent());
        Files.copy(inputStream, geoIpFile, StandardCopyOption.REPLACE_EXISTING);
      }
      readDatabase(geoIpFile);
    }
  }

  public Optional<String> lookupCountryCode(InetAddress inetAddress) {
    Assert.state(databaseReader != null, "Database has not been initialized");
    return noCatch(() -> {
      try {
        return Optional.of(databaseReader.country(inetAddress).getCountry().getIsoCode());
      } catch (AddressNotFoundException e) {
        log.warn("No entry for address: {}", inetAddress);
        return Optional.empty();
      }
    });
  }

  private void readDatabase(Path geoIpFile) throws IOException {
    databaseReader = new Builder(geoIpFile.toFile()).build();
  }
}
