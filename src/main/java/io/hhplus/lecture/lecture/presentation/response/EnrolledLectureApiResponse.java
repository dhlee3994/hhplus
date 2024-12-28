package io.hhplus.lecture.lecture.presentation.response;

import java.time.LocalDateTime;

import io.hhplus.lecture.lecture.application.response.EnrollLectureResponse;

public record EnrolledLectureApiResponse(
	Long lectureId,
	String title,
	String lecturerName,
	int currentEnrolledCount,
	LocalDateTime enrolledAt
) {
	public static EnrolledLectureApiResponse of(final EnrollLectureResponse response) {
		return new EnrolledLectureApiResponse(
			response.lectureId(),
			response.title(),
			response.lecturerName(),
			response.currentEnrolledCount(),
			response.enrolledAt()
		);
	}
}
