package io.hhplus.tdd.point;

import org.springframework.stereotype.Repository;

import io.hhplus.tdd.database.UserPointTable;

@Repository
public class UserPointRepository {

	private final UserPointTable userPointTable;

	public UserPointRepository(final UserPointTable userPointTable) {
		this.userPointTable = userPointTable;
	}

	public UserPoint point(final long id) {
		return userPointTable.selectById(id);
	}
}