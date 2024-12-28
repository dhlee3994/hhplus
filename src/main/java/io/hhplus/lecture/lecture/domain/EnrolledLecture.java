package io.hhplus.lecture.lecture.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import io.hhplus.lecture.global.BaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AttributeOverrides({
	@AttributeOverride(name = "createdAt", column = @Column(name = "enrolled_at"))
})
@Table(name = "enrolled_lecture")
@Entity
public class EnrolledLecture extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Long lectureId;

	@Column(nullable = false)
	private Long userId;

	@Builder
	private EnrolledLecture(final Long lectureId, final Long userId) {
		this.lectureId = lectureId;
		this.userId = userId;
	}
}
