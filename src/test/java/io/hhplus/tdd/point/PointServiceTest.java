package io.hhplus.tdd.point;

import static io.hhplus.tdd.point.TransactionType.CHARGE;
import static io.hhplus.tdd.point.TransactionType.USE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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

	@DisplayName("1000 포인트를 보유하고 있을 때 1000포인트를 충전하면 2000포인트가 된다.")
	@Test
	void charge() throws Exception {
		// given
		final long userId = System.currentTimeMillis();
		final long point = 1000L;
		final UserPoint userPoint = new UserPoint(userId, point, System.currentTimeMillis());
		given(userPointRepository.point(userId))
			.willReturn(userPoint);

		final long amount = 1000L;
		given(userPointRepository.charge(userId, userPoint.charge(amount)))
			.willReturn(new UserPoint(userId, userPoint.charge(amount), System.currentTimeMillis()));

		// when
		final UserPoint result = pointService.charge(userId, amount);

		// then
		assertThat(result).isNotNull()
			.extracting("id", "point")
			.contains(userId, 2000L);
		verify(pointHistoryRepository, times(1)).charge(userId, amount, result.updateMillis());
	}

	@DisplayName("양수가 아닌 포인트를 충전하려고 하면 예외가 발생한다.")
	@Test
	void chargeZeroPoint() throws Exception {
		// given
		final long userId = System.currentTimeMillis();
		final long point = 100L;
		final long amount = 0L;
		given(userPointRepository.point(userId))
			.willReturn(new UserPoint(userId, point, System.currentTimeMillis()));

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
		given(userPointRepository.point(userId))
			.willReturn(new UserPoint(userId, point, System.currentTimeMillis()));

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
		given(userPointRepository.point(userId))
			.willReturn(new UserPoint(userId, point, System.currentTimeMillis()));

		// when & then
		assertThatThrownBy(() -> pointService.charge(userId, amount))
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("최대 포인트는 100,000,000원입니다.");
	}

	@DisplayName("1000 포인트를 보유하고 있을 때 1000포인트를 사용하면 0포인트가 된다.")
	@Test
	void use() throws Exception {
		// given
		final long userId = System.currentTimeMillis();
		final long point = 1000L;
		final UserPoint userPoint = new UserPoint(userId, point, System.currentTimeMillis());
		given(userPointRepository.point(userId))
			.willReturn(userPoint);

		final long amount = 1000L;
		given(userPointRepository.use(userId, userPoint.use(amount)))
			.willReturn(new UserPoint(userId, userPoint.use(amount), System.currentTimeMillis()));

		// when
		final UserPoint result = pointService.use(userId, amount);

		// then
		assertThat(result).isNotNull()
			.extracting("id", "point")
			.contains(userId, 0L);
		verify(pointHistoryRepository, times(1)).use(userId, amount, result.updateMillis());
	}

	@DisplayName("양수가 아닌 포인트를 사용하려고 하면 예외가 발생한다.")
	@Test
	void useZeroPoint() throws Exception {
		// given
		final long userId = System.currentTimeMillis();
		final long point = 100L;
		final long amount = 0L;
		given(userPointRepository.point(userId))
			.willReturn(new UserPoint(userId, point, System.currentTimeMillis()));

		// when & then
		assertThatThrownBy(() -> pointService.use(userId, amount))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("사용 포인트는 양수여야합니다.");
	}

	@DisplayName("보유 포인트 이상으로 사용하려고 하면 예외가 발생한다.")
	@Test
	void useOverCurrentPoint() throws Exception {
		// given
		final long userId = System.currentTimeMillis();
		final long point = 1L;
		final long amount = 2L;
		given(userPointRepository.point(userId))
			.willReturn(new UserPoint(userId, point, System.currentTimeMillis()));

		// when & then
		assertThatThrownBy(() -> pointService.use(userId, amount))
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("보유 포인트보다 많이 사용할 수 없습니다. 사용 가능 포인트는 1입니다.");
	}
}
