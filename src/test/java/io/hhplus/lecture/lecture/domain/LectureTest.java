package io.hhplus.lecture.lecture.domain;

import static io.hhplus.lecture.global.exception.LectureErrorCode.LECTURE_IS_FULL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.hhplus.lecture.global.BaseTest;
import io.hhplus.lecture.global.exception.LectureException;

class LectureTest extends BaseTest {

	@DisplayName("현재 수강신청인원이 수강신청 가능 인원보다 같거나 많은 경우 수강신청시 예외가 발생한다.")
	@Test
	void givenEnrolledCountIsGreaterOrEqualThanCapacityWhenEnrollThrowsException() throws Exception {
		// given
		int capacity = 30;
		final Lecture lecture = Lecture.builder()
			.enrolledCount(capacity)
			.build();

		// when & then
		assertThatThrownBy(
			() -> lecture.enroll(capacity, 1L))
			.isInstanceOf(LectureException.class)
			.hasMessage(LECTURE_IS_FULL.getMessage());
	}

	@DisplayName("정상적으로 수강신청이 되면 수강신청 인원이 1 증가한다.")
	@Test
	void whenEnrollThenEnrolledCountPlusOne() throws Exception {
		// given
		int enrolledCount = 10;
		final Lecture lecture = Lecture.builder()
			.enrolledCount(enrolledCount)
			.build();
		
		int expectedEnrolledCount = enrolledCount + 1;

		// when
		lecture.enroll(30, 1L);

		// then
		assertThat(lecture.getEnrolledCount()).isEqualTo(expectedEnrolledCount);
	}

	@DisplayName("수강신청 한 인원이 수강 신청 가능 인원과 같은 경우 수강 신청가능 여부가 false이다.")
	@Test
	void givenEnrolledCountIsEqualToCapacityWhenIsEnrollableThenFalse() throws Exception {
		// given
		final int capacity = 30;
		final Lecture lecture = Lecture.builder()
			.enrolledCount(capacity)
			.build();

		// when
		final boolean result = lecture.isEnrollable(capacity);

		// then
		assertThat(result).isFalse();
	}

	@DisplayName("수강신청 한 인원이 수강 신청 가능 인원보다 많은 경우 수강 신청가능 여부가 false이다.")
	@Test
	void givenEnrolledCountIsGreaterThanCapacityWhenIsEnrollableThenFalse() throws Exception {
		// given
		final int capacity = 30;
		final Lecture lecture = Lecture.builder()
			.enrolledCount(capacity + 1)
			.build();

		// when
		final boolean result = lecture.isEnrollable(capacity);

		// then
		assertThat(result).isFalse();
	}

	@DisplayName("수강신청 한 인원이 수강 신청 가능 인원보다 적은 경우 수강 신청가능 여부가 true이다.")
	@Test
	void givenEnrolledCountIsLessThanCapacityWhenIsEnrollableThenFalse() throws Exception {
		// given
		final int capacity = 30;
		final Lecture lecture = Lecture.builder()
			.enrolledCount(capacity - 1)
			.build();

		// when
		final boolean result = lecture.isEnrollable(capacity);

		// then
		assertThat(result).isTrue();
	}

	@DisplayName("수강신청 한 인원과 정원이 같고, 이미 신청한 강의의 수강 신청 가능 여부는 N이다.")
	@Test
	void givenFullAndAlreadyEnrolledLectureWhenGetEnrollableReturnN() throws Exception {
		// given
		final boolean isEnrolled = true;
		final int capacity = 30;
		final Lecture lecture = Lecture.builder()
			.enrolledCount(capacity)
			.build();

		// when
		final String result = lecture.getEnrollable(capacity, isEnrolled);

		// then
		assertThat(result).isEqualTo("N");
	}

	@DisplayName("수강신청 한 인원이 정원보다 적고, 이미 신청한 강의의 수강 신청 가능 여부는 N이다.")
	@Test
	void givenNotFullAndAlreadyEnrolledLectureWhenGetEnrollableReturnN() throws Exception {
		// given
		final boolean isEnrolled = true;
		final int capacity = 30;
		final int enrolledCount = capacity - 1;
		final Lecture lecture = Lecture.builder()
			.enrolledCount(enrolledCount)
			.build();

		// when
		final String result = lecture.getEnrollable(capacity, isEnrolled);

		// then
		assertThat(result).isEqualTo("N");
	}

	@DisplayName("수강신청 한 인원이 정원보다 적고, 신청하지 않은 강의의 수강 신청 가능 여부는 Y이다.")
	@Test
	void givenFullAndNotEnrolledLectureWhenGetEnrollableReturnN() throws Exception {
		// given
		final boolean isEnrolled = false;
		final int capacity = 30;
		final int enrolledCount = capacity - 1;
		final Lecture lecture = Lecture.builder()
			.enrolledCount(enrolledCount)
			.build();

		// when
		final String result = lecture.getEnrollable(capacity, isEnrolled);

		// then
		assertThat(result).isEqualTo("Y");
	}
}
