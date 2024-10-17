package me.bigvirusboi.whitelist.util.time;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TimeUtil {
	// Formats time from millis to a human-readable format
	public static String toFormattedString(long millis) {
		if (millis < 1000) {
			return "0 s";
		}

		StringBuilder buffer = new StringBuilder();
		long remaining = millis;

		for (DurationUnit unit : DurationUnit.valuesReversed()) {
			long unitMillis = unit.getMultiplier();
			long amount = remaining / unitMillis;
			if (amount > 0) {
				buffer.append(amount).append(unit.getIdentifier()).append(" ");
				remaining %= unitMillis;
			}
		}

		return buffer.toString().trim();
	}

	// Parses a duration string like "2h 30m" into milliseconds
	public static long parseDuration(String input) {
		DurationParser parser = new DurationParser();
		parser.parse(input);
		return parser.getMillis();
	}
}
