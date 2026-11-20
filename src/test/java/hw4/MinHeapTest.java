package hw4;

import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MinHeapTest {
    @Test
    void returnsNullWhenEmptyAndCanBeReused() {
        MinHeap<Integer> heap = new MinHeap<>();
        assertNull(heap.get());

        heap.add(4);
        assertEquals(4, heap.get());
        assertTrue(heap.isEmpty());
        assertNull(heap.get());

        heap.add(2);
        assertEquals(2, heap.get());
    }

    @Test
    void removesMinimaInOrderIncludingDuplicates() {
        MinHeap<Integer> heap = new MinHeap<>();
        Collection<Integer> collection = heap;
        assertTrue(collection.addAll(List.of(8, 3, 5, 3, -1)));
        assertEquals(-1, heap.get());
        assertEquals(4, collection.size());
        assertFalse(collection.contains(-1));

        assertTrue(collection.add(0));
        for (int expected : List.of(0, 3, 3, 5, 8)) {
            assertEquals(expected, heap.get());
        }
        assertNull(heap.get());
    }

    @Test
    void repairsHeapAfterRemovingAnArbitraryElement() {
        MinHeap<Integer> heap = new MinHeap<>();
        heap.addAll(List.of(1, 5, 2, 8, 9, 3, 4));

        assertTrue(heap.remove(8));
        assertFalse(heap.remove(8));
        assertTrue(heap.remove(2));
        for (int expected : List.of(1, 3, 4, 5, 9)) {
            assertEquals(expected, heap.get());
        }
        assertNull(heap.get());
    }

    @Test
    void supportsIteratorRemovalAndBulkOperations() {
        MinHeap<Integer> heap = new MinHeap<>();
        heap.addAll(List.of(5, 1, 3, 2, 4, 2));

        Iterator<Integer> iterator = heap.iterator();
        assertThrows(IllegalStateException.class, iterator::remove);
        while (iterator.hasNext()) {
            if (iterator.next() == 2) {
                iterator.remove();
                assertThrows(IllegalStateException.class, iterator::remove);
            }
        }
        assertFalse(heap.contains(2));
        assertTrue(heap.removeAll(List.of(3, 4)));
        assertTrue(heap.retainAll(List.of(5)));
        assertEquals(5, heap.get());
        assertTrue(heap.isEmpty());

        heap.addAll(List.of(1, 2));
        heap.clear();
        assertNull(heap.get());
    }

    @Test
    void ordersStringsAndRejectsNull() {
        MinHeap<String> heap = new MinHeap<>();
        heap.addAll(List.of("z", "b", "a"));
        assertThrows(NullPointerException.class, () -> heap.add(null));
        assertEquals(3, heap.size());
        assertEquals("a", heap.get());
        assertEquals("b", heap.get());
        assertEquals("z", heap.get());
    }
}
