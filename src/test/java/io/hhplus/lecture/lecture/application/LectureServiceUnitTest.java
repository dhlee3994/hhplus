package io.hhplus.lecture.lecture.application;

import static io.hhplus.lecture.global.exception.LectureErrorCode.ALREADY_ENROLLED_LECTURE;
import static io.hhplus.lecture.global.exception.LectureErrorCode.LECTURE_IS_FULL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.SliceImpl;

import io.hhplus.lecture.config.TestConfig;
import io.hhplus.lecture.global.BaseTest;
import io.hhplus.lecture.global.exception.LectureException;
import io.hhplus.lecture.lecture.application.request.EnrollLectureRequest;
import io.hhplus.lecture.lecture.application.request.LectureSearchRequest;
import io.hhplus.lecture.lecture.application.response.EnrollLectureResponse;
import io.hhplus.lecture.lecture.domain.EnrolledLecture;
import io.hhplus.lecture.lecture.domain.EnrolledLectureRepository;
import io.hhplus.lecture.lecture.domain.Lecture;
import io.hhplus.lecture.lecture.domain.LectureRepository;
import io.hhplus.lecture.lecture.domain.Period;
import io.hhplus.lecture.lecture.infrastructure.response.EnrollLectureResult;

@Import(TestConfig.class)
@ExtendWith(MockitoExtension.class)
class LectureServiceUnitTest extends BaseTest {

	@InjectMocks
	private LectureService lectureService;

	@Mock
	private LectureRepository lectureRepository;

	@Mock
	private EnrolledLectureRepository enrolledLectureRepository;

	@Mock
	private LectureCapacityProvider lectureCapacityProvider;

	@DisplayName("신청 가능한 강의 목록 조회")
	@Nested
	class getAvailableLectures {

		@DisplayName("주어진 신청 시작일과 신청 종료일 사이에 신청 기간을 가진 해당하는 강의 목록을 조회할 수 있다.")
		@Test
		void getEnrollableLecturesByApplicationPeriod() throws Exception {
			// given
			final LocalDateTime startedAt = LocalDateTime.of(2024, 12, 20, 10, 0);
			final LocalDateTime endedAt = LocalDateTime.of(2024, 12, 21, 10, 0);

			final LectureSearchRequest lectureSearchRequest = new LectureSearchRequest(null, startedAt, endedAt);
			final Pageable pageable = PageRequest.of(0, 20);

			// @formatter:off
			given(lectureRepository.getEnrollableLectures(lectureSearchRequest.toRepositoryDto(), pageable))
				.willReturn(new SliceImpl<>(List.of(
					Lecture.builder().id(1L).period(new Period(startedAt, endedAt)).build(),
					Lecture.builder().id(2L).period(new Period(startedAt.plusSeconds(1), endedAt)).build(),
					Lecture.builder().id(3L).period(new Period(startedAt, endedAt.minusSeconds(1))).build(),
					Lecture.builder().id(4L).period(new Period(startedAt.minusSeconds(1), endedAt)).build(),
					Lecture.builder().id(5L).period(new Period(startedAt, endedAt.plusSeconds(1))).build()
				)));
			// @formatter:on

			// when
			final var result = lectureService.getEnrollableLectures(lectureSearchRequest, pageable);

			// then
			assertThat(result.getContent())
				.hasSize(5)
				.noneMatch(lectureResponse ->
					lectureResponse.applicationEndedAt().isBefore(startedAt) &&
						lectureResponse.applicationStartedAt().isAfter(endedAt)
				);
		}

		@DisplayName("강의 정원이 꽉 찬 강의는 강의 목록 조회시 강의 신청 가능 여부가 N으로 조회된다.")
		@Test
		void givenFullEnrolledCountLecturesWhenGetEnrollableLecturesThanIsEnrollableIsN() throws Exception {
			// given
			final LocalDateTime startedAt = LocalDateTime.of(2024, 12, 20, 10, 00);
			final LocalDateTime endedAt = LocalDateTime.of(2024, 12, 21, 10, 00);

			final LectureSearchRequest lectureSearchRequest = new LectureSearchRequest(null, startedAt, endedAt);
			final Pageable pageable = PageRequest.of(0, 20);

			final Long lectureId = 1L;
			final int capacity = lectureCapacityProvider.getCapacity(lectureId);
			given(lectureRepository.getEnrollableLectures(lectureSearchRequest.toRepositoryDto(), pageable))
				.willReturn(new SliceImpl<>(List.of(
					Lecture.builder()
						.id(lectureId)
						.enrolledCount(capacity)
						.period(new Period(startedAt, endedAt))
						.build()
				)));

			given(enrolledLectureRepository.isEnrolledLecture(eq(lectureId), any()))
				.willReturn(false);

			// when
			final var result = lectureService.getEnrollableLectures(lectureSearchRequest, pageable);

			// then
			assertThat(result.getContent()).hasSize(1)
				.extracting("enrollable")
				.containsExactly("N");
		}

