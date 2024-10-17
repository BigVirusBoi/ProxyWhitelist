package me.bigvirusboi.whitelist.util;

import com.google.common.io.Files;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Util {
	public static UUID parseUUID(String id) {
		if (id == null) return null;
		try {
			return UUID.fromString(id);
		} catch (IllegalArgumentException ex) {
			return null;
		}
	}

	public static void copyPartialMatches(String token, List<String> originals, List<String> collection) {
		for (String string : originals) {
			if (startsWithIgnoreCase(string, token)) {
				collection.add(string);
			}
		}
	}

	public static boolean startsWithIgnoreCase(String string, String prefix) {
		if (string.length() < prefix.length()) {
			return false;
		}
		return string.regionMatches(true, 0, prefix, 0, prefix.length());
	}

	public static File createFile(File file) {
		try {
			Files.createParentDirs(file);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException ignored) {}
		}
		return file;
	}
}
