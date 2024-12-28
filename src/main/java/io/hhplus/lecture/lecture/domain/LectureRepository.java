package io.hhplus.lecture.lecture.domain;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import io.hhplus.lecture.lecture.infrastructure.request.LectureSearchCondition;

public interface LectureRepository {

	Lecture getById(final Long id);

	Slice<Lecture> getEnrollableLectures(final LectureSearchCondition condition, final Pageable pageable);
}
