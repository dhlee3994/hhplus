package io.hhplus.tdd.point;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class PointService {

	private final UserPointRepository userPointRepository;
	private final PointHistoryRepository pointHistoryRepository;

	public PointService(
		final UserPointRepository userPointRepository,
		final PointHistoryRepository pointHistoryRepository
	) {
		this.userPointRepository = userPointRepository;
		this.pointHistoryRepository = pointHistoryRepository;
	}

	public UserPoint point(final long id) {
		return userPointRepository.point(id);
	}

	public List<PointHistory> history(final long id) {
		return pointHistoryRepository.history(id);
	}
}
