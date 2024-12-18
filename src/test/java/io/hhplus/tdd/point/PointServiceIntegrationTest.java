package io.hhplus.tdd.point;

import static io.hhplus.tdd.point.TransactionType.CHARGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

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
}
