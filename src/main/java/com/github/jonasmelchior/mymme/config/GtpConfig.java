package com.github.jonasmelchior.mymme.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "mme.gtp")
public interface GtpConfig {

    @WithDefault("127.0.0.1")
    String sgwIp();

    @WithDefault("2123")
    int sgwPort();

    @WithDefault("127.0.0.1")
    String localIp();

    @WithDefault("2123")
    int localPort();
}
