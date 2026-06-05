package cn.edu.sdu.sms.server.service;

import cn.edu.sdu.sms.server.mapper.SemesterConfigMapper;
import cn.edu.sdu.sms.server.models.SemesterConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class SemesterConfigService {

    @Autowired
    private SemesterConfigMapper mapper;

    public SemesterConfig getConfig() {
        return mapper.getConfig();
    }

    public SemesterConfig saveConfig(SemesterConfig config) {
        mapper.upsertConfig(config);
        return config;
    }

    public int calculateCurrentWeek() {
        SemesterConfig config = mapper.getConfig();
        if (config == null || config.getStartWeekDate() == null || config.getTotalWeeks() == null) return 1;
        long days = ChronoUnit.DAYS.between(config.getStartWeekDate(), LocalDate.now());
        int week = (int) (days / 7) + 1;
        return Math.max(1, Math.min(week, config.getTotalWeeks()));
    }
}
