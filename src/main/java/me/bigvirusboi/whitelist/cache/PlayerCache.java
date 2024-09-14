package me.bigvirusboi.whitelist.cache;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.beanium.lib.data.SimplePlayer;
import net.beanium.lib.tool.MinecraftAPI;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class PlayerCache {
	private final ProxyServer proxy;
	private final Map<String, SimplePlayer> cache = new HashMap<>();

	public PlayerCache(ProxyServer proxy) {
		this.proxy = proxy;
	}

	public SimplePlayer get(Player player) {
		SimplePlayer simplePlayer = new SimplePlayer(player.getUniqueId(), player.getUsername());
		cache.put(player.getUsername().toLowerCase(), simplePlayer);
		return simplePlayer;
	}

	public SimplePlayer getPlayer(String name) {
		SimplePlayer cachedResponse = cache.get(name.toLowerCase());
		if (cachedResponse != null) return cachedResponse;

		Optional<Player> optional = proxy.getPlayer(name);
		if (optional.isPresent()) {
			Player velocity = optional.get();

			SimplePlayer player = new SimplePlayer(velocity.getUniqueId(), velocity.getUsername());
			cache.put(velocity.getUsername(), player);
			return player;
		}

		SimplePlayer player = MinecraftAPI.getPlayer(name);
		if (player == null) return null;

		cache.put(player.name().toLowerCase(), player);
		return player;
	}
}
