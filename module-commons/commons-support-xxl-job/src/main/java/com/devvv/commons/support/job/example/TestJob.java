package com.devvv.commons.support.job.example;

import com.xxl.job.core.context.XxlJobHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Create by WangSJ on 2024/07/10
 */
@Slf4j
@Component
public class TestJob {


    // @XxlJob("test")
    public void test(String param) {
        String jobParam = XxlJobHelper.getJobParam();
        String jobLogFileName = XxlJobHelper.getJobLogFileName();
        log.warn("定时任务执行... jobParam:{} jobLogFileName：{}", jobParam, jobLogFileName);
    }

}
