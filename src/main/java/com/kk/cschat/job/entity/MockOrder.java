package com.kk.cschat.job.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "mock_order")
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class MockOrder {
    @Id
    @Column(name = "ord_no", nullable = false, length = 20)
    private String ordNo;

    @Column(name = "qty", nullable = false)
    private Integer qty;

    @Column(name = "price", nullable = false)
    private Integer price;

    @Column(name = "pay_complete_ts", nullable = false)
    private Instant payCompleteTs;

    @Column(name = "state", nullable = false, length = 1)
    private String state;

}