package csu.standard.simplePartition;

public class StandardPoint<T extends Number> {
	private T x_;
	private T y_;

	public StandardPoint(T x, T y) {
		this.x_ = x;
		this.y_ = y;
	}

	public T getX() {
		return x_;
	}

	public void setX(T x) {
		this.x_ = x;
	}

	public T getY() {
		return y_;
	}

	public void setY(T y) {
		this.y_ = y;
	}

	public double distanceTo(StandardPoint<?> point) {
		double dx = Math.abs(getX().doubleValue() - point.getX().doubleValue());
		double dy = Math.abs(getY().doubleValue() - point.getY().doubleValue());
		double d = Math.sqrt(dx * dx + dy * dy);
		return d;
	}
}
