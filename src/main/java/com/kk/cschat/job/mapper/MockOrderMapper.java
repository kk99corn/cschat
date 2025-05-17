package com.kk.cschat.job.mapper;

import com.kk.cschat.job.dto.MockOrderDto.MockOrderRequest;
import com.kk.cschat.job.dto.MockOrderDto.MockOrderResult;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MockOrderMapper {
    List<MockOrderResult> selectByOrdersAll();
    List<MockOrderResult> selectByOrders(MockOrderRequest request);
    List<MockOrderResult> selectZeroOffsetByOrders(MockOrderRequest request);
}
