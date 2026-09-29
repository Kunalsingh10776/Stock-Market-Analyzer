import com.quantanalyzer.dto.RiskMetricsDto;
import com.quantanalyzer.service.RiskCalculatorService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

public class RiskCalculatorServiceTest {

    private final RiskCalculatorService service = new RiskCalculatorService();

    @Test
    void maxDrawdown_detectsPeakToTroughDecline() {

        // Peak 1.20, trough 0.90 -> (1.20 - 0.90) / 1.20 = 25%
        List<Double> equity = List.of(1.00, 1.10, 1.20, 1.05, 0.90, 1.00);
        RiskMetricsDto metrics = service.computeMetrics(equity, null, 0.0);
        assertThat(metrics.getMaxDrawdownPct()).isCloseTo(25.0, within(0.01));
    }

    @Test
    void flatCurve_hasZeroSharpeAndZeroDrawdown() {
        List<Double> equity = List.of(1.0, 1.0, 1.0, 1.0);
        RiskMetricsDto metrics = service.computeMetrics(equity, null, 0.0);
        assertThat(metrics.getSharpeRatio()).isEqualTo(0.0);
        assertThat(metrics.getMaxDrawdownPct()).isEqualTo(0.0);
    }

}
