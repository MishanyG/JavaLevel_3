package HomeWork_1;

public class Matrix<T> {
    private T[] mtx;
    private int in_1 = 0;
    private int in_2 = 0;

    @SafeVarargs
    public Matrix(T ... mtx) {
        this.mtx = mtx;
    }

    public T[] ArrMtx(int in_1, int in_2) {
        T tmp = mtx[in_1];
        mtx[in_1] = mtx[in_2];
        mtx[in_2] = tmp;
        return mtx;
    }

    public T[] getMtx() {
        return mtx;
    }
}
