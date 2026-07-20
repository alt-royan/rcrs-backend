import org.junit.jupiter.api.Test;
import org.ultra.rcrs.utils.Url62;

import java.util.UUID;

public class Url62Test {

    @Test
    void convert(){
        System.out.println(Url62.encode(UUID.fromString("8428f981-c55d-47fc-8f80-e68daa311dcb")));
    }

}
