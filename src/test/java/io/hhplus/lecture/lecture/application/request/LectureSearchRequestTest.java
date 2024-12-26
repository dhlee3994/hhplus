package io.hhplus.lecture.lecture.application.request;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.hhplus.lecture.global.BaseTest;
import io.hhplus.lecture.global.exception.LectureErrorCode;
import io.hhplus.lecture.global.exception.LectureException;

class LectureSearchRequestTest extends BaseTest {

	@DisplayName("조회 시작일 비어있으면 객체 생성시 예외가 발생한다.")
	@Test
	void givenStartedAtIsNullWhenCreateSearchConditionThrowsException() throws Exception {
		// given
		final LocalDateTime startedAt = null;
		final LocalDateTime endedAt = LocalDateTime.of(2024, 12, 25, 10, 00);

		// when & then
		assertThatThrownBy(() -> new LectureSearchRequest(null, startedAt, endedAt))
			.isInstanceOf(LectureException.class)
			.hasMessage(LectureErrorCode.INVALID_SEARCH_PERIOD.getMessage());
	}
	
	@DisplayName("조회 종료일 비어있으면 객체 생성시 예외가 발생한다.")
	@Test
	void givenEndedAtIsNullWhenCreateSearchConditionThrowsException() throws Exception {
		// given
		final LocalDateTime startedAt = LocalDateTime.of(2024, 12, 25, 10, 00);
		final LocalDateTime endedAt = null;

		// when & then
		assertThatThrownBy(() -> new LectureSearchRequest(null, startedAt, endedAt))
			.isInstanceOf(LectureException.class)
			.hasMessage(LectureErrorCode.INVALID_SEARCH_PERIOD.getMessage());
	}

	@DisplayName("조회 종료일이 조회 시작일과 같으면 객체 생성시 예외가 발생한다.")
	@Test
	void givenEndedAtIsEqualToStartedAtWhenCreateSearchConditionThrowsException() throws Exception {
		// given
		final LocalDateTime startedAt = LocalDateTime.of(2024, 12, 25, 16, 22);
		final LocalDateTime endedAt = LocalDateTime.of(2024, 12, 25, 16, 22);

		// when & then
		assertThatThrownBy(() -> new LectureSearchRequest(null, startedAt, endedAt))
			.isInstanceOf(LectureException.class)
			.hasMessage(LectureErrorCode.INVALID_SEARCH_PERIOD.getMessage());
	}

	@DisplayName("조회 종료일이 조회 시작일보다 이전이면 객체 생성시 예외가 발생한다.")
	@Test
	void givenEndedAtIsBeforeThenStartedAtWhenCreateSearchConditionThrowsException() throws Exception {
		// given
		final LocalDateTime startedAt = LocalDateTime.of(2024, 12, 25, 16, 22);
		final LocalDateTime endedAt = LocalDateTime.of(2024, 12, 25, 16, 21);

		// when & then
		assertThatThrownBy(() -> new LectureSearchRequest(null, startedAt, endedAt))
			.isInstanceOf(LectureException.class)
			.hasMessage(LectureErrorCode.INVALID_SEARCH_PERIOD.getMessage());
	}
}
