import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Backtester {
    private final TradeSignalGenerator generator;
    private final double pipSize;

    public Backtester(TradeSignalGenerator generator, double pipSize) {
        this.generator = generator;
        this.pipSize = pipSize;
    }

    public void runSimulation() {
        List<Bar> history = new ArrayList<>();
        Random rnd = new Random(42);
        double price = 1900.00; // XAUUSDm anchor
        for (int i = 0; i < 3000; i++) {
            int hour = (i / 60) % 24;
            double spread = 0.02; // 2 cents
            double drift = (rnd.nextDouble() - 0.5) * 0.1; // random walk cents
            double open = price;
            double high = open + Math.abs(drift) + spread;
            double low = open - Math.abs(drift) - spread;
            double close = open + drift;
            double vol = 100 + rnd.nextInt(200);
            history.add(new Bar(open, high, low, close, vol, hour));
            price = close;
        }

        int trades = 0;
        int wins = 0;
        double balance = 10000.0;
        double maxEquity = balance;
        double maxDrawdown = 0.0;
        double totalPips = 0.0;
        List<TradeRecord> log = new ArrayList<>();

        List<Bar> window = new ArrayList<>();
        for (Bar b : history) {
            window.add(b);
            boolean londonNy = (b.hourOfDay >= 7 && b.hourOfDay <= 16) || (b.hourOfDay >= 13 && b.hourOfDay <= 22);
            if (window.size() < 60) continue;
            if (trades >= 100) break;

            String cmd = generator.maybeCreateOrder(window, londonNy);
            if (!cmd.startsWith("HOLD")) {
                String[] parts = cmd.split(",");
                String side = parts[0];
                double lot = Double.parseDouble(parts[2]);
                double sl = Double.parseDouble(parts[3]);
                double tp = Double.parseDouble(parts[4]);
                double entryPrice = window.get(window.size()-1).close;

                boolean hitTp = simulateOutcome(window, side, entryPrice, sl, tp);
                double pips = Math.abs((tp - entryPrice) / pipSize);
                double lossPips = Math.abs((entryPrice - sl) / pipSize);
                double pnlPips = hitTp ? pips : -lossPips;
                totalPips += pnlPips;
                wins += hitTp ? 1 : 0;
                trades++;
                double pipValueStdLot = 10.0; // adjust for gold
                double tradePnlUsd = pnlPips * pipValueStdLot * lot;
                balance += tradePnlUsd;
                maxEquity = Math.max(maxEquity, balance);
                maxDrawdown = Math.max(maxDrawdown, (maxEquity - balance));
                log.add(new TradeRecord(side, entryPrice, sl, tp, lot, 0.0, hitTp, pnlPips));
            }
        }

        double winRate = trades > 0 ? (wins * 100.0 / trades) : 0.0;
        System.out.println("Backtest Result: trades=" + trades + ", winRate=" + String.format("%.1f%%", winRate)
                + ", finalBalance=" + String.format("%.2f", balance)
                + ", maxDrawdown=" + String.format("%.2f", maxDrawdown)
                + ", totalPips=" + String.format("%.1f", totalPips));
    }

    private boolean simulateOutcome(List<Bar> window, String side, double entry, double sl, double tp) {
        int start = window.size() - 1;
        int end = Math.min(window.size(), start + 20);
        for (int i = start; i < end; i++) {
            Bar b = window.get(i);
            if (side.equals("BUY")) {
                if (b.low <= sl) return false;
                if (b.high >= tp) return true;
            } else {
                if (b.high >= sl) return false;
                if (b.low <= tp) return true;
            }
        }
        return false;
    }
}
