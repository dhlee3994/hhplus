package io.hhplus.tdd.point;

import java.util.List;
import java.util.concurrent.locks.Lock;

import org.springframework.stereotype.Service;

@Service
public class PointService {

	private final UserPointRepository userPointRepository;
	private final PointHistoryRepository pointHistoryRepository;
	private final LockManager lockManager;

	public PointService(
		final UserPointRepository userPointRepository,
		final PointHistoryRepository pointHistoryRepository,
		final LockManager lockManager
	) {
		this.userPointRepository = userPointRepository;
		this.pointHistoryRepository = pointHistoryRepository;
		this.lockManager = lockManager;
	}

	public UserPoint point(final long id) {
		return userPointRepository.point(id);
	}

	public List<PointHistory> history(final long id) {
		return pointHistoryRepository.history(id);
	}

	public UserPoint charge(final long id, final long amount) {
		UserPoint chargedUserPoint;

		final Lock lock = lockManager.getLock(id);
		lock.lock();
		try {
			final UserPoint userPoint = userPointRepository.point(id);
			final long chargedPoint = userPoint.charge(amount);
			chargedUserPoint = userPointRepository.charge(id, chargedPoint);
		} finally {
			lock.unlock();
		}

		pointHistoryRepository.charge(id, amount, chargedUserPoint.updateMillis());

		return chargedUserPoint;
	}

	public UserPoint use(final long id, final long amount) {
		UserPoint chargedUserPoint;

		final Lock lock = lockManager.getLock(id);
		lock.lock();
		try {
			final UserPoint userPoint = userPointRepository.point(id);
			final long usedPoint = userPoint.use(amount);
			chargedUserPoint = userPointRepository.use(id, usedPoint);
		} finally {
			lock.unlock();
		}

		pointHistoryRepository.use(id, amount, chargedUserPoint.updateMillis());

		return chargedUserPoint;
	}
}
