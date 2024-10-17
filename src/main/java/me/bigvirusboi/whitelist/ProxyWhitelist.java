package me.bigvirusboi.whitelist;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import me.bigvirusboi.whitelist.cache.PlayerCache;
import me.bigvirusboi.whitelist.command.WhitelistCommand;
import me.bigvirusboi.whitelist.manager.Whitelist;
import me.bigvirusboi.whitelist.util.Constants;
import net.kyori.adventure.text.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Logger;

@Plugin(id = "whitelist", name = "ProxyWhitelist", version = BuildConstants.VERSION,
		description = "Advanced whitelist system for Velocity", authors = {"BigVirusBoi"})
public final class ProxyWhitelist {
	private final ProxyServer server;

	private final PlayerCache cache;
	private final Whitelist whitelist;

	@Inject
	public ProxyWhitelist(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
		this.server = server;

		logger.info("ProxyWhitelist is loading");
		this.cache = new PlayerCache(server);
		this.whitelist = new Whitelist(server, cache, dataDirectory);

		try {
			whitelist.load();
		} catch (IOException e) {
			throw new RuntimeException("Unable to load whitelist", e);
		}
	}

	@Subscribe
	public void onProxyInitialization(ProxyInitializeEvent event) {
		server.getEventManager().register(this, LoginEvent.class, e -> {
			Player player = e.getPlayer();
			if (!whitelist.isEnabled()) return;
			if (whitelist.isExpired(cache.get(player))) {
				e.setResult(ResultedEvent.ComponentResult.denied(Component.text("§cYour whitelist expired")));
				return;
			}
			if (!whitelist.isWhitelisted(player)) {
				e.setResult(ResultedEvent.ComponentResult.denied(Component.text("§cYou are not whitelisted")));
			}
		});

		CommandManager manager = server.getCommandManager();
		manager.register(manager.metaBuilder(Constants.MAIN_COMMAND_NAME).aliases(Constants.COMMAND_ALIASES)
				.plugin(this).build(), new WhitelistCommand(server, whitelist, cache).getCommand());
	}
}
