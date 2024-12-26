package io.hhplus.lecture.lecture.application;

import static io.hhplus.lecture.global.exception.LectureErrorCode.ALREADY_ENROLLED_LECTURE;
import static io.hhplus.lecture.global.exception.LectureErrorCode.LECTURE_IS_FULL;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import io.hhplus.lecture.config.TestConfig;
import io.hhplus.lecture.global.BaseTest;
import io.hhplus.lecture.global.DataCleaner;
import io.hhplus.lecture.lecture.application.request.EnrollLectureRequest;
import io.hhplus.lecture.lecture.domain.Lecture;
import io.hhplus.lecture.lecture.domain.Period;
import io.hhplus.lecture.lecture.infrastructure.LectureJpaRepository;

@Import(TestConfig.class)
@SpringBootTest
class LectureServiceConcurrencyTest extends BaseTest {

	@Autowired
	private LectureService lectureService;

	@Autowired
	private LectureJpaRepository lectureJpaRepository;

	@Autowired
	private DataCleaner dataCleaner;

	@BeforeEach
	void setUp() {
		dataCleaner.cleaning();
	}

	@DisplayName("40명이 동시에 동일한 강의에 신청하면 30명만 강의신청에 성공하고 10명은 실패한다.")
	@Test
	void oneLecture40UserEnroll() throws Exception {
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

		final List<CompletableFuture<Boolean>> tasks = new ArrayList<>();
		final AtomicInteger exceptionCount = new AtomicInteger(0);

		final int enrollTryCount = 40;

		// when
		for (int i = 1; i <= enrollTryCount; i++) {
			final long userId = i;
			tasks.add(CompletableFuture.supplyAsync(() -> {
				lectureService.enroll(new EnrollLectureRequest(userId, savedLecture.getId()));
				return true;
			}).exceptionally(e -> {
				if (e.getMessage().contains(LECTURE_IS_FULL.getMessage())) {
					exceptionCount.incrementAndGet();
				}
				return false;
			}));
		}

		// then
		CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0])).join();

		int successCount = 0;
		int failureCount = 0;
		for (CompletableFuture<Boolean> task : tasks) {
			if (task.get()) {
				successCount++;
			} else {
				failureCount++;
			}
		}

		assertThat(exceptionCount.get()).isEqualTo(10);
		assertThat(successCount).isEqualTo(30);
		assertThat(failureCount).isEqualTo(exceptionCount.get());
	}


	@DisplayName("동일한 유저가 동일한 강의를 동시에 5번 신청하면 1번의 요청만 성공하고 나머지는 실패한다.")
	@Test
	void oneLectureOneUserEnroll() throws Exception {
		// given
		final Long userId = 1L;
		final LocalDateTime startedAt = LocalDateTime.of(2024, 12, 20, 10, 0);
		final LocalDateTime endedAt = LocalDateTime.of(2024, 12, 21, 10, 0);
		final Lecture savedLecture = lectureJpaRepository.save(Lecture.builder()
			.title("lectureA")
			.lecturerName("lecturerA")
			.period(new Period(startedAt, endedAt))
			.build());

		final List<CompletableFuture<Boolean>> tasks = new ArrayList<>();
		final AtomicInteger exceptionCount = new AtomicInteger(0);

		final int enrollTryCount = 5;

		// when
		for (int i = 0; i < enrollTryCount; i++) {
			tasks.add(CompletableFuture.supplyAsync(() -> {
				lectureService.enroll(new EnrollLectureRequest(userId, savedLecture.getId()));
				return true;
			}).exceptionally(e -> {
				if (e.getMessage().contains(ALREADY_ENROLLED_LECTURE.getMessage())) {
					exceptionCount.incrementAndGet();
				}
				return false;
			}));
		}

		// then
		CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0])).join();

		int successCount = 0;
		int failureCount = 0;
		for (CompletableFuture<Boolean> task : tasks) {
			if (task.get()) {
				successCount++;
			} else {
				failureCount++;
			}
		}

		assertThat(exceptionCount.get()).isEqualTo(4);
		assertThat(successCount).isEqualTo(1);
		assertThat(failureCount).isEqualTo(exceptionCount.get());
	}
}
