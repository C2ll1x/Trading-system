public class Bar {
	public final double open;
	public final double high;
	public final double low;
	public final double close;
	public final double volume;
	public final int hourOfDay; // 0-23

	public Bar(double open, double high, double low, double close, double volume, int hourOfDay) {
		this.open = open;
		this.high = high;
		this.low = low;
		this.close = close;
		this.volume = volume;
		this.hourOfDay = hourOfDay;
	}
}
