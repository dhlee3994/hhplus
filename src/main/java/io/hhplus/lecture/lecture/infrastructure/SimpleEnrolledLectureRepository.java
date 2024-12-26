package io.hhplus.lecture.lecture.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import io.hhplus.lecture.lecture.domain.EnrolledLecture;
import io.hhplus.lecture.lecture.domain.EnrolledLectureRepository;
import io.hhplus.lecture.lecture.infrastructure.response.EnrollLectureResult;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class SimpleEnrolledLectureRepository implements EnrolledLectureRepository {

	private final EnrolledJpaRepository enrolledJpaRepository;

	@Override
	public EnrolledLecture save(final EnrolledLecture enrolledLecture) {
		return enrolledJpaRepository.save(enrolledLecture);
	}

	@Override
	public Optional<EnrolledLecture> findByLectureIdAndUserId(final Long lectureId, final Long userId) {
		return enrolledJpaRepository.findByLectureIdAndUserId(lectureId, userId);
	}

	@Override
	public boolean isEnrolledLecture(final Long lectureId, final Long userId) {
		return findByLectureIdAndUserId(lectureId, userId).isPresent();
	}
}
