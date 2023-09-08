package org.bsrserver.altair.whitelist.command;

import java.util.List;
import java.util.stream.Stream;

import net.kyori.adventure.text.Component;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;

import org.bsrserver.altair.whitelist.AltairWhitelist;

public class Command implements SimpleCommand {
    private final AltairWhitelist altairWhitelist;

    public Command(AltairWhitelist altairWhitelist) {
        this.altairWhitelist = altairWhitelist;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        // execute
        if (source instanceof ConsoleCommandSource) {
            if (args.length == 1 && args[0].equals("update")) {

                altairWhitelist.getWhitelistManager().updateWhitelist();
                source.sendMessage(Component.text("§aUpdate success."));
            }
        } else {
            source.sendMessage(Component.text("§cThis command can only be executed by console."));
        }
    }

    @Override
    public List<String> suggest(final Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        // return empty list if source is player
        if (source instanceof Player) {
            return List.of();
        }

        // suggests
        if (args.length <= 1) {
            return Stream.of("update")
                    .filter(s -> s.startsWith(args.length > 0 ? args[0] : ""))
                    .toList();
        } else {
            return List.of();
        }
    }
}
