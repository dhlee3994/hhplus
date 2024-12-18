package io.hhplus.tdd.point;

import org.springframework.stereotype.Service;

@Service
public class PointService {

	private final UserPointRepository userPointRepository;

	public PointService(final UserPointRepository userPointRepository) {
		this.userPointRepository = userPointRepository;
	}

	public UserPoint point(final long id) {
		return userPointRepository.point(id);
	}
}
