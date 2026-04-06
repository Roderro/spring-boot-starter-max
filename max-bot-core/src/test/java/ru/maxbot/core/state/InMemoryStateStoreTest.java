package ru.maxbot.core.state;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class InMemoryStateStoreTest {

    @Test
    void initialStateIsNull() {
        var store = new InMemoryStateStore();
        assertNull(store.getState(1L));
    }

    @Test
    void setAndGetState() {
        var store = new InMemoryStateStore();
        store.setState(1L, "STEP_1");
        assertEquals("STEP_1", store.getState(1L));
    }

    @Test
    void clearState() {
        var store = new InMemoryStateStore();
        store.setState(1L, "STEP_1");
        store.clearState(1L);
        assertNull(store.getState(1L));
    }

    @Test
    void setNullClearsState() {
        var store = new InMemoryStateStore();
        store.setState(1L, "STEP_1");
        store.setState(1L, null);
        assertNull(store.getState(1L));
    }

    @Test
    void statesAreIsolatedByChatId() {
        var store = new InMemoryStateStore();
        store.setState(1L, "A");
        store.setState(2L, "B");
        assertEquals("A", store.getState(1L));
        assertEquals("B", store.getState(2L));
    }
}