		@DisplayName("강의 정원이 아직 다 차지 않은 강의여도 이미 신청한 강의는 강의 목록 조회시 강의 신청 가능 여부가 N으로 조회된다.")
		@Test
		void givenNotFullEnrolledCountLecturesAndEnrolledWhenGetEnrollableLecturesThanIsEnrollableIsY() throws Exception {
			// given
			final LocalDateTime startedAt = LocalDateTime.of(2024, 12, 20, 10, 00);
			final LocalDateTime endedAt = LocalDateTime.of(2024, 12, 21, 10, 00);

			final Long lectureId = 1L;
			final Long userId = 1L;
			final int capacity = lectureCapacityProvider.getCapacity(lectureId);

			final LectureSearchRequest lectureSearchRequest = new LectureSearchRequest(userId, startedAt, endedAt);
			final Pageable pageable = PageRequest.of(0, 20);

			given(lectureRepository.getEnrollableLectures(lectureSearchRequest.toRepositoryDto(), pageable))
				.willReturn(new SliceImpl<>(List.of(
					Lecture.builder()
						.id(lectureId)
						.enrolledCount(capacity - 1)
						.period(new Period(startedAt, endedAt))
						.build()
				)));

			given(enrolledLectureRepository.isEnrolledLecture(lectureId, userId))
				.willReturn(true);

			// when
			final var result = lectureService.getEnrollableLectures(lectureSearchRequest, pageable);

			// then
			assertThat(result.getContent()).hasSize(1)
				.extracting("enrollable")
				.containsExactly("N");
		}

		@DisplayName("강의 정원이 아직 다 차지 않은 강의는 유저 아이디가 주어지지 않은 상태로 강의 목록 조회시 강의 신청 가능 여부가 Y로 조회된다.")
		@Test
		void givenNotFullEnrolledCountLecturesWhenGetEnrollableLecturesThanIsEnrollableIsY() throws Exception {
			// given
			final LocalDateTime startedAt = LocalDateTime.of(2024, 12, 20, 10, 00);
			final LocalDateTime endedAt = LocalDateTime.of(2024, 12, 21, 10, 00);

			final Long lectureId = 1L;
			final Long userId = null;
			final int capacity = lectureCapacityProvider.getCapacity(lectureId);

			final LectureSearchRequest lectureSearchRequest = new LectureSearchRequest(userId, startedAt, endedAt);
			final Pageable pageable = PageRequest.of(0, 20);

			given(lectureRepository.getEnrollableLectures(lectureSearchRequest.toRepositoryDto(), pageable))
				.willReturn(new SliceImpl<>(List.of(
					Lecture.builder()
						.id(lectureId)
						.enrolledCount(capacity - 1)
						.period(new Period(startedAt, endedAt))
						.build()
				)));

			given(enrolledLectureRepository.isEnrolledLecture(lectureId, userId))
				.willReturn(false);

			// when
			final var result = lectureService.getEnrollableLectures(lectureSearchRequest, pageable);

			// then
			assertThat(result.getContent()).hasSize(1)
				.extracting("enrollable")
				.containsExactly("Y");
		}
	}

	@DisplayName("강의 신청")
	@Nested
	class EnrollLecture {

		@DisplayName("이미 신청한 강의를 신청하면 예외가 발생한다.")
		@Test
		void givenAlreadyEnrolledLectureWhenEnrollThrowsException() throws Exception {
			// given
			final Long userId = 1L;
			final Long lectureId = 1L;
			final EnrollLectureRequest request = new EnrollLectureRequest(userId, lectureId);

			final EnrolledLecture enrolledLecture = EnrolledLecture.builder()
				.lectureId(lectureId)
				.userId(userId)
				.build();

			given(enrolledLectureRepository.findByLectureIdAndUserId(lectureId, userId))
				.willReturn(Optional.of(enrolledLecture));

			// when & then
			assertThatThrownBy(() -> lectureService.enroll(request))
				.isInstanceOf(LectureException.class)
				.hasMessage(ALREADY_ENROLLED_LECTURE.getMessage());
		}

		@DisplayName("강의 신청 시 강의 정원이 꽉 찼으면 예외가 발생한다.")
		@Test
		void givenFullEnrolledCountLectureWhenEnrollThrowsException() throws Exception {
			// given
			final int capacity = 30;
			final Long userId = 1L;
			final Long lectureId = 1L;
			final EnrollLectureRequest request = new EnrollLectureRequest(userId, lectureId);

			given(enrolledLectureRepository.findByLectureIdAndUserId(lectureId, userId))
				.willReturn(Optional.empty());

			given(lectureCapacityProvider.getCapacity(lectureId))
				.willReturn(capacity);

			final Lecture lecture = Lecture.builder()
				.id(lectureId)
				.enrolledCount(capacity)
				.build();

			given(lectureRepository.getById(lectureId))
				.willReturn(lecture);

			// when & then
			assertThatThrownBy(() -> lectureService.enroll(request))
				.isInstanceOf(LectureException.class)
				.hasMessage(LECTURE_IS_FULL.getMessage());
		}
	}
}
