import HomeWork_6L3.ReturnedArray;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class ExceptionTestRetArray {
    private int[] in;

    public ExceptionTestRetArray(int[] in) {
        this.in = in;
    }

    @Parameterized.Parameters
    public static int[][][] dataForTest() {
        return new int[][][] {
                {new int[]{8, 8, 18, 2, 5}},
                {new int[]{1, 2, 5, 3, 7}},
                {new int[]{12, 18, 93, 1}},
                {new int[]{11, 58, 17}}
        };
    }

    @Test(expected = RuntimeException.class)
    public void massTestArrayExc() {
        ReturnedArray.RetArray(in);
    }
}
