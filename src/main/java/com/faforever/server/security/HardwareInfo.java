package com.faforever.server.security;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
class HardwareInfo {
  private Machine machine;

  @Data
  public static class Machine {
    private String uuid;
    private Memory memory;
    private Disks disks;
    private Bios bios;
    private Processor processor;

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
      @JsonProperty("smbbversion")
      private String smbbVersion;
      private String serial;
    }

    @Data
    static class Processor {
      private String name;
      private String id;
    }
  }
}
