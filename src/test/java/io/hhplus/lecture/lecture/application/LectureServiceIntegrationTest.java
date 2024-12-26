package io.hhplus.lecture.lecture.application;

import static io.hhplus.lecture.global.exception.LectureErrorCode.ALREADY_ENROLLED_LECTURE;
import static io.hhplus.lecture.global.exception.LectureErrorCode.LECTURE_IS_FULL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import io.hhplus.lecture.config.TestConfig;
import io.hhplus.lecture.global.BaseTest;
import io.hhplus.lecture.global.DataCleaner;
import io.hhplus.lecture.global.exception.LectureException;
import io.hhplus.lecture.lecture.application.request.EnrollLectureRequest;
import io.hhplus.lecture.lecture.application.request.LectureSearchRequest;
import io.hhplus.lecture.lecture.application.response.EnrollLectureResponse;
import io.hhplus.lecture.lecture.domain.EnrolledLecture;
import io.hhplus.lecture.lecture.domain.Lecture;
import io.hhplus.lecture.lecture.domain.Period;
import io.hhplus.lecture.lecture.infrastructure.EnrolledJpaRepository;
import io.hhplus.lecture.lecture.infrastructure.LectureJpaRepository;

@Import(TestConfig.class)
@SpringBootTest
class LectureServiceIntegrationTest extends BaseTest {

	@Autowired
	private LectureService lectureService;

	@Autowired
	private LectureJpaRepository lectureJpaRepository;

	@Autowired
	private EnrolledJpaRepository enrolledJpaRepository;

	@Autowired
	private LectureCapacityProvider lectureCapacityProvider;

	@Autowired
	private DataCleaner dataCleaner;

	@BeforeEach
	void setUp() {
		dataCleaner.cleaning();
	}

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
			lectureJpaRepository.saveAll(List.of(
				Lecture.builder().title("lectureA").lecturerName("lecturerNameA").period(new Period(startedAt, endedAt)).build(),
				Lecture.builder().title("lectureB").lecturerName("lecturerNameB").period(new Period(startedAt.plusSeconds(1), endedAt)).build(),
				Lecture.builder().title("lectureC").lecturerName("lecturerNameC").period(new Period(startedAt, endedAt.minusSeconds(1))).build(),
				Lecture.builder().title("lectureD").lecturerName("lecturerNameD").period(new Period(startedAt.minusSeconds(1), endedAt)).build(),
				Lecture.builder().title("lectureE").lecturerName("lecturerNameE").period(new Period(startedAt, endedAt.plusSeconds(1))).build(),
				Lecture.builder().title("lectureF").lecturerName("lecturerNameF").period(new Period(startedAt.minusSeconds(2), startedAt.minusSeconds(1))).build(),
				Lecture.builder().title("lectureG").lecturerName("lecturerNameG").period(new Period(endedAt.plusSeconds(1), endedAt.plusSeconds(2))).build()
			));
			// @formatter:on

			// when
			final var result = lectureService.getEnrollableLectures(lectureSearchRequest, pageable);

