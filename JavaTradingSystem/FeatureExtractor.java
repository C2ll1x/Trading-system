import java.util.ArrayList;
import java.util.List;

public class FeatureExtractor {
	public double ema(List<Double> values, int period) {
		if (values.isEmpty() || period <= 1) return values.isEmpty() ? 0.0 : values.get(values.size() - 1);
		double k = 2.0 / (period + 1.0);
		double ema = values.get(0);
		for (int i = 1; i < values.size(); i++) {
			ema = values.get(i) * k + ema * (1.0 - k);
		}
		return ema;
	}

	public double rsi(List<Double> closes, int period) {
		if (closes.size() <= period) return 50.0;
		double gain = 0.0;
		double loss = 0.0;
		for (int i = closes.size() - period; i < closes.size(); i++) {
			double change = closes.get(i) - closes.get(i - 1);
			if (change >= 0) gain += change; else loss -= change;
		}
		if (loss == 0) return 100.0;
		double rs = (gain / period) / (loss / period);
		return 100.0 - (100.0 / (1.0 + rs));
	}

	public List<Double> toCloses(List<Bar> bars) {
		List<Double> out = new ArrayList<>();
		for (Bar b : bars) out.add(b.close);
		return out;
	}

	public List<Double> toHighs(List<Bar> bars) {
		List<Double> out = new ArrayList<>();
		for (Bar b : bars) out.add(b.high);
		return out;
	}

	public List<Double> toLows(List<Bar> bars) {
		List<Double> out = new ArrayList<>();
		for (Bar b : bars) out.add(b.low);
		return out;
	}
}
