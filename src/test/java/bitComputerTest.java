import org.junit.jupiter.api.Test;

public class bitComputerTest {
    @Test
    public void test001() throws Exception {
        int baseIndex = 10;
        System.out.println(baseIndex >>> 3);
        System.out.println(baseIndex & (-8));
        System.out.println(baseIndex & 7);
    }
}
