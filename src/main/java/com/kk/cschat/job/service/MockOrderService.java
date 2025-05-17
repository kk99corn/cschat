package com.kk.cschat.job.service;

import com.kk.cschat.job.dto.MockOrderDto.MockOrderRequest;
import com.kk.cschat.job.dto.MockOrderDto.MockOrderResult;
import com.kk.cschat.job.mapper.MockOrderMapper;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MockOrderService {

    private final MockOrderMapper mockOrderMapper;
    private static final int LIMIT = 1000;

    public List<MockOrderResult> selectAll() {
        return mockOrderMapper.selectByOrdersAll();
    }

    public long selectByOrders(LocalDateTime payTs, int count) {
        long totalPrice = 0;
        int offset = 0;

        while (true) {
            MockOrderRequest request = MockOrderRequest.builder()
                                                       .payCompleteTs(payTs)
                                                       .offset(offset)
                                                       .limit(LIMIT)
                                                       .build();

            List<MockOrderResult> results = mockOrderMapper.selectByOrders(request);
            if (results.isEmpty()) {
                break;
            }

            totalPrice += results.stream()
                                 .mapToLong(MockOrderResult::getPrice)
                                 .sum();

            offset += LIMIT;
            if (offset >= count) {
                break;
            }
        }

        return totalPrice;
    }

    public long selectZeroOffsetByOrders(LocalDateTime payTs, int count) {
        long totalPrice = 0;
        int totalCount = 0;
        LocalDateTime lastPayTs = payTs;
        String lastOrdNo = "000000000000000"; // 가장 작은 값으로 시작

        while (true) {
            MockOrderRequest request = MockOrderRequest.builder()
                                                       .payCompleteTs(lastPayTs)
                                                       .ordNo(lastOrdNo)
                                                       .limit(LIMIT)
                                                       .build();

            List<MockOrderResult> results = mockOrderMapper.selectZeroOffsetByOrders(request);
            if (results.isEmpty()) {
                break;
            }

            totalPrice += results.stream()
                                 .mapToLong(MockOrderResult::getPrice)
                                 .sum();

            totalCount += results.size();
            lastOrdNo = results.get(results.size() - 1).getOrdNo();
            lastPayTs = results.get(results.size() - 1).getPayCompleteTs();

            if (totalCount >= count) {
                break;
            }
        }

        return totalPrice;
    }
}
