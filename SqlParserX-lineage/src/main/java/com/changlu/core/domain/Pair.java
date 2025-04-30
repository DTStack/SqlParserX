package com.changlu.core.domain;

import java.util.Objects;

/**
 * 通用的键值对容器，可存储任意两个类型的值
 * @param <K> 第一个值的类型
 * @param <V> 第二个值的类型
 */
public class Pair<K, V> {
    private final K first;
    private final V second;

    public Pair(K first, V second) {
        this.first = first;
        this.second = second;
    }

    public K getFirst() {
        return first;
    }

    public V getSecond() {
        return second;
    }

    @Override
    public String toString() {
        return "Pair{" +
                "first=" + first +
                ", second=" + second +
                '}';
    }

    // 可选：实现 equals 和 hashCode 方法，方便比较
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return Objects.equals(first, pair.first) &&
               Objects.equals(second, pair.second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }

    // 提供一个静态工厂方法，方便构造 Pair
    public static <K, V> Pair<K, V> of(K first, V second) {
        return new Pair<>(first, second);
    }
}