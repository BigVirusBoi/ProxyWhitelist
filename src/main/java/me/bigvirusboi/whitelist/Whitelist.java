package me.bigvirusboi.whitelist;

import com.google.inject.Inject;
import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.Getter;
import lombok.Setter;
import me.bigvirusboi.whitelist.cache.PlayerCache;
import me.bigvirusboi.whitelist.command.WhitelistCommand;
import net.beanium.lib.data.JSON;
import net.beanium.lib.data.SimplePlayer;
import net.beanium.lib.time.TimeUtil;
import net.beanium.lib.velocity.command.CommandManager;
import net.kyori.adventure.text.Component;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Logger;

@Plugin(id = "whitelist", name = "Whitelist", version = BuildConstants.VERSION,
		description = "Adds an advanced whitelist to Velocity", authors = {"BigVirusBoi"},
		dependencies = {@Dependency(id = "beanlib")})
public final class Whitelist {
	private final ProxyServer proxy;
	private final Logger logger;
	private final Path dataDirectory;

	private final PlayerCache cache;

	@Inject
	public Whitelist(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
		this.proxy = server;
		this.logger = logger;
		this.dataDirectory = dataDirectory;

		logger.info("ProxyWhitelist is loading");
		this.cache = new PlayerCache(proxy);

		load();
	}

	@Subscribe
	public void onProxyInitialization(ProxyInitializeEvent event) {
		proxy.getEventManager().register(this, LoginEvent.class, e -> {
			Player player = e.getPlayer();
			if (!enabled) return;
			if (isExpired(cache.get(player))) {
				e.setResult(ResultedEvent.ComponentResult.denied(Component.text("§cYour whitelist expired")));
				return;
			}
			if (!isWhitelisted(player)) {
				e.setResult(ResultedEvent.ComponentResult.denied(Component.text("§cYou are not whitelisted")));
				return;
			}
		});

		CommandManager manager = new CommandManager(this, proxy);
		manager.register(new WhitelistCommand(proxy, this, cache));
	}

	@Getter
	@Setter
	private boolean enabled = false;
	@Getter
	private final Map<SimplePlayer, Long> whitelisted = new TreeMap<>(Comparator.comparing(SimplePlayer::name));

	public String getDurationFormatted(SimplePlayer player) {
		if (!whitelisted.containsKey(player)) return "§4NONE";

		long expire = whitelisted.get(player);
		if (expire == -1) {
			return "§2INFINITE";
		}
		if (System.currentTimeMillis() > expire) {
			return "§cEXPIRED";
		}

		return "§a" + TimeUtil.formatToSeconds(expire - System.currentTimeMillis());
	}

	public long getDuration(UUID uuid) {
		for (SimplePlayer player : whitelisted.keySet()) {
			if (player.uuid().equals(uuid)) return whitelisted.get(player);
		}
		return 0;
	}

	public boolean isExpired(SimplePlayer player) {
		return !isWhitelisted(player) && contains(player);
	}

	private boolean isWhitelisted(Long timestamp) {
		if (timestamp == null) return false;
		if (timestamp == -1) return true;
		return timestamp > System.currentTimeMillis();
	}

	public boolean isWhitelisted(UUID uuid) {
		return isWhitelisted(getDuration(uuid));
	}

	public boolean isWhitelisted(SimplePlayer player) {
		return isWhitelisted(whitelisted.get(player));
	}

	public boolean isWhitelisted(Player player) {
		return isWhitelisted(player.getUniqueId());
	}

	public void kickNotWhitelisted() {
		for (Player player : proxy.getAllPlayers()) {
			if (isWhitelisted(player)) continue;

			if (isExpired(cache.get(player))) {
				player.disconnect(Component.text("§cYour whitelist expired"));
			} else {
				player.disconnect(Component.text("§cYou are not whitelisted"));
			}
		}
	}

	public void clear(boolean expiredOnly) {
		if (expiredOnly) {
			List<SimplePlayer> players = new ArrayList<>();
			for (SimplePlayer cachedPlayer : whitelisted.keySet()) {
				if (!isWhitelisted(cachedPlayer)) players.add(cachedPlayer);
			}
			players.forEach(whitelisted::remove);
		} else {
			whitelisted.clear();
		}
		save();
	}

	public boolean isEmpty() {
		return whitelisted.isEmpty();
	}

	public boolean contains(SimplePlayer player) {
		return whitelisted.containsKey(player);
	}

	public void remove(SimplePlayer player) {
		whitelisted.remove(player);
		save();
	}

	public void set(SimplePlayer player, long expire) {
		whitelisted.put(player, expire);
		save();
	}

	private void load() {
		JSON data = JSON.loadFile(getFile(), new JSON());

		enabled = data.getBoolean("enabled", false);

		for (JSON entry : data.getObjectArray("whitelist")) {
			UUID uuid = entry.getUUID("uuid");
			String name = entry.getString("name");
			long expire = entry.getLong("expire");

			whitelisted.put(new SimplePlayer(uuid, name), expire);
		}
	}

	public void save() {
		JSON data = new JSON();
		data.set("enabled", enabled);

		List<JSON> whitelist = new ArrayList<>();
		for (SimplePlayer player : whitelisted.keySet()) {
			if (!isWhitelisted(player)) continue;

			JSON entry = new JSON();
			entry.set("uuid", player.uuid().toString());
			entry.set("name", player.name());
			entry.set("expire", whitelisted.get(player));
			whitelist.add(entry);
		}
		data.set("whitelist", whitelist);

		data.save(getFile());
	}

	private File getFile() {
		return new File(dataDirectory.toFile(), "whitelist.json");
	}
}
