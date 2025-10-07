import java.util.List;

public class RiskManager {
    private final double accountBalanceUsd;
    private final double riskPerTradeFraction; // e.g., 0.01 for 1%
    private final double pipValuePerStandardLotUsd; // e.g., 10 USD per pip for EURUSD 1.0 lot
    private final double pipSize; // e.g., 0.0001 for EURUSD

    public RiskManager(double accountBalanceUsd,
                       double riskPerTradeFraction,
                       double pipValuePerStandardLotUsd,
                       double pipSize) {
        this.accountBalanceUsd = accountBalanceUsd;
        this.riskPerTradeFraction = riskPerTradeFraction;
        this.pipValuePerStandardLotUsd = pipValuePerStandardLotUsd;
        this.pipSize = pipSize;
    }

    public double computeATR(List<Double> closePrices, int period) {
        if (closePrices.size() < period + 1) return 0.0;
        double sumTr = 0.0;
        for (int i = closePrices.size() - period; i < closePrices.size(); i++) {
            double high = closePrices.get(i);
            double low = closePrices.get(i - 1);
            double prevClose = closePrices.get(i - 1);
            double tr = Math.max(high - low, Math.max(Math.abs(high - prevClose), Math.abs(low - prevClose)));
            sumTr += tr;
        }
        return sumTr / period;
    }

    public double computeLotSizeFromAtrRisk(double atrPriceUnits, double slAtrMultiplier) {
        if (atrPriceUnits <= 0.0) return 0.01;
        double slPriceUnits = atrPriceUnits * slAtrMultiplier;
        double slPips = slPriceUnits / pipSize;
        if (slPips <= 0.0) return 0.01;
        double riskUsd = accountBalanceUsd * riskPerTradeFraction;
        double lotSize = riskUsd / (slPips * pipValuePerStandardLotUsd);
        return normalizeLot(lotSize);
    }

    public double computeLotForFixedSlRisk(double riskFraction, double slPips) {
        if (slPips <= 0.0) return 0.01;
        double riskUsd = accountBalanceUsd * riskFraction;
        double lotSize = riskUsd / (slPips * pipValuePerStandardLotUsd);
        return normalizeLot(lotSize);
    }

    // Tiered balance-based lot sizing:
    // $100-$200 => 0.02, $300 => 0.03, $400 => 0.04, $500 => 0.05, ...
    public double computeLotByBalanceScale() {
        double balance = accountBalanceUsd;
        int tier = (int) Math.ceil(balance / 100.0);
        double lot;
        if (tier <= 2) {
            lot = 0.02;
        } else {
            lot = tier * 0.01;
        }
        return normalizeLot(lot);
    }

    public double[] computeSlTpPrices(String action, double currentPrice,
                                      double atrPriceUnits,
                                      double slAtrMultiplier,
                                      double rrMultiple) {
        double slDistance = atrPriceUnits * slAtrMultiplier;
        double tpDistance = slDistance * rrMultiple;
        double slPrice;
        double tpPrice;
        if ("BUY".equals(action)) {
            slPrice = currentPrice - slDistance;
            tpPrice = currentPrice + tpDistance;
        } else if ("SELL".equals(action)) {
            slPrice = currentPrice + slDistance;
            tpPrice = currentPrice - tpDistance;
        } else {
            slPrice = 0.0;
            tpPrice = 0.0;
        }
        return new double[] { roundPrice(slPrice), roundPrice(tpPrice) };
    }

    private double normalizeLot(double lot) {
        if (lot < 0.01) lot = 0.01;
        // round down to 0.01 increments
        lot = Math.floor(lot * 100.0) / 100.0;
        return lot;
    }

    private double roundPrice(double price) {
        // round to pip fraction consistent with pipSize (e.g., 0.0001)
        double factor = Math.round(1.0 / pipSize);
        return Math.round(price * factor) / factor;
    }
} 