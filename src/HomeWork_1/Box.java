package HomeWork_1;

import java.util.ArrayList;
import java.util.stream.IntStream;

public class Box<T extends Fruit> {
    private ArrayList<T> box = new ArrayList<>();

    public float getWeight() {
        float weight = 0.0f;
        for (T fruit : box)
            weight += fruit.getWeight();

        return weight;
    }

    public void putFruit(T fruit, int quantity) {
        IntStream.range(0, quantity).forEach(i -> box.add(fruit));
    }

    public boolean compare(Box comparedBox) {
        return getWeight() == comparedBox.getWeight();
    }

    public void pourOverFruit(Box <T> whereToPour) {
        whereToPour.box.addAll(box);
        box.clear();
    }
}
