package io.hhplus.lecture.lecture.application.response;

import java.time.LocalDateTime;

import io.hhplus.lecture.lecture.domain.EnrolledLecture;
import io.hhplus.lecture.lecture.domain.Lecture;
import io.hhplus.lecture.lecture.infrastructure.response.EnrollLectureResult;

public record EnrollLectureResponse(
	Long lectureId,
	String title,
	String lecturerName,
	int currentEnrolledCount,
	LocalDateTime enrolledAt
) {
	public static EnrollLectureResponse of(final Lecture lecture, final EnrolledLecture enrolledLecture) {
		return new EnrollLectureResponse(
			lecture.getId(),
			lecture.getTitle(),
			lecture.getLecturerName(),
			lecture.getEnrolledCount(),
			enrolledLecture.getCreatedAt()
		);
	}

	public static EnrollLectureResponse of(final EnrollLectureResult enrollLectureResult) {
		return new EnrollLectureResponse(
			enrollLectureResult.lectureId(),
			enrollLectureResult.title(),
			enrollLectureResult.lecturerName(),
			enrollLectureResult.currentEnrolledCount(),
			enrollLectureResult.enrolledAt()
		);
	}
}
