package io.hhplus.tdd.point;

public record UserPoint(
	long id,
	long point,
	long updateMillis
) {

	private static final Long MAX_POINT = 100_000_000L;
	private static final Long MAX_CHARGE_POINT = 100_000L;

	public static UserPoint empty(long id) {
		return new UserPoint(id, 0, System.currentTimeMillis());
	}

	public long charge(final long point) {
		validateChargePoint(point);
		return this.point + point;
	}

	private void validateChargePoint(final long chargePoint) {
		if (chargePoint <= 0) {
			throw new IllegalStateException("충전 포인트는 양수여야합니다.");
		}

		if (chargePoint > MAX_CHARGE_POINT) {
			throw new IllegalStateException("1회 최대 충전가능량은 100,000원입니다.");
		}

		if (this.point + chargePoint > MAX_POINT) {
			throw new IllegalStateException("최대 포인트는 100,000,000원입니다. 충전 가능 포인트는 " + (MAX_POINT - this.point) + "입니다.");
		}
	}
}
