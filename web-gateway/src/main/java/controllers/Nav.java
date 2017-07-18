package controllers;

import akka.japi.Pair;
import com.example.auction.user.api.User;
import org.ocpsoft.prettytime.PrettyTime;
import org.ocpsoft.prettytime.impl.ResourcesTimeFormat;
import org.ocpsoft.prettytime.impl.ResourcesTimeUnit;
import org.ocpsoft.prettytime.units.JustNow;
import org.pcollections.PSequence;
import play.i18n.Messages;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Date;
import java.util.Optional;
import java.util.TimeZone;

public class Nav {

    private final Messages messages;
    private final PSequence<User> users;
    private final Optional<User> user;
    // todo - make these based on users language/timezone
    private static final PrettyTime prettyTime = new PrettyTime();
    static {
        prettyTime.removeUnit(JustNow.class);
        ResourcesTimeUnit justNow = new ResourcesTimeUnit() {
            {
                setMaxQuantity(10000);
            }
            @Override
            protected String getResourceKeyPrefix() {
                return "JustNow";
            }
        };
        prettyTime.registerUnit(justNow, new ResourcesTimeFormat(justNow));
    }

    private static final ZoneId zoneId = ZoneId.systemDefault();
    private static final DateTimeFormatter todayFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    public Nav(Messages messages, PSequence<User> users, Optional<User> user) {
        this.messages = messages;
        this.users = users;
        this.user = user;
    }

    public PSequence<User> getUsers() {
        return users;
    }

    public Optional<User> getUser() {
        return user;
    }

    public Messages messages() {
        return messages;
    }

    public String formatInstant(Instant instant) {
        Duration duration = Duration.between(instant, Instant.now());
        if (duration.abs().compareTo(Duration.of(1, ChronoUnit.HOURS)) < 0) {
            return prettyTime.format(Date.from(instant));
        }

        ZonedDateTime dateTime = ZonedDateTime.ofInstant(instant, zoneId);
        ZonedDateTime today = ZonedDateTime.now(zoneId).withHour(0).withMinute(0).withSecond(0);
        ZonedDateTime tomorrow = today.plus(1, ChronoUnit.DAYS);
        if (dateTime.compareTo(today) > 0 && dateTime.compareTo(tomorrow) < 0) {
            return todayFormatter.format(dateTime);
        }

        return dateFormatter.format(dateTime);
    }

    public String formatDuration(Duration duration) {
        Pair<ChronoUnit, Long> pair = Durations.fromJDuration(duration);

        String key ;
        switch (pair.first()){
            case WEEKS:   {key = "durationWeeks"; break;}
            case DAYS:    {key = "durationDays"; break;}
            case HOURS:   {key = "durationHours"; break;}
            case MINUTES: {key = "durationMinutes"; break;}
            default:      {key = "durationSeconds"; break;}
        }

        return messages.at(key, pair.second());

    }
}
