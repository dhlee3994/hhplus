package io.hhplus.lecture.lecture.presentation;

import static org.springframework.data.domain.Sort.Direction.ASC;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.hhplus.lecture.global.ApiResponse;
import io.hhplus.lecture.global.exception.LectureErrorCode;
import io.hhplus.lecture.global.exception.LectureException;
import io.hhplus.lecture.lecture.application.LectureService;
import io.hhplus.lecture.lecture.presentation.request.EnrollLectureApiRequest;
import io.hhplus.lecture.lecture.presentation.request.LectureSearchApiRequest;
import io.hhplus.lecture.lecture.presentation.response.EnrolledLectureApiResponse;
import io.hhplus.lecture.lecture.presentation.response.LectureSearchApiResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RequestMapping("/api/v1/lectures")
@RestController
public class LectureApiController {

	private final LectureService lectureService;

	@GetMapping("/enrollable")
	public ApiResponse<Slice<LectureSearchApiResponse>> getEnrollableLectures(
		@RequestParam(required = false) Long userId,
		@RequestParam(required = false) LocalDateTime applicationStartedAt,
		@RequestParam(required = false) LocalDateTime applicationEndedAt,
		@PageableDefault(sort = "period.startedAt", direction = ASC) final Pageable pageable
	) {
		final var searchRequest = new LectureSearchApiRequest(userId, applicationStartedAt, applicationEndedAt);
		final var enrollableLectures = lectureService.getEnrollableLectures(searchRequest.toServiceDto(), pageable);
		return ApiResponse.success(enrollableLectures.map(LectureSearchApiResponse::of));
	}

	@PostMapping("/enroll")
	public ApiResponse<EnrolledLectureApiResponse> enroll(@RequestBody EnrollLectureApiRequest request) {
		return ApiResponse.success(EnrolledLectureApiResponse.of(lectureService.enroll(request.toServiceDto())));
	}

	@GetMapping("/enroll")
	public ApiResponse<List<EnrolledLectureApiResponse>> getEnrolledLectures(@RequestParam Long userId) {
		if (userId < 1) {
			throw new LectureException(LectureErrorCode.INVALID_USER_ID);
		}

		return ApiResponse.success(lectureService.getEnrolledLectures(userId)
			.stream()
			.map(EnrolledLectureApiResponse::of)
			.toList());
	}
}
