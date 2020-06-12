package HomeWork_1;

import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        Matrix<Integer> mtx = new Matrix<>(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
        mtx.ArrMtx(1, 9);
        Matrix<String> mtxT = new Matrix<>("a", "b", "c", "d", "e", "f", "g", "h", "j", "k");
        mtxT.ArrMtx(2, 8);

        MatrixOfArray<String> matrixOfArray= new MatrixOfArray<>();
        ArrayList<String> arr = matrixOfArray.converting(mtxT.getMtx());

        MatrixOfArray<Integer> matrixOArray= new MatrixOfArray<>();
        ArrayList<Integer> arrI = matrixOArray.converting(mtx.getMtx());

        Box<Apple> appleBox_1 = new Box<>();
        Box<Apple> appleBox_2 = new Box<>();
        Box<Orange> orangeBox_1 = new Box<>();
        Box<Orange> orangeBox_2 = new Box<>();

        appleBox_1.putFruit(new Apple(), 5);
        appleBox_2.putFruit(new Apple(), 9);
        orangeBox_1.putFruit(new Orange(), 13);
        orangeBox_2.putFruit(new Orange(), 6);

        orangeBox_2.compare(appleBox_2);
        appleBox_2.pourOverFruit(appleBox_1);
    }
}
