package io.hhplus.tdd.point;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PointController.class)
class PointControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private PointService pointService;

	@DisplayName("양수가 아닌 포인트를 충전하려고 하면 400을 반환한다.")
	@Test
	void chargeZeroPoint() throws Exception {
		mockMvc.perform(patch("/point/{id}/charge", 1L)
				.contentType(MediaType.APPLICATION_JSON)
				.content("0"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("400"))
			.andExpect(jsonPath("$.message").value("충전 포인트는 양수여야합니다."))
			.andDo(print());
	}

	@DisplayName("양수가 아닌 포인트를 충전하려고 하면 400을 반환한다.")
	@Test
	void useZeroPoint() throws Exception {
		mockMvc.perform(patch("/point/{id}/use", 1L)
				.contentType(MediaType.APPLICATION_JSON)
				.content("0"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("400"))
			.andExpect(jsonPath("$.message").value("사용 포인트는 양수여야합니다."))
			.andDo(print());
	}
}
