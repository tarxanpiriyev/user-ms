package az.user_ms;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class HashTest {
    @Test
    void genHash() {
        System.out.println("HASH_START:" + new BCryptPasswordEncoder().encode("user123") + ":HASH_END");
    }
}
