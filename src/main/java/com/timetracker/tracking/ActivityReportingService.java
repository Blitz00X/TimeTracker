package com.timetracker.tracking;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public class ActivityReportingService {

    private final ActivitySessionDao sessionDao;

    public ActivityReportingService(ActivitySessionDao sessionDao) {
        this.sessionDao = Objects.requireNonNull(sessionDao, "sessionDao");
    }

    public ActivityReportingService() {
        this(new ActivitySessionDao());
    }

    public List<ActivityDailyTotal> getTotalsForDate(LocalDate date) {
        Objects.requireNonNull(date, "date");
        return sessionDao.findTotalsForDate(date);
    }
}
