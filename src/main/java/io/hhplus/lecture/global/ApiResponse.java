package io.hhplus.lecture.global;

import lombok.Getter;

@Getter
public class ApiResponse<T> {

	private final int code;
	private final String message;
	private final T data;

	private ApiResponse(final int code, final String message, final T data) {
		this.code = code;
		this.message = message;
		this.data = data;
	}

	private static <T> ApiResponse<T> of(final int code, final String message, final T data) {
		return new ApiResponse<>(code, message, data);
	}

	public static <T> ApiResponse<T> success(final T data) {
		return of(200, "OK", data);
	}
}
