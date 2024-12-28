package io.hhplus.lecture.lecture.presentation.response;

import java.time.LocalDateTime;

import io.hhplus.lecture.lecture.application.response.LectureSearchResponse;

public record LectureSearchApiResponse(
	Long id,
	String title,
	String lecturerName,
	int capacity,
	int enrolledCount,
	LocalDateTime applicationStartedAt,
	LocalDateTime applicationEndedAt,
	String enrollable
) {
	public static LectureSearchApiResponse of(final LectureSearchResponse response) {
		return new LectureSearchApiResponse(
			response.id(),
			response.title(),
			response.lecturerName(),
			response.capacity(),
			response.enrolledCount(),
			response.applicationStartedAt(),
			response.applicationEndedAt(),
			response.enrollable()
		);
	}
}
