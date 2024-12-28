package io.hhplus.lecture.lecture.domain;

import static io.hhplus.lecture.global.exception.LectureErrorCode.INVALID_LECTURE_PERIOD;

import jakarta.persistence.Embeddable;
import java.time.LocalDateTime;

import io.hhplus.lecture.global.exception.LectureException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Embeddable
public class Period {

	private LocalDateTime startedAt;
	private LocalDateTime endedAt;

	public Period(final LocalDateTime startedAt, final LocalDateTime endedAt) {
		this.startedAt = startedAt;
		this.endedAt = endedAt;
	}

	private void validatePeriod(final LocalDateTime startedAt, final LocalDateTime endedAt) {
		if (startedAt == null || endedAt == null) {
			throw new LectureException(INVALID_LECTURE_PERIOD);
		}

		if (startedAt.isAfter(endedAt)) {
			throw new LectureException(INVALID_LECTURE_PERIOD);
		}
	}
}
