package io.hhplus.lecture.lecture.application.request;

public record EnrollLectureRequest(
	Long userId,
	Long lectureId
) {
}
