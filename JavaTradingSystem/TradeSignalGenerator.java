import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class TradeSignalGenerator {
    // Example close price history (replace with real market data source)
    private List<Double> priceHistory = Arrays.asList(1900.00, 1900.15, 1899.95, 1900.30, 1900.55, 1900.40,
            1900.80, 1901.20, 1901.00, 1901.50, 1901.35, 1901.70, 1901.90, 1902.10, 1901.85, 1902.05,
            1902.40, 1902.25, 1902.60, 1902.90, 1902.50, 1902.30, 1902.70, 1902.95, 1903.10, 1903.30,
            1903.00, 1902.80, 1903.10, 1903.25, 1903.55, 1903.40, 1903.85, 1904.10, 1904.00, 1904.25,
            1904.55, 1904.35, 1904.80, 1905.00, 1904.85, 1905.10, 1905.35, 1905.20, 1905.55, 1905.75,
            1905.60, 1905.90, 1906.10, 1906.00, 1906.25, 1906.50, 1906.35, 1906.65, 1906.90, 1907.10,
            1906.95, 1907.25, 1907.45, 1907.30);

    // Logistic weights: [bias, r1, r3, ema10-ema50, rsi14, volZ]
    private final double[] weights = new double[] { 0.0, 2.0, 1.0, 1.5, 1.0, 0.5 };
    private final LogisticModel model = new LogisticModel(weights);
    private final FeatureExtractor fx = new FeatureExtractor();
    private final TrendFilter trendFilter = new TrendFilter();

    // Risk manager parameters (adjust for your broker setup)
    private final RiskManager riskManager = new RiskManager(
            10000.0,   // account balance USD
            0.02,      // default risk per trade
            10.0,      // USD per pip per 1.0 lot (approx for gold, adjust per broker)
            0.10       // pip size for XAUUSDm (10 cents = 1 pip)
    );

    // Strategy parameters
    private final double buyThreshold = 0.55;
    private final double sellThreshold = 0.45;
    private final double riskFraction = 0.02; // 2%
    private final double slPips = 50.0;
    private final double tpPips = 100.0; // 1:2 R:R
    private final double pipSize = 0.10; // 10 cents per pip for XAUUSDm

    public String generateSignal() {
        // Build minimal synthetic bars from closes for indicator calculations
        List<Bar> bars = closesToBars(priceHistory);
        if (bars.size() < 60) {
            return "HOLD,XAUUSDm,0.0,0,0";
        }

        double prob = model.predictProbability(buildFeatures(bars));
        String side = "HOLD";
        if (prob >= buyThreshold && trendFilter.allowBuy(bars)) side = "BUY";
        else if (prob <= sellThreshold && trendFilter.allowSell(bars)) side = "SELL";
        if ("HOLD".equals(side)) {
            return "HOLD,XAUUSDm,0.0,0,0";
        }

        Bar last = bars.get(bars.size() - 1);
        double entry = last.close;
        double sl = side.equals("BUY") ? entry - slPips * pipSize : entry + slPips * pipSize;
        double tp = side.equals("BUY") ? entry + tpPips * pipSize : entry - tpPips * pipSize;
        double lot = riskManager.computeLotForFixedSlRisk(riskFraction, slPips);

        return side + ",XAUUSDm," + String.format(java.util.Locale.US, "%.2f", lot)
                + "," + String.format(java.util.Locale.US, "%.2f", sl)
                + "," + String.format(java.util.Locale.US, "%.2f", tp);
    }

    // New: API compatible with Backtester/TradeExecutor usage
    public String maybeCreateOrder(List<Bar> history, boolean sessionOpen) {
        if (!sessionOpen) return "HOLD,XAUUSDm,0.0,0,0";
        if (history == null || history.size() < 60) return "HOLD,XAUUSDm,0.0,0,0";
        double prob = model.predictProbability(buildFeatures(history));
        String side = "HOLD";
        if (prob >= buyThreshold && trendFilter.allowBuy(history)) side = "BUY";
        else if (prob <= sellThreshold && trendFilter.allowSell(history)) side = "SELL";
        if ("HOLD".equals(side)) return "HOLD,XAUUSDm,0.0,0,0";
        Bar last = history.get(history.size() - 1);
        double entry = last.close;
        double sl = side.equals("BUY") ? entry - slPips * pipSize : entry + slPips * pipSize;
        double tp = side.equals("BUY") ? entry + tpPips * pipSize : entry - tpPips * pipSize;
        double lot = riskManager.computeLotForFixedSlRisk(riskFraction, slPips);
        return side + ",XAUUSDm," + String.format(java.util.Locale.US, "%.2f", lot)
                + "," + String.format(java.util.Locale.US, "%.2f", sl)
                + "," + String.format(java.util.Locale.US, "%.2f", tp);
    }

    private List<Bar> closesToBars(List<Double> closes) {
        List<Bar> out = new ArrayList<>();
        int hour = 12; // synthetic mid-session hour
        for (double c : closes) {
            out.add(new Bar(c, c, c, c, 100.0, hour));
        }
        return out;
    }

    private double[] buildFeatures(List<Bar> bars) {
        // Features aligned with TradeExecutor
        List<Double> closes = fx.toCloses(bars);
        int n = closes.size();
        double p0 = closes.get(n - 1);
        double p1 = closes.get(n - 2);
        double p3 = closes.get(n - 4);
        double r1 = safeDiv(p0 - p1, p1);
        double r3 = safeDiv(p0 - p3, p3);
        double ema10 = fx.ema(closes, 10);
        double ema50 = fx.ema(closes, 50);
        double rsi = fx.rsi(closes, 14) / 100.0;
        double volZ = 0.0; // placeholder since synthetic volumes are constant
        return new double[] { 1.0, r1, r3, ema10 - ema50, rsi, volZ };
    }

    private double safeDiv(double a, double b) {
        return b == 0.0 ? 0.0 : a / b;
    }
}
