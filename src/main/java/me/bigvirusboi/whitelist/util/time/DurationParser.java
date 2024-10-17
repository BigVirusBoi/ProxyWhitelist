package me.bigvirusboi.whitelist.util.time;

import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
public class DurationParser {
	private final List<DurationUnit.DurationKey> keys = new ArrayList<>();
	private long duration = 0;

	public DurationParser(long millis) {
		this.duration = millis;
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
			int amount = Integer.parseInt(numberBuilder.toString().trim());
			DurationUnit unit = DurationUnit.fromIdentifier(unitBuilder.toString().trim());
			if (unit != null) {
				keys.add(new DurationUnit.DurationKey(amount, unit));
			}
			numberBuilder.setLength(0);
			unitBuilder.setLength(0);
		}
	}

	public long getMillis() {
		if (duration != 0) return duration;
		return keys.stream().mapToLong(key -> key.unit().toMillis(key.amount())).sum();
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
}
