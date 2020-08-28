import HomeWork_6L3.ArrayOfNumbers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.assertEquals;

@RunWith(Parameterized.class)
public class TestArrayOfNumbers {
    private boolean result;
    private int[] in;

    public TestArrayOfNumbers(int[] in, boolean result) {
        this.in = in;
        this.result = result;
    }

    @Parameterized.Parameters
    public static Iterable<Object[]> dataForTest() {
        return Arrays.asList(new Object[][]{
                {new int[]{1, 1, 1, 4, 1, 4, 4}, true},
                {new int[]{1, 1, 1, 1, 1, 1, 4}, true},
                {new int[]{4, 4, 4, 1, 4, 4, 4}, true},
                {new int[]{1, 1, 1, 1, 1, 1, 1}, false},
                {new int[]{4, 4, 4, 4, 4, 4, 4}, false},
                {new int[]{1, 1, 1, 2, 1, 4, 4}, false},
        });
    }

    @Test
    public void massTestAdd() {
        assertEquals(result, ArrayOfNumbers.arrayNum(in));
    }

}
