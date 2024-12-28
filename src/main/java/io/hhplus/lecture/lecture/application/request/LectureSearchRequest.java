package io.hhplus.lecture.lecture.application.request;

import static io.hhplus.lecture.global.exception.LectureErrorCode.INVALID_SEARCH_PERIOD;

import java.time.LocalDateTime;

import io.hhplus.lecture.global.exception.LectureException;
import io.hhplus.lecture.lecture.infrastructure.request.LectureSearchCondition;

public record LectureSearchRequest(
	Long userId,
	LocalDateTime applicationStartedAt,
	LocalDateTime applicationEndedAt
) {
	public LectureSearchRequest {
		if (applicationStartedAt == null || applicationEndedAt == null) {
			throw new LectureException(INVALID_SEARCH_PERIOD);
		}

		if (applicationEndedAt.equals(applicationStartedAt) || applicationEndedAt.isBefore(applicationStartedAt)) {
			throw new LectureException(INVALID_SEARCH_PERIOD);
		}
	}

	public LectureSearchCondition toRepositoryDto() {
		return new LectureSearchCondition(userId, applicationStartedAt, applicationEndedAt);
	}
}
