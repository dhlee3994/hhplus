package io.hhplus.lecture.lecture.application;

import static io.hhplus.lecture.global.exception.LectureErrorCode.ALREADY_ENROLLED_LECTURE;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.hhplus.lecture.global.exception.LectureException;
import io.hhplus.lecture.lecture.application.request.EnrollLectureRequest;
import io.hhplus.lecture.lecture.application.request.LectureSearchRequest;
import io.hhplus.lecture.lecture.application.response.EnrollLectureResponse;
import io.hhplus.lecture.lecture.application.response.LectureSearchResponse;
import io.hhplus.lecture.lecture.domain.EnrolledLecture;
import io.hhplus.lecture.lecture.domain.EnrolledLectureRepository;
import io.hhplus.lecture.lecture.domain.Lecture;
import io.hhplus.lecture.lecture.domain.LectureRepository;
import io.hhplus.lecture.lecture.infrastructure.response.EnrollLectureResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class LectureService {

	private final LectureRepository lectureRepository;
	private final EnrolledLectureRepository enrolledLectureRepository;
	private final LectureCapacityProvider lectureCapacityProvider;

	public Slice<LectureSearchResponse> getEnrollableLectures(
		final LectureSearchRequest request,
		final Pageable pageable
	) {
		final Slice<Lecture> enrollableLectures =
			lectureRepository.getEnrollableLectures(request.toRepositoryDto(), pageable);

		final List<LectureSearchResponse> responses = enrollableLectures.getContent()
			.stream()
			.map(lecture -> {
				final int capacity = lectureCapacityProvider.getCapacity(lecture.getId());

				final boolean isEnrolledLecture = enrolledLectureRepository.isEnrolledLecture(lecture.getId(), request.userId());
				final String enrollable = lecture.getEnrollable(capacity, isEnrolledLecture);

				return LectureSearchResponse.of(lecture, capacity, enrollable);
			})
			.toList();

		return new SliceImpl<>(responses, pageable, enrollableLectures.hasNext());
	}

	@Transactional
	public EnrollLectureResponse enroll(final EnrollLectureRequest request) {

		final Lecture lecture = lectureRepository.getById(request.lectureId());

		enrolledLectureRepository.findByLectureIdAndUserId(request.lectureId(), request.userId())
			.ifPresent(enrolledLecture -> {
				throw new LectureException(ALREADY_ENROLLED_LECTURE);
			});

		final int capacity = lectureCapacityProvider.getCapacity(request.lectureId());
		final EnrolledLecture enrolledLecture = lecture.enroll(capacity, request.userId());

		final EnrolledLecture savedEnrolledLecture = enrolledLectureRepository.save(enrolledLecture);

		return EnrollLectureResponse.of(lecture, savedEnrolledLecture);
	}

	public List<EnrollLectureResponse> getEnrolledLectures(final Long userId) {
		final List<EnrollLectureResult> enrolledLectures = enrolledLectureRepository.findAllByUserId(userId);
		return enrolledLectures.stream()
			.map(EnrollLectureResponse::of)
			.toList();
	}
}
