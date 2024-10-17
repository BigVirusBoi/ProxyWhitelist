package me.bigvirusboi.whitelist.cache;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class PlayerCache {
	private final ProxyServer proxy;
	private final Map<String, CachedPlayer> cache = new HashMap<>();

	public PlayerCache(ProxyServer proxy) {
		this.proxy = proxy;
	}

	public CachedPlayer get(Player player) {
		CachedPlayer cachedPlayer = new CachedPlayer(player.getUniqueId(), player.getUsername());
		cache.put(player.getUsername().toLowerCase(), cachedPlayer);
		return cachedPlayer;
	}

	public CachedPlayer getPlayer(String name) {
		CachedPlayer cachedResponse = cache.get(name.toLowerCase());
		if (cachedResponse != null) return cachedResponse;

		Optional<Player> optional = proxy.getPlayer(name);
		if (optional.isPresent()) {
			Player velocity = optional.get();

			CachedPlayer player = new CachedPlayer(velocity.getUniqueId(), velocity.getUsername());
			cache.put(velocity.getUsername(), player);
			return player;
		}

		CachedPlayer player = MinecraftAPI.getPlayer(name);
		if (player == null) return null;

		cache.put(player.name().toLowerCase(), player);
		return player;
	}
}
