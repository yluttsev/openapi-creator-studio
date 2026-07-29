package ru.luttsev.studio.core.reference;

public sealed interface ReferenceResolution<T>
        permits ResolvedReference, UnresolvedReference {
}
