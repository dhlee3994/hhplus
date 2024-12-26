package io.hhplus.lecture.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LectureErrorCode {

	// 100
	INVALID_LECTURE_ID("L101", "유효하지 않은 강의입니다."),
	// 200
	INVALID_LECTURE_PERIOD("L201", "유효하지 않은 기간입니다."),
	INVALID_SEARCH_PERIOD("L202", "유효하지 않은 조회 날짜 입니다."),

	// 300
	LECTURE_IS_FULL("L301", "수강 인원이 가득 찼습니다."),
	ALREADY_ENROLLED_LECTURE("L302", "이미 신청된 특강입니다."),
	;

	private final String code;
	private final String message;
}
