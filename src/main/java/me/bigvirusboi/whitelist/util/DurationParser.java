package me.bigvirusboi.whitelist.util;

import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
public class DurationParser {
	private final List<DurationKey> keys = new ArrayList<>();
	private long duration = 0;

	private DurationParser(long duration) {
		this.duration = duration;
	}

	public void parse(String input) {
		duration = 0;

		keys.clear();
		StringBuilder numberBuilder = new StringBuilder();
		StringBuilder unitBuilder = new StringBuilder();

		for (char ch : input.trim().toCharArray()) {
			if (Character.isDigit(ch)) {
				if (!unitBuilder.isEmpty()) {
					addKey(numberBuilder, unitBuilder);
				}

				numberBuilder.append(ch);
			} else {
				unitBuilder.append(ch);
			}
		}
		addKey(numberBuilder, unitBuilder);
	}

	private void addKey(StringBuilder numberBuilder, StringBuilder unitBuilder) {
		if (!numberBuilder.isEmpty() && !unitBuilder.isEmpty()) {
			int amount = Integer.parseInt(numberBuilder.toString());
			DurationUnit unit = DurationUnit.fromIdentifier(unitBuilder.toString());
			if (unit != null) {
				keys.add(new DurationKey(amount, unit));
			}
			numberBuilder.setLength(0);
			unitBuilder.setLength(0);
		}
	}

	public long getMillis() {
		if (duration != 0) return duration;

		return keys.stream().mapToLong(key -> key.unit().toMillis(key.amount())).sum();
	}

	public long getSeconds() {
		return getMillis() / 1000;
	}

	public boolean isValid() {
		return !keys.isEmpty();
	}

	public boolean isPermanent() {
		return duration == -1;
	}

	public static DurationParser ofMillis(long millis) {
		return new DurationParser(millis);
	}

	public static DurationParser permanent() {
		return ofMillis(-1);
	}

	public record DurationKey(int amount, DurationUnit unit) {}

	public enum DurationUnit {
		SECONDS(1000, "s"),
		MINUTES(SECONDS, 60, "m"),
		HOURS(MINUTES, 60, "h"),
		DAYS(HOURS, 24, "d"),
		WEEKS(DAYS, 7, "w"),
		MONTHS(DAYS, 30, "mo"),
		YEARS(DAYS, 365, "y");

		private final long multiplier;
		private final String identifier;

		DurationUnit(long multiplier, String identifier) {
			this.multiplier = multiplier;
			this.identifier = identifier;
		}

		DurationUnit(DurationUnit unit, long multiplier, String identifier) {
			this.multiplier = unit.multiplier * multiplier;
			this.identifier = identifier;
		}

		public long toMillis(int amount) {
			return amount * multiplier;
		}

		public static DurationUnit fromIdentifier(String identifier) {
			for (DurationUnit unit : values()) {
				if (unit.identifier.equalsIgnoreCase(identifier)) {
					return unit;
				}
			}
			return null;
		}
	}
}
