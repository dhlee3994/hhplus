package io.hhplus.lecture.lecture.infrastructure.request;

import java.time.LocalDateTime;

public record LectureSearchCondition(
	Long userId,
	LocalDateTime applicationStartedAt,
	LocalDateTime applicationEndedAt
) {
}
