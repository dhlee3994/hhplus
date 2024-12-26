package io.hhplus.lecture.lecture.domain;

import java.util.List;
import java.util.Optional;

import io.hhplus.lecture.lecture.infrastructure.response.EnrollLectureResult;

public interface EnrolledLectureRepository {

	EnrolledLecture save(final EnrolledLecture enrolledLecture);

	List<EnrollLectureResult> findAllByUserId(final Long userId);

	Optional<EnrolledLecture> findByLectureIdAndUserId(final Long lectureId, final Long userId);

	boolean isEnrolledLecture(final Long lectureId, final Long userId);
}
