package me.bigvirusboi.whitelist.cache;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import me.bigvirusboi.whitelist.util.Util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;
import java.util.logging.Logger;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MinecraftAPI {
	private static final String MOJANG_API_URL = "https://api.mojang.com/users/profiles/minecraft/";

	public static CachedPlayer getPlayer(String query) {
		try {
			URL url = new URL(MOJANG_API_URL + query);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setConnectTimeout(5_000); // 5 seconds timeout
			connection.setReadTimeout(5_000);

			int responseCode = connection.getResponseCode();
			if (responseCode == 200) {
				BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				StringBuilder response = new StringBuilder();
				String inputLine;
				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();

				JsonObject jsonObject = JsonParser.parseString(response.toString()).getAsJsonObject();
				String uuidString = jsonObject.get("id").getAsString();
				UUID uuid = Util.parseUUID(uuidString);
				String name = jsonObject.get("name").getAsString();
				if (uuid == null || name == null) {
					Logger.getLogger("MinecraftAPI").severe("Unable to GET player " + query + ": " + response.toString());
					return null;
				}

				return new CachedPlayer(uuid, name);
			}
		} catch (Exception ex) {
			Logger.getLogger("MinecraftAPI").severe("Unable to GET player " + query + ": " + ex);
			ex.printStackTrace();
		}
		return null;
	}
}
