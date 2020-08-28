package HomeWork_6L3;

import java.util.Arrays;

public class ReturnedArray {
    public static int[] RetArray(int[] in) throws RuntimeException {
        int n = 0;
        if (Arrays.toString(in).contains("4")) {
            for (int i = 0; i < in.length; i++) {
                if (Arrays.toString(new int[]{in[i]}).contains("4"))
                    n = i;
            }
            n++;
            int[] out = new int[in.length - n];
            System.arraycopy(in, n, out, 0, out.length);
            return out;
        } else {
            throw new RuntimeException("This array does not contain any fours!");
        }
    }
}
