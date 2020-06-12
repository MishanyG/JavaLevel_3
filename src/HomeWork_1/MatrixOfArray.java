package HomeWork_1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class MatrixOfArray<T> {

    public ArrayList<T> converting(T[] m) {
        return (ArrayList<T>)Arrays.stream(m).collect(Collectors.toList());
    }
}
