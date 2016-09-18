package org.wasd;

public class Swapper<T> {

    private T one;
    private T two;

    public Swapper(T one, T two) {
        this.one = one;
        this.two = two;
    }

    public T getOne() {
        return one;
    }

    public T getTwo() {
        return two;
    }

    public void swap() {
        T tmp = one;
        one = two;
        two = tmp;
    }
}
