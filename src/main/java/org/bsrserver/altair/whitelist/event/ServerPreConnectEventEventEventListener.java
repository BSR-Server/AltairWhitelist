package org.bsrserver.altair.whitelist.event;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.kyori.adventure.text.Component;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;

import org.bsrserver.altair.whitelist.AltairWhitelist;

public class ServerPreConnectEventEventEventListener {
    private final AltairWhitelist altairWhitelist;

    public ServerPreConnectEventEventEventListener(AltairWhitelist altairWhitelist) {
        this.altairWhitelist = altairWhitelist;
    }

    @Subscribe
    public void onServerConnectedEvent(ServerPreConnectEvent event) {
        Map<String, Set<UUID>> whitelist = altairWhitelist.getWhitelistManager().getWhitelist();
        String serverName = event.getOriginalServer().getServerInfo().getName();
        UUID uuid = event.getPlayer().getUniqueId();

        // check is whitelisted
        if (!whitelist.containsKey(serverName) || !whitelist.get(serverName).contains(uuid)) {
            Component message = Component.text("§cYou are not white-listed on this server!");

            // user is first connection or switch server
            if (event.getPlayer().getCurrentServer().isPresent()) {
                event.setResult(ServerPreConnectEvent.ServerResult.denied());
                event.getPlayer().sendMessage(message);
            } else {
                event.getPlayer().disconnect(message);
            }
        }
    }
}
