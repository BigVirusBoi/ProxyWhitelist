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
	public static UUID parseUUID(String input) {
		if (input == null) return null;
		try {
			return UUID.fromString(input);
		} catch (IllegalArgumentException ignored) {}
		try {
			return fromTrimmedUUID(input);
		} catch (IllegalArgumentException ignored) {}
		return null;
	}

	public static UUID fromTrimmedUUID(String input) throws IllegalArgumentException {
		if (input == null)
			throw new IllegalArgumentException();

		StringBuilder builder = new StringBuilder(input.trim());
		/* Backwards adding to avoid index adjustments */
		try {
			builder.insert(20, "-");
			builder.insert(16, "-");
			builder.insert(12, "-");
			builder.insert(8, "-");
		} catch (StringIndexOutOfBoundsException e) {
			throw new IllegalArgumentException();
		}

		return UUID.fromString(builder.toString());
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

	public static void createFile(File file) {
		try {
			Files.createParentDirs(file);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			}
		}
	}
}
