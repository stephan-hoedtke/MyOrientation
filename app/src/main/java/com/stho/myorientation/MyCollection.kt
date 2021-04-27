package com.stho.myorientation

import kotlin.collections.ArrayList

/**
 * A class the keeps a collection of items, that can be iterated through, and modified at the same time
 *
 * Motivation:
 *      avoid ConcurrentModificationException
 *      when iterating (foreach, removeIf, ...) while items are added simultaneously
 */
class MyCollection<T> {

    private val array: ArrayList<T> = ArrayList()
    private val lock: Any = Any()

    /**
     * append a new element
     */
    fun add(element: T) {
        synchronized(lock) {
            array.add(element)
        }
    }

    /**
     * remove all elements which satisfy the filter condition: filter(T) == true
     */
    fun removeIf(filter: (T) -> Boolean) {
        synchronized(lock) {
            array.removeIf(filter)
        }
    }

    /**
     * remove all elements
     */
    fun clear() {
        synchronized(lock) {
            array.clear()
        }
    }

    /**
     * return a collection of all items which may be used with foreach ...
     */
    fun elements(): Iterable<T> {
        synchronized(lock) {
            return array.toList()
        }
    }
}
