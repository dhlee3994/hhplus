package io.hhplus.lecture.lecture.presentation.request;

import static io.hhplus.lecture.global.exception.LectureErrorCode.INVALID_SEARCH_PERIOD;

import java.time.LocalDateTime;

import io.hhplus.lecture.global.exception.LectureException;
import io.hhplus.lecture.lecture.application.request.LectureSearchRequest;

public record LectureSearchApiRequest(
	Long userId,
	LocalDateTime applicationStartedAt,
	LocalDateTime applicationEndedAt
) {
	public LectureSearchApiRequest {
		if (applicationStartedAt == null) {
			applicationStartedAt = LocalDateTime.now();
		}

		if (applicationEndedAt == null) {
			applicationEndedAt = LocalDateTime.now();
		}

		if (applicationEndedAt.equals(applicationStartedAt) || applicationEndedAt.isBefore(applicationStartedAt)) {
			throw new LectureException(INVALID_SEARCH_PERIOD);
		}
	}

	public LectureSearchRequest toServiceDto() {
		return new LectureSearchRequest(userId, applicationStartedAt, applicationEndedAt);
	}
}
