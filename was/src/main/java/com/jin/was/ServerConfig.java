package com.jin.was;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

public record ServerConfig(
    List<Host> hosts,
    Connector connector
) {

    record Host(
        String name,
        String appBase,
        AccessLogValve accessLogValve,
        List<Map<String, String>> errorReportValve
    ) {

        record AccessLogValve(
            String directory,
            String prefix,
            String suffix
        ) {

        }
    }

    record Connector(
        int port
    ) {

    }

    private static final ServerConfig INSTANCE;

    static {
        try {
            // TODO : 하드코딩
            INSTANCE = new ObjectMapper().readValue(ServerConfig.class.getClassLoader().getResource("conf/server.json"), ServerConfig.class);
        } catch (IOException e) {
            throw new RuntimeException(e); // TODO
        }
    }

    public static ServerConfig getInstance() {
        return INSTANCE;
    }

    public int port() {
        return this.connector.port;
    }

    public String appBasePath(String hostName) {
        Host host = this.hosts.stream().filter(it -> it.name.equals(hostName)).findFirst().get(); // TODO NULL
        return getClass().getResource(host.appBase.startsWith("/") ? host.appBase : "/" + host.appBase).getPath(); // TODO NULL
    }

    public String errorPagePath(String hostName, ErrorCode errorCode) {
        Host host = this.hosts.stream().filter(it -> it.name.equals(hostName)).findFirst().get(); // TODO NULL
        if (host.errorReportValve().isEmpty()) {
            return "html/" + errorCode.code + ".html";
        }
        return "html/" + errorCode.code + ".html";
    }
}
