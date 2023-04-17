package com.jin.was.config;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jin.was.ResponseCode;
import com.jin.was.util.PathUtils;

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
    private static final String CONFIG_PATH = "conf/server.json";
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
        String appBase = DEFAULT_APP_BASE;
        if (optionalHost.isPresent()) {
            appBase = optionalHost.get().appBase;
        }
        return getClass().getResource(PathUtils.affixRootPath(appBase)).getPath();
    }

    public String errorPagePath(String hostName, ResponseCode responseCode) {
        Host host = this.hosts.stream().filter(it -> it.name.equals(hostName)).findFirst().get(); // TODO NULL
        String errorPage = host.errorReportValve().get(responseCode.code());
        return errorPage != null ? errorPage : DEFAULT_ERROR_PAGE_ROOT + PathUtils.affixRootPath(responseCode.code()) + ".html";
    }
}
