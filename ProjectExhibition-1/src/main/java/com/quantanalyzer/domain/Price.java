package com.quantanalyzer.domain;

//a Java class that maps directly to a database table.
// Each instance of this class represents one row: one stock's OHLCV data for one specific day.

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
        name ="price",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_ticker_bar_date",
                columnNames = {"ticker", "bar_date"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Price {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String ticker;

    @Column(name = "bar_date", nullable = false)
    private LocalDate barDate;

    @Column(name ="open", nullable = false, precision = 19, scale = 6)
    private BigDecimal openPrice;

    @Column(name ="high", nullable = false, precision = 19, scale = 6)
    private BigDecimal highPrice;

    @Column(name = "low", nullable = false, precision= 19, scale = 6)
    private BigDecimal lowPrice;

    @Column(name ="close", nullable = false, precision = 19, scale = 6)
    private BigDecimal closePrice;

    @Column(nullable = false)
    private Long volume;
}
