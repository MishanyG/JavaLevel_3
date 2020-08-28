package HomeWork_6L3;

import java.util.Arrays;

public class ArrayOfNumbers {
    public static boolean arrayNum(int[] in) {
        if (Arrays.toString(in).contains("1") && Arrays.toString(in).contains("4")) {
            for (int value : in)
                if (!Arrays.toString(new int[]{value}).contains("1") && !Arrays.toString(new int[]{value}).contains("4"))
                    return false;
        }
        else return false;
        return true;
    }
}