			// then
			assertThat(result.getContent()).hasSize(5)
				.noneMatch(lectureResponse ->
					lectureResponse.applicationStartedAt().isBefore(startedAt) &&
						lectureResponse.applicationEndedAt().isAfter(endedAt)
				)
				.extracting("title", "lecturerName")
				.containsExactly(
					tuple("lectureA", "lecturerNameA"),
					tuple("lectureB", "lecturerNameB"),
					tuple("lectureC", "lecturerNameC"),
					tuple("lectureD", "lecturerNameD"),
					tuple("lectureE", "lecturerNameE")
				);
		}

		@DisplayName("강의 정원이 꽉 찬 강의는 강의 목록 조회시 강의 신청 가능 여부가 N으로 조회된다.")
		@Test
		void givenFullEnrolledCountLecturesWhenGetEnrollableLecturesThanIsEnrollableIsN() throws Exception {
			// given
			final String title = "lectureA";
			final String lecturerName = "lecturerNameA";
			final LocalDateTime startedAt = LocalDateTime.of(2024, 12, 20, 10, 0);
			final LocalDateTime endedAt = LocalDateTime.of(2024, 12, 21, 10, 0);
			final Lecture lecture = Lecture.builder()
				.title(title)
				.lecturerName(lecturerName)
				.period(new Period(startedAt, endedAt))
				.build();

			final Lecture savedLecture = lectureJpaRepository.save(lecture);

			final int capacity = lectureCapacityProvider.getCapacity(savedLecture.getId());
			final Long userId = 1L;
			for (int i = 1; i <= capacity; i++) {
				lectureService.enroll(new EnrollLectureRequest(userId + i, savedLecture.getId()));
			}

			final LectureSearchRequest lectureSearchRequest = new LectureSearchRequest(null, startedAt, endedAt);
			final Pageable pageable = PageRequest.of(0, 20);

			// when
			final var result = lectureService.getEnrollableLectures(lectureSearchRequest, pageable);

			// then
			assertThat(result.getContent()).hasSize(1)
				.extracting("enrollable")
				.containsExactly("N");
		}

		@DisplayName("이미 신청한 강의 조회시  강의 정원이 꽉 차지 않아도 강의 목록 조회시 강의 신청 가능 여부가 N으로 조회된다.")
		@Test
		void givenEnrolledLectureWhenGetEnrollableLecturesThanIsEnrollableIsN() throws Exception {
			// given
			final LocalDateTime startedAt = LocalDateTime.of(2024, 12, 20, 10, 0);
			final LocalDateTime endedAt = LocalDateTime.of(2024, 12, 21, 10, 0);
			final Lecture lecture = Lecture.builder()
				.title("lectureA")
				.lecturerName("lecturerNameA")
				.period(new Period(startedAt, endedAt))
				.build();

			final Lecture savedLecture = lectureJpaRepository.save(lecture);

			final Long userId = 1L;
			lectureService.enroll(new EnrollLectureRequest(userId, savedLecture.getId()));

			final LectureSearchRequest lectureSearchRequest = new LectureSearchRequest(userId, startedAt, endedAt);
			final Pageable pageable = PageRequest.of(0, 20);

			// when
			final var result = lectureService.getEnrollableLectures(lectureSearchRequest, pageable);

			// then
			assertThat(result.getContent()).hasSize(1)
				.extracting("enrollable")
				.containsExactly("N");

			assertThat(result.getContent().get(0).enrolledCount())
				.isLessThan(lectureCapacityProvider.getCapacity(savedLecture.getId()));
		}

		@DisplayName("신청한 적이 없고 강의 정원이 아직 다 차지 않은 강의는 강의 목록 조회시 강의 신청 가능 여부가 Y로 조회된다.")
		@Test
		void givenNotFullEnrolledCountLecturesWhenGetEnrollableLecturesThanIsEnrollableIsY() throws Exception {
			// given
			final String title = "lectureA";
			final String lecturerName = "lecturerNameA";
			final LocalDateTime startedAt = LocalDateTime.of(2024, 12, 20, 10, 0);
			final LocalDateTime endedAt = LocalDateTime.of(2024, 12, 21, 10, 0);
			final Lecture lecture = Lecture.builder()
				.title(title)
				.lecturerName(lecturerName)
				.period(new Period(startedAt, endedAt))
				.build();

			final Lecture savedLecture = lectureJpaRepository.save(lecture);

			final int capacity = lectureCapacityProvider.getCapacity(savedLecture.getId());
			final Long userId = 1L;
			for (int i = 1; i <= (capacity - 1); i++) {
				lectureService.enroll(new EnrollLectureRequest(userId + i, savedLecture.getId()));
			}

			final LectureSearchRequest lectureSearchRequest = new LectureSearchRequest(null, startedAt, endedAt);
			final Pageable pageable = PageRequest.of(0, 20);

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
			final String title = "titleA";
			final String lecturerName = "lecturerNameA";
			final LocalDateTime startedAt = LocalDateTime.of(2024, 12, 20, 10, 0);
			final LocalDateTime endedAt = LocalDateTime.of(2024, 12, 21, 10, 0);
			final Lecture savedLecture = lectureJpaRepository.save(Lecture.builder()
				.title(title)
				.lecturerName(lecturerName)
				.period(new Period(startedAt, endedAt))
				.build());

			enrolledJpaRepository.save(
				EnrolledLecture.builder().lectureId(savedLecture.getId()).userId(userId).build());

			final EnrollLectureRequest request = new EnrollLectureRequest(userId, savedLecture.getId());

			// when & then
			assertThatThrownBy(() -> lectureService.enroll(request))
				.isInstanceOf(LectureException.class)
				.hasMessage(ALREADY_ENROLLED_LECTURE.getMessage());
		}

		@DisplayName("강의 신청 시 강의 정원이 꽉 찼으면 예외가 발생한다.")
		@Test
		void givenFullEnrolledCountLectureWhenEnrollThrowsException() throws Exception {
			// given
			final String title = "titleA";
			final String lecturerName = "lecturerNameA";
			final LocalDateTime startedAt = LocalDateTime.of(2024, 12, 20, 10, 0);
			final LocalDateTime endedAt = LocalDateTime.of(2024, 12, 21, 10, 0);

			final Lecture savedLecture = lectureJpaRepository.save(Lecture.builder()
				.title(title)
				.lecturerName(lecturerName)
				.period(new Period(startedAt, endedAt))
				.build());

			final int capacity = lectureCapacityProvider.getCapacity(savedLecture.getId());
			final Long userId = 1L;
			for (int i = 1; i <= capacity; i++) {
				lectureService.enroll(new EnrollLectureRequest(userId + i, savedLecture.getId()));
			}

			final EnrollLectureRequest request = new EnrollLectureRequest(userId, savedLecture.getId());

			// when & then
			assertThatThrownBy(() -> lectureService.enroll(request))
				.isInstanceOf(LectureException.class)
				.hasMessage(LECTURE_IS_FULL.getMessage());
		}

		@DisplayName("정원이 꽉 차지 않았고 신청한 이력이 없는 강의를 신청하면 강의 신청이 성공한다.")
		@Test
		void givenNotFullEnrolledCountAndNotEnrolledLectureWhenEnrollThenSuccess() throws Exception {
			// given
			final String title = "titleA";
			final String lecturerName = "lecturerNameA";
			final LocalDateTime startedAt = LocalDateTime.of(2024, 12, 20, 10, 0);
			final LocalDateTime endedAt = LocalDateTime.of(2024, 12, 21, 10, 0);

			final Lecture savedLecture = lectureJpaRepository.save(Lecture.builder()
				.title(title)
				.lecturerName(lecturerName)
				.period(new Period(startedAt, endedAt))
				.build());

			final int capacity = lectureCapacityProvider.getCapacity(savedLecture.getId());
			final Long userId = 1L;
			for (int i = 1; i <= capacity - 1; i++) {
				lectureService.enroll(new EnrollLectureRequest(userId + i, savedLecture.getId()));
			}

			final EnrollLectureRequest request = new EnrollLectureRequest(userId, savedLecture.getId());

			// when
			final EnrollLectureResponse result = lectureService.enroll(request);

			// then
			assertThat(result).isNotNull()
				.extracting("lectureId", "title", "lecturerName", "currentEnrolledCount")
				.containsExactly(savedLecture.getId(), title, lecturerName, capacity);
		}
	}
}
