package io.hhplus.lecture.lecture.infrastructure;

import static io.hhplus.lecture.global.exception.LectureErrorCode.INVALID_LECTURE_ID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

import io.hhplus.lecture.global.exception.LectureException;
import io.hhplus.lecture.lecture.domain.Lecture;
import io.hhplus.lecture.lecture.domain.LectureRepository;
import io.hhplus.lecture.lecture.infrastructure.request.LectureSearchCondition;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class SimpleLectureRepository implements LectureRepository {

	private final LectureJpaRepository lectureJpaRepository;

	@Override
	public Lecture getById(final Long id) {
		return lectureJpaRepository.findById(id)
			.orElseThrow(() -> new LectureException(INVALID_LECTURE_ID));
	}

	@Override
	public Slice<Lecture> getEnrollableLectures(
		final LectureSearchCondition condition,
		final Pageable pageable
	) {
		return lectureJpaRepository.findAllByApplicationPeriod(
			condition.applicationStartedAt(),
			condition.applicationEndedAt(),
			pageable
		);
	}
}
