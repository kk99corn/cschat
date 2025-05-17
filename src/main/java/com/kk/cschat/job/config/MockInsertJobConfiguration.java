package com.kk.cschat.job.config;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class MockInsertJobConfiguration {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final JdbcTemplate jdbcTemplate;

    @Bean
    public Job mockInsertOrderJob() {
        return new JobBuilder("mockInsertOrderJob", jobRepository)
            .start(mockInsertOrderStep())
            .build();
    }

    @Bean
    public Step mockInsertOrderStep() {
        return new StepBuilder("mockInsertOrderStep", jobRepository)
            .tasklet((contribution, chunkContext) -> {
                generateAndInsertMockData(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 5, 8));
                return RepeatStatus.FINISHED;
            }, transactionManager)
            .build();
    }

    private void generateAndInsertMockData(LocalDate start, LocalDate end) {
        Random random = new Random();
        int recordsPerDay = 100000;
        int counter = 1;
        int batchSize = 5000;

        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            List<Object[]> batch = new ArrayList<>();
            String datePrefix = date.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));

            for (int i = 0; i < recordsPerDay; i++) {
                String ordNo = datePrefix + String.format("%07d", counter++);
                int qty = random.nextInt(10) + 1;
                int price = random.nextInt(100000 - 1000 + 1) + 1000;
                String state = String.valueOf(random.nextInt(4));
                LocalDateTime payCompleteTs = date.atTime(LocalTime.ofSecondOfDay(random.nextInt(86400)));

                batch.add(new Object[]{ordNo, qty, price, payCompleteTs, state});

                // 5000건마다 커밋
                if (batch.size() == batchSize) {
                    jdbcTemplate.batchUpdate(
                        "INSERT INTO mock_order (ord_no, qty, price, pay_complete_ts, state) VALUES (?, ?, ?, ?, ?)",
                        batch
                    );
                    batch.clear(); // 커밋 후 리스트 비움
                }
            }

            // 남은 데이터 커밋
            if (!batch.isEmpty()) {
                jdbcTemplate.batchUpdate(
                    "INSERT INTO mock_order (ord_no, qty, price, pay_complete_ts, state) VALUES (?, ?, ?, ?, ?)",
                    batch
                );
            }

            System.out.println("Inserted " + recordsPerDay + " rows for date " + date);
        }
    }
}
