package io.hhplus.tdd.point;

import static io.hhplus.tdd.point.TransactionType.CHARGE;
import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;

@SpringBootTest
class PointServiceIntegrationTest {

	@Autowired
	private PointService pointService;

	@Autowired
	private UserPointTable userPointTable;

	@Autowired
	private PointHistoryTable pointHistoryTable;



	@DisplayName("사용자의 포인트를 조회할 수 있다.")
	@Test
	void point() throws Exception {
		// given
		final long id = System.currentTimeMillis();
		userPointTable.insertOrUpdate(id, 2000L);

		// when
		final UserPoint result = pointService.point(id);

		// then
		assertThat(result).isNotNull()
			.extracting("id", "point")
			.contains(id, 2000L);
	}

	@DisplayName("최초 사용자의 포인트를 조회하면 0포인트로 조회된다.")
	@Test
	void givenNewUserWhenPointReturnZeroPoint() throws Exception {
		// given
		final long id = System.currentTimeMillis();

		// when
		final UserPoint result = pointService.point(id);

		// then
		assertThat(result).isNotNull()
			.extracting("id", "point")
			.contains(id, 0L);
	}

	@DisplayName("사용자의 아이디로 포인트 히스토리를 조회할 수 있다.")
	@Test
	void history() throws Exception {
		// given
		final long userId = System.currentTimeMillis();
		final long amount = 2000L;
		final TransactionType type = CHARGE;
		final long updateMillis = System.currentTimeMillis();
		pointHistoryTable.insert(userId, amount, type, updateMillis);

		// when
		final List<PointHistory> results = pointService.history(userId);

		// then
		assertThat(results).hasSize(1)
			.extracting("userId", "amount", "type", "updateMillis")
			.containsExactlyInAnyOrder(
				tuple(userId, 2000L, CHARGE, updateMillis)
			);
	}

	@DisplayName("포인트 히스토리가 없는 사용자가 히스토리를 조회하면 빈 리스트를 반환한다.")
	@Test
	void emptyHistoryUserReturnEmptyList() throws Exception {
		// given
		final long userId = System.currentTimeMillis();

		// when
		final List<PointHistory> results = pointService.history(userId);

		// then
		assertThat(results).isEmpty();
	}

	@DisplayName("1000 포인트를 보유하고 있을 때 1000포인트를 충전하면 2000포인트가 된다.")
	@Test
	void charge() throws Exception {
		// given
		final long userId = System.currentTimeMillis();
		final long point = 1000L;
		final long amount = 1000L;
		userPointTable.insertOrUpdate(userId, point);

		// when
		final UserPoint result = pointService.charge(userId, amount);

		// then
		assertThat(result).isNotNull()
			.extracting("id", "point")
			.contains(userId, 2000L);

		final List<PointHistory> histories = pointHistoryTable.selectAllByUserId(userId);
		assertThat(histories).hasSize(1)
			.extracting("userId", "amount", "type", "updateMillis")
			.contains(tuple(userId, amount, CHARGE, result.updateMillis()));
	}

	@DisplayName("양수가 아닌 포인트를 충전하려고 하면 예외가 발생한다.")
	@Test
	void chargeZeroPoint() throws Exception {
		// given
		final long userId = System.currentTimeMillis();
		final long point = 100L;
		final long amount = 0L;
		userPointTable.insertOrUpdate(userId, point);

		// when & then
		assertThatThrownBy(() -> pointService.charge(userId, amount))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("충전 포인트는 양수여야합니다.");
	}

	@DisplayName("1회 최대 충전 가능량을 초과해 충전하려고 하면 예외가 발생한다.")
	@Test
	void chargeOverMaxOncePoint() throws Exception {
		// given
		final long userId = System.currentTimeMillis();
		final long point = 100L;
		final long amount = 100_001L;
		userPointTable.insertOrUpdate(userId, point);

		// when & then
		assertThatThrownBy(() -> pointService.charge(userId, amount))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("1회 최대 충전가능량은 100,000원입니다.");
	}

	@DisplayName("최대 포인트 이상으로 충전하려고 하면 예외가 발생한다.")
	@Test
	void chargeOverMaxPoint() throws Exception {
		// given
		final long userId = System.currentTimeMillis();
		final long point = 100_000_000L;
		final long amount = 1L;
		userPointTable.insertOrUpdate(userId, point);

		// when & then
		assertThatThrownBy(() -> pointService.charge(userId, amount))
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("최대 포인트는 100,000,000원입니다.");
	}
}
