package com.distrimind.util.crypto.fortuna;

import static org.testng.Assert.*;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class CounterTest {

    private Counter counter;

    @BeforeMethod
    public void before() {
        counter = new Counter(128);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void shouldFailForInvalidNumberOfBits() {
        new Counter(127);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void shouldFailForTooFewBits() {
        new Counter(0);
    }

    @Test
    public void shouldRollOverBackToZero() {
        Counter smallCounter = new Counter(8);
        for (int i = 0; i < 256; i++) {
            smallCounter.increment();
        }
        assertTrue(smallCounter.isZero());
    }

    @Test
    public void shouldBeZero() {
        assertTrue(counter.isZero());
    }

    @Test
    public void shouldIncrementOneStep() {
        counter.increment();
        byte[] state = counter.getState();
        assertEquals(state.length, 16);
        assertEquals(state[0], 1);
        assertFalse(counter.isZero());
    }

    @Test
    public void shouldFillFirstByteWithOnes() {
        for (int i = 0; i < 255; i++) {
            counter.increment();
        }
        byte[] state = counter.getState();
        assertEquals((byte) 0xff, state[0]);
    }

    @Test
    public void shouldRollOverIntoNextByte() {
        for (int i = 0; i < 256; i++) {
            counter.increment();
        }
        byte[] state = counter.getState();
        assertEquals((byte) 0x0, state[0]);
        assertEquals((byte) 0x1, state[1]);
    }

    @Test
    public void shouldRollOverIntoNextByteAgain() {
        for (int i = 0; i < 257; i++) {
            counter.increment();
        }
        byte[] state = counter.getState();
        assertEquals((byte) 0x1, state[0]);
        assertEquals((byte) 0x1, state[1]);
    }

    @Test
    public void shouldRollOverIntoThirdByte() {
        for (int i = 0; i < 256*256; i++) {
            counter.increment();
        }
        byte[] state = counter.getState();
        assertEquals((byte) 0x0, state[0]);
        assertEquals((byte) 0x0, state[1]);
        assertEquals((byte) 0x1, state[2]);
    }
}
