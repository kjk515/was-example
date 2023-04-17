package com.jin.was.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jin.was.ResponseCode;

public record ServerConfig(
    int port,
    List<Host> hosts
) {
    record Host(
        String name,
        String appBase,
        AccessLogValve accessLogValve,
        Map<String, String> errorReportValve
    ) {
        record AccessLogValve(
            String directory,
            String prefix,
            String suffix
        ) {}
    }

    private static final ServerConfig INSTANCE;
    private static final String CONFIG_PATH = "server.json";
    private static final String DEFAULT_ERROR_PAGE_ROOT = "html";
    private static final String DEFAULT_APP_BASE = "webapps";

    static {
        try {
            INSTANCE = new ObjectMapper().readValue(ServerConfig.class.getClassLoader().getResource(CONFIG_PATH), ServerConfig.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static ServerConfig getInstance() {
        return INSTANCE;
    }

    public String appBasePath(String hostName) {
        Optional<Host> optionalHost = this.hosts.stream().filter(it -> it.name.equals(hostName)).findFirst();
        String appBase = null;
        if (optionalHost.isPresent()) {
            appBase = optionalHost.get().appBase;
        }
        if (appBase == null) {
            appBase = DEFAULT_APP_BASE;
        }

        return getClass().getClassLoader().getResource(appBase).getPath();
    }

    public InputStream errorPageStream(String hostName, ResponseCode responseCode) {
        Optional<Host> host = this.hosts.stream().filter(it -> it.name.equals(hostName)).findFirst();
        String errorPage = null;
        if (host.isPresent() && host.get().errorReportValve() != null) {
            errorPage = host.get().errorReportValve().get(responseCode.code());
        }
        if (errorPage == null) {
            errorPage = DEFAULT_ERROR_PAGE_ROOT + "/" + responseCode.code() + ".html";
        }

        return getClass().getClassLoader().getResourceAsStream(errorPage);
    }
}
