package ru.luttsev.studio.core.model.path;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.RandomAccess;

final class PresenceAwareList<E> extends AbstractList<E>
        implements RandomAccess {

    private final ArrayList<E> values = new ArrayList<>();
    private final Runnable mutationListener;

    PresenceAwareList(Runnable mutationListener) {
        this.mutationListener = Objects.requireNonNull(
                mutationListener,
                "mutationListener must not be null");
    }

    @Override
    public E get(int index) {
        return values.get(index);
    }

    @Override
    public int size() {
        return values.size();
    }

    @Override
    public E set(int index, E element) {
        E previous = values.set(index, element);
        mutationListener.run();
        return previous;
    }

    @Override
    public void add(int index, E element) {
        values.add(index, element);
        modCount++;
        mutationListener.run();
    }

    @Override
    public E remove(int index) {
        E removed = values.remove(index);
        modCount++;
        mutationListener.run();
        return removed;
    }

    @Override
    protected void removeRange(int fromIndex, int toIndex) {
        values.subList(fromIndex, toIndex).clear();
        modCount++;
        mutationListener.run();
    }

    void replaceContents(Collection<? extends E> source) {
        ArrayList<E> copiedValues = new ArrayList<>(source);
        values.clear();
        values.addAll(copiedValues);
        modCount++;
        mutationListener.run();
    }

    void clearSilently() {
        values.clear();
        modCount++;
    }
}
