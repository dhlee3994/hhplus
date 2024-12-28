package io.hhplus.lecture.lecture.presentation.request;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.hhplus.lecture.global.BaseTest;
import io.hhplus.lecture.global.exception.LectureErrorCode;
import io.hhplus.lecture.global.exception.LectureException;

class LectureSearchApiRequestTest extends BaseTest {

	@DisplayName("조회 시작일이 비어있어도 현재 날짜을 조회 시작일로 하는 객체가 생성된다.")
	@Test
	void givenStartedAtIsNullWhenCreateSearchRequestThenStartedAtIsToday() throws Exception {
		// given
		final LocalDateTime now = LocalDateTime.now();
		final LocalDateTime startedAt = null;
		final LocalDateTime endedAt = now.plusDays(1);

		// when
		final LectureSearchApiRequest searchRequest = new LectureSearchApiRequest(null, startedAt, endedAt);

		// then
		assertThat(searchRequest.applicationStartedAt()).isAfterOrEqualTo(now);
		assertThat(searchRequest.applicationEndedAt()).isEqualTo(endedAt);
	}

	@DisplayName("조회 종료일이 비어있어도 현재 날짜를 조회 종료일로 하는 객체가 생성된다.")
	@Test
	void givenEndedAtIsNullWhenCreateSearchRequestThenEndedAtIsToday() throws Exception {
		// given
		final LocalDateTime now = LocalDateTime.now();
		final LocalDateTime startedAt = now.minusDays(1);
		final LocalDateTime endedAt = null;

		// when
		final LectureSearchApiRequest searchRequest = new LectureSearchApiRequest(null, startedAt, endedAt);

		// then
		assertThat(searchRequest.applicationStartedAt()).isEqualTo(startedAt);
		assertThat(searchRequest.applicationEndedAt()).isAfterOrEqualTo(now);
	}

	@DisplayName("조회 종료일이 조회 시작일과 같으면 객체 생성시 예외가 발생한다.")
	@Test
	void givenEndedAtIsEqualToStartedAtWhenCreateSearchRequestThrowsException() throws Exception {
		// given
		final LocalDateTime startedAt = LocalDateTime.of(2024, 12, 25, 16, 22);
		final LocalDateTime endedAt = LocalDateTime.of(2024, 12, 25, 16, 22);

		// when & then
		assertThatThrownBy(() -> new LectureSearchApiRequest(null, startedAt, endedAt))
			.isInstanceOf(LectureException.class)
			.hasMessage(LectureErrorCode.INVALID_SEARCH_PERIOD.getMessage());
	}

	@DisplayName("조회 종료일이 조회 시작일보다 이전이면 객체 생성시 예외가 발생한다.")
	@Test
	void givenEndedAtIsBeforeThenStartedAtWhenCreateSearchRequestThrowsException() throws Exception {
		// given
		final LocalDateTime startedAt = LocalDateTime.of(2024, 12, 25, 16, 22);
		final LocalDateTime endedAt = LocalDateTime.of(2024, 12, 25, 16, 21);

		// when & then
		assertThatThrownBy(() -> new LectureSearchApiRequest(null, startedAt, endedAt))
			.isInstanceOf(LectureException.class)
			.hasMessage(LectureErrorCode.INVALID_SEARCH_PERIOD.getMessage());
	}
}
