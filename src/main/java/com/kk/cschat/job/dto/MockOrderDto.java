package com.kk.cschat.job.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class MockOrderDto {

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    public static class MockOrderRequest {
        private String ordNo;
        private LocalDateTime payCompleteTs;
        private int offset;
        private int limit;
    }

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    public static class MockOrderResult {
        private String ordNo;
        private int qty;
        private int price;
        private LocalDateTime payCompleteTs;
        private String state;
    }
}
