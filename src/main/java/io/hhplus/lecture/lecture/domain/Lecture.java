package io.hhplus.lecture.lecture.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import io.hhplus.lecture.global.BaseEntity;
import io.hhplus.lecture.global.exception.LectureErrorCode;
import io.hhplus.lecture.global.exception.LectureException;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "lecture", uniqueConstraints = @UniqueConstraint(name = "uk_lecture_title", columnNames = "title"))
@Entity
public class Lecture extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(length = 100, nullable = false)
	private String title;

	@Column(length = 30, nullable = false)
	private String lecturerName;

	@Column(nullable = false)
	private int enrolledCount;

	@AttributeOverrides(value = {
		@AttributeOverride(name = "startedAt", column = @Column(name = "application_start_date_time")),
		@AttributeOverride(name = "endedAt", column = @Column(name = "application_end_date_time"))
	})
	@Embedded
	private Period period;

	@Builder
	private Lecture(final Long id, final String title, final String lecturerName, final int enrolledCount, final Period period) {
		this.id = id;
		this.title = title;
		this.lecturerName = lecturerName;
		this.enrolledCount = enrolledCount;
		this.period = period;
	}

	public EnrolledLecture enroll(final int capacity, final Long userId) {
		if (canNotEnroll(capacity)) {
			throw new LectureException(LectureErrorCode.LECTURE_IS_FULL);
		}
		this.enrolledCount++;

		return EnrolledLecture.builder()
			.lectureId(this.id)
			.userId(userId)
			.build();
	}

	public boolean isEnrollable(final int capacity) {
		return enrolledCount < capacity;
	}

	private boolean canNotEnroll(final int capacity) {
		return !isEnrollable(capacity);
	}

	public String getEnrollable(final int capacity, final boolean isEnrolled) {
		if (this.enrolledCount == capacity) {
			return "N";
		}
		return isEnrolled ? "N" : "Y";
	}
}

