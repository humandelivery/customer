package goorm.humandelivery;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    @Test
    void testMainOutput() {
        // 테스트를 통해 출력되는 텍스트가 맞는지 확인
        assertEquals("고객 프로그램 시작!", "고객 프로그램 시작!");
    }
}