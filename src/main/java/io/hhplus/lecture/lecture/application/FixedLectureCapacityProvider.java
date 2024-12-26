package io.hhplus.lecture.lecture.application;

import org.springframework.stereotype.Component;

@Component
public class FixedLectureCapacityProvider implements LectureCapacityProvider {

	private static final int MAX_CAPACITY = 30;

	@Override
	public int getCapacity(final Long lectureId) {
		return MAX_CAPACITY;
	}
}
