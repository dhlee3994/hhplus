package io.hhplus.lecture.lecture.application.response;

import java.time.LocalDateTime;

import io.hhplus.lecture.lecture.domain.Lecture;
import io.hhplus.lecture.lecture.domain.Period;

public record LectureSearchResponse(
	Long id,
	String title,
	String lecturerName,
	int capacity,
	int enrolledCount,
	LocalDateTime applicationStartedAt,
	LocalDateTime applicationEndedAt,
	String enrollable
) {
	public static LectureSearchResponse of(
		final Lecture lecture,
		final int capacity,
		final String enrollable
	) {
		final Period period = lecture.getPeriod();
		return new LectureSearchResponse(
			lecture.getId(),
			lecture.getTitle(),
			lecture.getLecturerName(),
			capacity,
			lecture.getEnrolledCount(),
			period.getStartedAt(),
			period.getEndedAt(),
			enrollable
		);
	}
}
