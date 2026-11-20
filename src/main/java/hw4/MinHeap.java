package hw4;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Objects;

/** A binary min-heap ordered by the natural ordering of its elements. */
public class MinHeap<E extends Comparable<? super E>> extends AbstractCollection<E> {
    private final ArrayList<E> elements = new ArrayList<>();
    private int modificationCount;

    @Override
    public boolean add(E element) {
        elements.add(Objects.requireNonNull(element));
        modificationCount++;
        siftUp(elements.size() - 1, element);
        return true;
    }

    /** Removes and returns the smallest element, or returns null if empty. */
    public E get() {
        return elements.isEmpty() ? null : removeAt(0);
    }

    @Override
    public int size() {
        return elements.size();
    }

    @Override
    public boolean contains(Object element) {
        return elements.contains(element);
    }

    @Override
    public boolean remove(Object element) {
        int index = elements.indexOf(element);
        if (index < 0) {
            return false;
        }
        removeAt(index);
        return true;
    }

    @Override
    public void clear() {
        if (!elements.isEmpty()) {
            elements.clear();
            modificationCount++;
        }
    }

    @Override
    public Iterator<E> iterator() {
        // Heap repairs can move elements across the iterator's position.
        Iterator<E> snapshot = new ArrayList<>(elements).iterator();
        return new Iterator<>() {
            private int expectedModificationCount = modificationCount;
            private E lastReturned;

            @Override
            public boolean hasNext() {
                return snapshot.hasNext();
            }

            @Override
            public E next() {
                checkForModification();
                lastReturned = snapshot.next();
                return lastReturned;
            }

            @Override
            public void remove() {
                checkForModification();
                if (lastReturned == null) {
                    throw new IllegalStateException();
                }

                int index = 0;
                while (elements.get(index) != lastReturned) {
                    index++;
                }
                removeAt(index);
                expectedModificationCount = modificationCount;
                lastReturned = null;
            }

            private void checkForModification() {
                if (expectedModificationCount != modificationCount) {
                    throw new ConcurrentModificationException();
                }
            }
        };
    }

    private E removeAt(int index) {
        E removed = elements.get(index);
        E last = elements.removeLast();
        modificationCount++;

        if (index < elements.size()) {
            if (index > 0 && last.compareTo(elements.get(parentOf(index))) < 0) {
                siftUp(index, last);
            } else {
                siftDown(index, last);
            }
        }
        return removed;
    }

    private void siftUp(int index, E element) {
        while (index > 0) {
            int parent = parentOf(index);
            E parentElement = elements.get(parent);
            if (element.compareTo(parentElement) >= 0) {
                break;
            }
            elements.set(index, parentElement);
            index = parent;
        }
        elements.set(index, element);
    }

    private void siftDown(int index, E element) {
        int size = elements.size();
        while (index < size / 2) {
            int child = 2 * index + 1;
            int right = child + 1;
            if (right < size && elements.get(right).compareTo(elements.get(child)) < 0) {
                child = right;
            }

            E childElement = elements.get(child);
            if (element.compareTo(childElement) <= 0) {
                break;
            }
            elements.set(index, childElement);
            index = child;
        }
        elements.set(index, element);
    }

    private int parentOf(int index) {
        return (index - 1) / 2;
    }
}
