package io.hhplus.lecture.lecture.presentation.request;

import io.hhplus.lecture.lecture.application.request.EnrollLectureRequest;

public record EnrollLectureApiRequest(
	Long userId,
	Long lectureId
) {
	public EnrollLectureRequest toServiceDto() {
		return new EnrollLectureRequest(userId, lectureId);
	}
}
