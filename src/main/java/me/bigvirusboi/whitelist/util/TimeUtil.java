package me.bigvirusboi.whitelist.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TimeUtil {
	public static final long ONE_SECOND_MILLIS = 1000;
	public static final long ONE_MINUTE_MILLIS = ONE_SECOND_MILLIS * 60;
	public static final long ONE_HOUR_MILLIS = ONE_MINUTE_MILLIS * 60;
	public static final long ONE_DAY_MILLIS = ONE_HOUR_MILLIS * 24;

	private static final TimeUnitInfo[] UNIT_INFO = new TimeUnitInfo[]{
			new TimeUnitInfo(TimeUnit.SECONDS, "s"),
			new TimeUnitInfo(TimeUnit.MINUTES, "m"),
			new TimeUnitInfo(TimeUnit.HOURS, "h"),
			new TimeUnitInfo(TimeUnit.DAYS, "d")
	};

	private static final Map<TimeUnit, String> UNITS = new HashMap<>();
	static {
		for (TimeUnitInfo info : UNIT_INFO) {
			UNITS.put(info.unit, info.name);
		}
	}

	public static String toFormattedString(long time, TimeUnit unit) {
		if (time < 1) {
			return "0 " + UNITS.get(unit);
		}

		StringBuilder buffer = new StringBuilder();
		long remaining = time;

		for (TimeUnitInfo unitInfo : UNIT_INFO) {
			if (unitInfo.unit.ordinal() >= unit.ordinal()) {
				long mod = remaining % getUnitDivisor(unitInfo.unit);
				if (mod > 0) {
					buffer.insert(0, mod + unitInfo.name + " ");
				}
				remaining /= getUnitDivisor(unitInfo.unit);
			}
		}

		return buffer.toString().trim();
	}

	private static long getUnitDivisor(TimeUnit unit) {
		return switch (unit) {
			case SECONDS, MINUTES -> 60;
			case HOURS -> 24;
			case DAYS -> 365;
			default -> 0;
		};
	}

	public static String millisToDurationString(long milliseconds, boolean prefix) {
		long seconds = milliseconds / 1000;
		if (seconds <= 0) {
			return "now";
		}

		StringBuilder duration = new StringBuilder();
		if (prefix) {
			duration.append("in ");
		}
		if (seconds < 60) {
			duration.append(seconds == 1 ? "1 second" : seconds + " seconds");
			return duration.toString();
		}

		long minutes = seconds / 60;
		if (minutes < 60) {
			duration.append(minutes == 1 ? "1 minute" : minutes + " minutes");
			return duration.toString();
		}

		long hours = minutes / 60;
		if (hours < 24) {
			duration.append(hours == 1 ? "1 hour" : hours + " hours");
			return duration.toString();
		}

		long days = hours / 24;
		duration.append(days == 1 ? "1 day" : days + " days");
		return duration.toString();
	}

	public static float daysSince(long time) {
		return (float) (System.currentTimeMillis() - time) / ONE_DAY_MILLIS;
	}

	public static String formatMillis(long millis) {
		return toFormattedString(millis, TimeUnit.MILLISECONDS);
	}

	public static String formatToSeconds(long millis) {
		return toFormattedString(millis / 1000, TimeUnit.SECONDS);
	}

	private record TimeUnitInfo(TimeUnit unit, String name) {}
}
