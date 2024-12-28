package io.hhplus.lecture.global.exception;

import lombok.Getter;

@Getter
public class LectureException extends RuntimeException {

	private final String code;

	public LectureException(final LectureErrorCode errorCode) {
		super(errorCode.getMessage());
		this.code = errorCode.getCode();
	}
}
