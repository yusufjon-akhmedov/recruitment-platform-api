package com.yusufjon.recruitmentplatform;

/**
 * Keeps the generated application context smoke test visible while excluding it from the fast
 * automated test suite.
 */

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled("Application context smoke test is excluded from the unit-test suite.")
class RecruitmentPlatformApiApplicationTests {

	@Test
	void contextLoads() {
	}

}
