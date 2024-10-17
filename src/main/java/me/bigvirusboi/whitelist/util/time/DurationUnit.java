package me.bigvirusboi.whitelist.util.time;

import lombok.Getter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Getter
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

	// Returns units in reverse order for formatting (from largest to smallest)
	public static DurationUnit[] valuesReversed() {
		DurationUnit[] values = values();
		List<DurationUnit> reversed = Arrays.asList(values);
		Collections.reverse(reversed);
		return reversed.toArray(new DurationUnit[0]);
	}

	// Represents a unit and amount of time (e.g., 2h or 30m)
	public record DurationKey(int amount, DurationUnit unit) {}
}
