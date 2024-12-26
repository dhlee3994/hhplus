package io.hhplus.lecture.lecture.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import io.hhplus.lecture.lecture.domain.EnrolledLecture;
import io.hhplus.lecture.lecture.infrastructure.response.EnrollLectureResult;

public interface EnrolledJpaRepository extends JpaRepository<EnrolledLecture, Long> {

	Optional<EnrolledLecture> findByLectureIdAndUserId(Long lectureId, Long userId);

	@Query("""
		select new io.hhplus.lecture.lecture.infrastructure.response.EnrollLectureResult(
				e.lectureId, 
			   	l.title, 
			   	l.lecturerName, 
			   	l.enrolledCount, 
			   	e.createdAt)
		from EnrolledLecture e
			join Lecture l on e.lectureId = l.id
		where e.userId = :userId
	""")
	List<EnrollLectureResult> findAllByUserId(final Long userId);
}
