package com.youzidata.udpnettysocket.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
public class ASMGCSDto {

    private String root = "INTERFACE_LESFD";

    private Head head = new Head();

    private Body body = new Body();

    @Getter
    @Setter
    @ToString
    public class Head {
        private String sndtm;
        private String msgid;
        private String title;

    }

    @Getter
    @Setter
    @ToString
    public class Body {
        private String titles_subscribe;
        private String subrep;
        private Integer error;
        private String reason;
        private String unsubrep;
        private Rwylight rwylight;
        private Rwyalert rwyalert;
    }

    @Getter
    @Setter
    @ToString
    public static class Rwylight {
        private List<Runway> runway = new ArrayList<>();
    }

    @Getter
    @Setter
    @ToString
    public static class Rwyalert {
        private List<Runway> runway = new ArrayList<>();
    }

    @Getter
    @Setter
    @ToString
    public static class Runway {
        private String light;
        private String status;
        private String light_code;
        private String light_msg;
        private String rwy;
        public Runway(){}
        public Runway(String light, String status, String light_code, String light_msg, String rwy) {
            this.light = light;
            this.status = status;
            this.light_code = light_code;
            this.light_msg = light_msg;
            this.rwy = rwy;
        }
    }
}
