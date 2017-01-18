package com.faforever.server.security;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
class UidPayload {
  private String session;
  private Machine machine;
  private Desktop desktop;

  @Data
  public static class Desktop {
    private int width;
    private int height;
  }

  @Data
  public static class Machine {
    private String uuid;
    private Memory memory;
    private Disks disks;
    private Bios bios;
    private Processor processor;
    private Motherboard motherboard;
    private Model model;
    private OperatingSystem os;

    @Data
    static class Memory {
      private String serial0;
    }

    @Data
    static class Disks {
      @JsonProperty("controller_id")
      private String controllerId;
      @JsonProperty("vserial")
      private String vSerial;
    }

    @Data
    static class Bios {
      private String manufacturer;
      private String version;
      private String date;
      private String serial;
      private String description;
      @JsonProperty("smbbversion")
      private String smbbVersion;
    }

    @Data
    static class Processor {
      private String name;
      private String id;
    }

    @Data
    static class Motherboard {
      private String vendor;
      private String name;
    }

    @Data
    static class Model {
      private String name;
      private String manufacturer;
    }

    @Data
    static class OperatingSystem {
      private String version;
      private String type;
    }
  }
}
