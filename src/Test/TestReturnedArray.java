import HomeWork_6L3.ReturnedArray;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

@RunWith(Parameterized.class)
public class TestReturnedArray {
    private int[] in;
    private int[] exp;

    public TestReturnedArray(int[] in, int[] exp) {
        this.in = in;
        this.exp = exp;
    }

    @Parameterized.Parameters
    public static Iterable<Object[]> dataForTest() {
        return Arrays.asList(new Object[][]{
                {new int[]{5, 4, 3, 2, 1, 1, 4, 8, 3, 5, 11}, new int[]{8, 3, 5, 11}},
                {new int[]{1, 2, 5, 4, 9, 44, 15, 2, 5}, new int[]{15, 2, 5}},
                {new int[]{8, 5, 2, 4, 3, 2}, new int[]{3, 2}}
        });
    }

    @Test
    public void massTestArray() {
        assertArrayEquals(exp, ReturnedArray.RetArray(in));
    }
}
