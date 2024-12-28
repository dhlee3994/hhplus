package io.hhplus.lecture.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import io.hhplus.lecture.lecture.application.LectureCapacityProvider;
import io.hhplus.lecture.lecture.application.ThirtyLectureCapacityProvider;

@TestConfiguration
public class TestConfig {

	@Bean
	public LectureCapacityProvider lectureCapacityProvider() {
		return new ThirtyLectureCapacityProvider();
	}
}
