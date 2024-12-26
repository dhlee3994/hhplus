package io.hhplus.lecture.lecture.infrastructure;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import io.hhplus.lecture.lecture.domain.Lecture;

public interface LectureJpaRepository extends JpaRepository<Lecture, Long> {

	Optional<Lecture> findById(final Long lectureId);

	@Query("""
		select l
		from Lecture l
		where l.period.startedAt < l.period.endedAt
		  and not (l.period.startedAt > :endedAt or l.period.endedAt < :startedAt)
		""")
	Slice<Lecture> findAllByApplicationPeriod(
		@Param("startedAt") final LocalDateTime applicationStartedAt,
		@Param("endedAt") final LocalDateTime applicationEndedAt,
		final Pageable pageable
	);
}
