public class LogisticModel {
	private final double[] weights; // includes bias as w0; features must include 1.0

	public LogisticModel(double[] weights) {
		this.weights = weights;
	}

	public double predictProbability(double[] features) {
		int n = Math.min(features.length, weights.length);
		double z = 0.0;
		for (int i = 0; i < n; i++) {
			z += features[i] * weights[i];
		}
		if (z < -35) return 0.0; // numeric guardrails
		if (z > 35) return 1.0;
		return 1.0 / (1.0 + Math.exp(-z));
	}
}
