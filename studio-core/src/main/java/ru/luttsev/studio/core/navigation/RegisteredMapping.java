package ru.luttsev.studio.core.navigation;

@FunctionalInterface
interface RegisteredMapping {

    void collect(Object node, ChildrenCollector children);
}
