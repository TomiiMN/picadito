package com.tomasmendez.picadito;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PicaditoApplicationTests {

	@Test
	void contextLoads() {
	}

    @Test
    void ciShouldFail() {
        org.junit.jupiter.api.Assertions.fail("Testing that CI blocks the merge");
    }

}
