import com.quantanalyzer.domain.Price;
import com.quantanalyzer.service.BacktestEngineService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

public class BacktestEngineServiceTest {

    private final BacktestEngineService service = new BacktestEngineService();

    @Test
    void buyAndHoldCurve_reflectsTotalPriceChange() {
        List<Price> bars = List.of(
                bar("2024-01-01", "100"),
                bar("2024-01-02", "110"),  // +10%
                bar("2024-01-03", "99")    // -10% from 110
        );

        List<Double> curve = service.buildBuyAndHoldEquityCurve(bars);

        assertThat(curve.get(0)).isEqualTo(1.0);
        assertThat(curve.get(1)).isCloseTo(1.10, within(0.001));
        assertThat(curve.get(2)).isCloseTo(0.99, within(0.001)); // 1.10 * 0.90
    }

    private Price bar(String date, String close) {
        BigDecimal price = new BigDecimal(close);
        return Price.builder()
                .ticker("TEST")
                .barDate(LocalDate.parse(date))
                .openPrice(price).highPrice(price).lowPrice(price).closePrice(price)
                .volume(1000L)
                .build();

    }
}
