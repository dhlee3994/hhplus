package io.hhplus.lecture.lecture.infrastructure.response;

import java.time.LocalDateTime;

import io.hhplus.lecture.lecture.domain.EnrolledLecture;
import io.hhplus.lecture.lecture.domain.Lecture;

public record EnrollLectureResult(
	Long lectureId,
	String title,
	String lecturerName,
	int currentEnrolledCount,
	LocalDateTime enrolledAt
){
	public static EnrollLectureResult of(final Lecture lecture, final EnrolledLecture enrolledLecture) {
		return new EnrollLectureResult(
			lecture.getId(),
			lecture.getTitle(),
			lecture.getLecturerName(),
			lecture.getEnrolledCount(),
			enrolledLecture.getCreatedAt()
		);
	}
}
