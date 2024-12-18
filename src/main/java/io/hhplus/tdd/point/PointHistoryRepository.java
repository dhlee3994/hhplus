package io.hhplus.tdd.point;

import java.util.List;

import org.springframework.stereotype.Repository;

import io.hhplus.tdd.database.PointHistoryTable;

@Repository
public class PointHistoryRepository {

	private final PointHistoryTable pointHistoryTable;

	public PointHistoryRepository(final PointHistoryTable pointHistoryTable) {
		this.pointHistoryTable = pointHistoryTable;
	}

	public List<PointHistory> history(final long id) {
		return pointHistoryTable.selectAllByUserId(id);
	}

	public PointHistory charge(final long id, final long amount, final long updateMillis) {
		return pointHistoryTable.insert(id, amount, TransactionType.CHARGE, updateMillis);
	}
}
