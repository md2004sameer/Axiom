package com.Axiom;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = "security.jwt.secret=test-jwt-secret-with-at-least-32-bytes")
class AxiomApplicationTests {

	@Test
	void contextLoads() {
	}

}
