package me.bigvirusboi.whitelist.manager;

import com.google.gson.*;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.Getter;
import lombok.Setter;
import me.bigvirusboi.whitelist.cache.CachedPlayer;
import me.bigvirusboi.whitelist.cache.PlayerCache;
import me.bigvirusboi.whitelist.util.Permissions;
import me.bigvirusboi.whitelist.util.TimeUtil;
import me.bigvirusboi.whitelist.util.Util;
import net.kyori.adventure.text.Component;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;

@Getter
@Setter
public class Whitelist {
	private final ProxyServer proxy;
	private final PlayerCache cache;
	private final Path dataDirectory;
	private final Gson gson;

	private boolean enabled = false;
	private final Map<CachedPlayer, Long> whitelisted = new TreeMap<>(Comparator.comparing(CachedPlayer::name));

	public Whitelist(ProxyServer proxy, PlayerCache cache, Path dataDirectory) {
		this.proxy = proxy;
		this.cache = cache;
		this.dataDirectory = dataDirectory;
		this.gson = new GsonBuilder().create();
	}

	public String getDurationFormatted(CachedPlayer player) {
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
		for (CachedPlayer player : whitelisted.keySet()) {
			if (player.uuid().equals(uuid)) return whitelisted.get(player);
		}
		return 0;
	}

	public boolean isExpired(CachedPlayer player) {
		return !isWhitelisted(player) && contains(player);
	}

	private boolean checkWhitelistStatus(Long timestamp) {
		if (timestamp == null) return false;
		if (timestamp == -1) return true;
		return timestamp > System.currentTimeMillis();
	}

	public boolean isWhitelisted(CachedPlayer player) {
		return checkWhitelistStatus(whitelisted.get(player));
	}

	public boolean isWhitelisted(Player player) {
		if (player.hasPermission(Permissions.BYPASS)) return true;
		return checkWhitelistStatus(getDuration(player.getUniqueId()));
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
			List<CachedPlayer> players = new ArrayList<>();
			for (CachedPlayer cachedPlayer : whitelisted.keySet()) {
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

	public boolean contains(CachedPlayer player) {
		return whitelisted.containsKey(player);
	}

	public void remove(CachedPlayer player) {
		whitelisted.remove(player);
		save();
	}

	public void set(CachedPlayer player, long expire) {
		whitelisted.put(player, expire);
		save();
	}

	public void load() throws IOException {
		File file = getFile();
		if (!file.exists()) return;

		FileReader fileReader = new FileReader(file, StandardCharsets.UTF_8);
		var element = JsonParser.parseReader(fileReader);
		fileReader.close();

		if (element == null) return;
		if (!element.isJsonObject()) return;

		JsonObject object = element.getAsJsonObject();

		enabled = object.has("enabled") && object.get("enabled").getAsBoolean();

		for (JsonElement entryEl : object.getAsJsonArray("whitelist")) {
			JsonObject entry = entryEl.getAsJsonObject();

			UUID uuid = Util.parseUUID(entry.get("uuid").getAsString());
			String name = entry.get("name").getAsString();
			long expire = entry.get("expire").getAsLong();

			whitelisted.put(new CachedPlayer(uuid, name), expire);
		}
	}

	public void save() {
		JsonObject object = new JsonObject();
		object.addProperty("enabled", enabled);

		JsonArray array = new JsonArray();
		for (CachedPlayer player : whitelisted.keySet()) {
			if (!isWhitelisted(player)) continue;

			JsonObject entry = new JsonObject();
			entry.addProperty("uuid", player.uuid().toString());
			entry.addProperty("name", player.name());
			entry.addProperty("expire", whitelisted.get(player));
			array.add(entry);
		}
		object.add("whitelist", array);

		File file = getFile();
		if (!file.exists()) {
			Util.createFile(file);
		}

		try {
			FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8);
			gson.toJson(object, writer);
			writer.close();
		} catch (IOException ex) {
			throw new RuntimeException("Unable to save JSON to file " + file, ex);
		}
	}

	private File getFile() {
		return new File(dataDirectory.toFile(), "whitelist.json");
	}
}
