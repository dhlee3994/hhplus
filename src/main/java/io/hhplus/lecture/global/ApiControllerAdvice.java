package io.hhplus.lecture.global;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.hhplus.lecture.global.exception.LectureException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class ApiControllerAdvice {

	@ExceptionHandler(LectureException.class)
	public ProblemDetail handleLectureException(final LectureException e) {
		log.error("handleLectureException:", e);

		final ProblemDetail problemDetail =
			ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());

		problemDetail.setProperty("code", e.getCode());
		return problemDetail;
	}

	@ExceptionHandler(Exception.class)
	public ProblemDetail handleException(final Exception e) {
		log.error("handleException:", e);
		return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
	}
}
