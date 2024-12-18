package io.hhplus.tdd.point;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserPointTest {

	@DisplayName("포인트를 충전하면 현재 보유 포인트에 충전량을 더한 값을 반환한다.")
	@Test
	void charge() throws Exception {
		// given
		final UserPoint userPoint = new UserPoint(1L, 1000L, System.currentTimeMillis());

		// when
		final long result = userPoint.charge(1000L);

		// then
		assertThat(result).isEqualTo(2000L);
	}

	@DisplayName("양수가 아닌 포인트를 충전하면 예외가 발생한다.")
	@Test
	void chargeZeroPoint() throws Exception {
		// given
		final UserPoint userPoint = new UserPoint(1L, 1000L, System.currentTimeMillis());

		// when & then
		assertThatThrownBy(() -> userPoint.charge(0L))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("충전 포인트는 양수여야합니다.");
	}

	@DisplayName("1회 최대 충전가능량 이상으로 충전을 시도하면 오류가 발생한다.")
	@Test
	void chargeMaxOncePoint() throws Exception {
		// given
		final UserPoint userPoint = new UserPoint(1L, 1000L, System.currentTimeMillis());

		// when & then
		assertThatThrownBy(() -> userPoint.charge(100_001L))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("1회 최대 충전가능량은 100,000원입니다.");
	}

	@DisplayName("최대 포인트 보유량 이상으로 충전을 시도하면 예외가 발생한다.")
	@Test
	void chargeMaxPoint() throws Exception {
		// given
		final UserPoint userPoint = new UserPoint(1L, 99_999_999L, System.currentTimeMillis());

		// when & then
		assertThatThrownBy(() -> userPoint.charge(2L))
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("최대 포인트는 100,000,000원입니다. 충전 가능 포인트는 1입니다.");
	}
}
