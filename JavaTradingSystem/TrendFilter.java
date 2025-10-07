import java.util.List;

public class TrendFilter {
	private final FeatureExtractor fx = new FeatureExtractor();

	public boolean allowBuy(List<Bar> bars) {
		if (bars.size() < 60) return false;
		double ema50 = fx.ema(fx.toCloses(bars), 50);
		double rsi14 = fx.rsi(fx.toCloses(bars), 14);
		double lastClose = bars.get(bars.size() - 1).close;
		return lastClose > ema50 && rsi14 > 55.0;
	}

	public boolean allowSell(List<Bar> bars) {
		if (bars.size() < 60) return false;
		double ema50 = fx.ema(fx.toCloses(bars), 50);
		double rsi14 = fx.rsi(fx.toCloses(bars), 14);
		double lastClose = bars.get(bars.size() - 1).close;
		return lastClose < ema50 && rsi14 < 45.0;
	}
}
