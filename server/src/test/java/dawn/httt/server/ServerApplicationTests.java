package dawn.httt.server;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@EnableAutoConfiguration
@ActiveProfiles("test")
class ServerApplicationTests {

    @Test
    void contextLoads() {
    }

}
