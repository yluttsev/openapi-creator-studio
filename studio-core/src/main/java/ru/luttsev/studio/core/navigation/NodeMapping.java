package ru.luttsev.studio.core.navigation;

@FunctionalInterface
interface NodeMapping<T> {

    void collect(T node, ChildrenCollector children);
}
