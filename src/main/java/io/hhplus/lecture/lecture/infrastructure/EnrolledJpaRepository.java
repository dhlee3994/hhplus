package io.hhplus.lecture.lecture.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import io.hhplus.lecture.lecture.domain.EnrolledLecture;
import io.hhplus.lecture.lecture.infrastructure.response.EnrollLectureResult;

public interface EnrolledJpaRepository extends JpaRepository<EnrolledLecture, Long> {

	Optional<EnrolledLecture> findByLectureIdAndUserId(Long lectureId, Long userId);
}
