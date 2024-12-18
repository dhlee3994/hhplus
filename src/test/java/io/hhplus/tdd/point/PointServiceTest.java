package io.hhplus.tdd.point;

import static io.hhplus.tdd.point.TransactionType.CHARGE;
import static io.hhplus.tdd.point.TransactionType.USE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.BDDMockito.given;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {

	@InjectMocks
	private PointService pointService;

	@Mock
	private UserPointRepository userPointRepository;

	@Mock
	private PointHistoryRepository pointHistoryRepository;

	@DisplayName("사용자의 포인트를 조회할 수 있다.")
	@Test
	void point() throws Exception {
		// given
		final long id = System.currentTimeMillis();
		given(userPointRepository.point(id))
			.willReturn(new UserPoint(id, 2000L, System.currentTimeMillis()));

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
		given(userPointRepository.point(id))
			.willReturn(UserPoint.empty(id));

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
		given(pointHistoryRepository.history(userId))
			.willReturn(List.of(
				new PointHistory(1L, userId, 2000L, CHARGE, System.currentTimeMillis()),
				new PointHistory(2L, userId, 1000L, USE, System.currentTimeMillis())
			));

		// when
		final List<PointHistory> results = pointService.history(userId);

		// then
		assertThat(results).hasSize(2)
			.extracting("userId", "amount", "type")
			.containsExactlyInAnyOrder(
				tuple(userId, 2000L, CHARGE),
				tuple(userId, 1000L, USE)
			);
	}

	@DisplayName("포인트 히스토리가 없는 사용자가 히스토리를 조회하면 빈 리스트를 반환한다.")
	@Test
	void emptyHistoryUserReturnEmptyList() throws Exception {
		// given
		final long userId = System.currentTimeMillis();
		given(pointHistoryRepository.history(userId))
			.willReturn(List.of());

		// when
		final List<PointHistory> results = pointService.history(userId);

		// then
		assertThat(results).isEmpty();
	}
}
