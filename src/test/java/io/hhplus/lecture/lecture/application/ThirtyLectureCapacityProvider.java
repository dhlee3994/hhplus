package io.hhplus.lecture.lecture.application;

public class ThirtyLectureCapacityProvider implements LectureCapacityProvider {

	@Override
	public int getCapacity(final Long lectureId) {
		return 30;
	}
}
