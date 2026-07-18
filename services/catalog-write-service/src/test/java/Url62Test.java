import org.junit.jupiter.api.Test;
import org.ultra.rcrs.utils.Url62;

import java.util.UUID;

public class Url62Test {

    @Test
    void convert(){
        System.out.println(Url62.encode(UUID.fromString("0f54397e-fa6b-43cb-ab71-3147b6c4f55d")));
    }

}
