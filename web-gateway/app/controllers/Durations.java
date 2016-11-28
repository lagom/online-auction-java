package controllers;

import akka.japi.Pair;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 *
 */
public class Durations {

    public static Pair<ChronoUnit, Long> fromJDuration(Duration duration) {
        if (duration.getSeconds() % ChronoUnit.WEEKS.getDuration().getSeconds() == 0) {
            return Pair.create(ChronoUnit.WEEKS, duration.toDays() / 7);
        } else if (duration.getSeconds() % ChronoUnit.DAYS.getDuration().getSeconds() == 0) {
            return Pair.create(ChronoUnit.DAYS, duration.toDays());
        } else if (duration.getSeconds() % ChronoUnit.HALF_DAYS.getDuration().getSeconds() == 0) {
            return Pair.create(ChronoUnit.HALF_DAYS, duration.toHours() / 12);
        } else if (duration.getSeconds() % ChronoUnit.HOURS.getDuration().getSeconds() == 0) {
            return Pair.create(ChronoUnit.HOURS, duration.toHours());
        } else if (duration.getSeconds() % ChronoUnit.MINUTES.getDuration().getSeconds() == 0) {
            return Pair.create(ChronoUnit.MINUTES, duration.toMinutes());
        } else {
            return Pair.create(ChronoUnit.SECONDS, duration.getSeconds());
        }

    }
}
