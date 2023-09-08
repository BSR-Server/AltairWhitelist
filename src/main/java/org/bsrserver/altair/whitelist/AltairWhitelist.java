package org.bsrserver.altair.whitelist;

import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.slf4j.Logger;
import com.google.inject.Inject;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;

import org.bsrserver.altair.whitelist.config.Config;
import org.bsrserver.altair.whitelist.command.Command;
import org.bsrserver.altair.whitelist.data.WhitelistManager;
import org.bsrserver.altair.whitelist.event.ServerPreConnectEventEventEventListener;

@Plugin(
        id = "altairwhitelist",
        name = "Altair Whitelist",
        version = "1.0.0",
        url = "https://www.bsrserver.org:8443",
        description = "Whitelist Plugin",
        authors = {"Andy Zhang"}
)
public class AltairWhitelist {
    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private final ProxyServer proxyServer;
    private final Logger logger;
    private final Path dataDirectory;
    private final WhitelistManager whitelistManager;

    @Inject
    public AltairWhitelist(ProxyServer proxyServer, Logger logger, @DataDirectory Path dataDirectory) {
        this.proxyServer = proxyServer;
        this.logger = logger;
        this.dataDirectory = dataDirectory;

        // load config
        Config.getInstance().loadConfig(dataDirectory);

        // init data
        whitelistManager = new WhitelistManager(this);
    }

    @Subscribe
    public void onInitialize(ProxyInitializeEvent event) {
        // register event
        proxyServer.getEventManager().register(this, new ServerPreConnectEventEventEventListener(this));

        // register command
        proxyServer.getCommandManager().register(
                proxyServer.getCommandManager().metaBuilder("altairwhitelist").plugin(this).build(),
                new Command(this)
        );
    }

    public ScheduledExecutorService getScheduledExecutorService() {
        return scheduledExecutorService;
    }

    public ProxyServer getProxyServer() {
        return proxyServer;
    }

    public Logger getLogger() {
        return logger;
    }

    public Path getDataDirectory() {
        return dataDirectory;
    }

    public WhitelistManager getWhitelistManager() {
        return whitelistManager;
    }
}