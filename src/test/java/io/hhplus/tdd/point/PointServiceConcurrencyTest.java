package io.hhplus.tdd.point;

import static io.hhplus.tdd.point.TransactionType.CHARGE;
import static io.hhplus.tdd.point.TransactionType.USE;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PointServiceConcurrencyTest {

	@Autowired
	private PointService pointService;

	@Autowired
	private UserPointRepository userPointRepository;

	@Autowired
	private PointHistoryRepository pointHistoryRepository;

	@DisplayName("한 사람에 대한 포인트 충전 기능은 동시성문제가 발생하지 않는다.")
	@Test
	void charge() throws Exception {
		// given
		final long userId = System.currentTimeMillis();

		final int threadCount = 10;
		final ExecutorService executorService = Executors.newFixedThreadPool(32);
		final CountDownLatch countDownLatch = new CountDownLatch(threadCount);

		// when
		for (int i = 1; i <= threadCount; i++) {
			final int amount = i;
			executorService.execute(() -> {
				try {
					pointService.charge(userId, amount);
				} finally {
					countDownLatch.countDown();
				}
			});
		}
		countDownLatch.await();
		executorService.shutdown();

		// then
		final UserPoint userPoint = userPointRepository.point(userId);
		assertThat(userPoint.point()).isEqualTo(55L);

		final List<PointHistory> histories = pointHistoryRepository.history(userId);
		assertThat(histories).hasSize(threadCount);

		for (PointHistory history : histories) {
			assertThat(history.type()).isEqualTo(CHARGE);
		}
	}

	@DisplayName("한 사람에 대한 포인트 사용 기능은 동시성문제가 발생하지 않는다.")
	@Test
	void use() throws Exception {
		// given
		final long userId = System.currentTimeMillis();
		userPointRepository.charge(userId, 100L);

		final int threadCount = 10;
		final ExecutorService executorService = Executors.newFixedThreadPool(32);
		final CountDownLatch countDownLatch = new CountDownLatch(threadCount);

		// when
		for (int i = 1; i <= threadCount; i++) {
			final int amount = i;
			executorService.submit(() -> {
				try {
					pointService.use(userId, amount);
				} finally {
					countDownLatch.countDown();
				}
			});
		}
		countDownLatch.await();
		executorService.shutdown();

		// then
		final UserPoint userPoint = userPointRepository.point(userId);
		assertThat(userPoint.point()).isEqualTo(45L);

		final List<PointHistory> histories = pointHistoryRepository.history(userId);
		assertThat(histories).hasSize(threadCount);

		for (PointHistory history : histories) {
			assertThat(history.type()).isEqualTo(USE);
		}
	}

	@DisplayName("한 사람에 대한 포인트 충전/사용 요청이 동시에 들어와도 동시성문제가 발생하지 않는다.")
	@Test
	void chargeAndUse() throws Exception {
		// given
		final long userId = System.currentTimeMillis();
		userPointRepository.charge(userId, 100L);

		final int threadCount = 10;
		final ExecutorService executorService = Executors.newFixedThreadPool(32);
		final CountDownLatch countDownLatch = new CountDownLatch(threadCount);

		// when
		for (int i = 1; i <= threadCount; i++) {
			final int amount = i;
			executorService.submit(() -> {
				try {
					if (amount % 3 == 0) {
						// 3 + 6 + 9 = 18
						pointService.charge(userId, amount);
					} else {
						// 1 + 2 + 4 + 5 + 7 + 8 + 10 = 37
						pointService.use(userId, amount);
					}
				} finally {
					countDownLatch.countDown();
				}
			});
		}
		countDownLatch.await();
		executorService.shutdown();

		// then
		final UserPoint userPoint = userPointRepository.point(userId);
		assertThat(userPoint.point()).isEqualTo(81L);

		final List<PointHistory> histories = pointHistoryRepository.history(userId);
		assertThat(histories).hasSize(threadCount);

		for (PointHistory history : histories) {
			if (history.amount() % 3 == 0) {
				assertThat(history.type()).isEqualTo(CHARGE);
			} else {
				assertThat(history.type()).isEqualTo(USE);
			}
		}
	}
}
