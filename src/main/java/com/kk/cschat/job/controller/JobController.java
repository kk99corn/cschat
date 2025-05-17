package com.kk.cschat.job.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/job")
@RequiredArgsConstructor
public class JobController {

    private final JobLauncher jobLauncher;
    private final Job job;

    @PostMapping("/mockJob")
    public String runJob() throws Exception {
        JobParameters params = new JobParametersBuilder()
            .addLong("time", System.currentTimeMillis()) // 중복 실행 방지
            .toJobParameters();

        jobLauncher.run(job, params);
        return "배치 실행됨";
    }
}
