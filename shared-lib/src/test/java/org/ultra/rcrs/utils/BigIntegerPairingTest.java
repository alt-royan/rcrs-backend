package org.ultra.rcrs.utils;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

class BigIntegerPairingTest {

    @Test
    void pairZeroZero() {
        BigInteger[] result = BigIntegerPairing.unpair(BigIntegerPairing.pair(BigInteger.ZERO, BigInteger.ZERO));
        assertEquals(BigInteger.ZERO, result[0]);
        assertEquals(BigInteger.ZERO, result[1]);
    }

    @Test
    void pairOneOne() {
        BigInteger[] result = BigIntegerPairing.unpair(BigIntegerPairing.pair(BigInteger.ONE, BigInteger.ONE));
        assertEquals(BigInteger.ONE, result[0]);
        assertEquals(BigInteger.ONE, result[1]);
    }

    @Test
    void pairLongMax() {
        BigInteger hi = BigInteger.valueOf(Long.MAX_VALUE);
        BigInteger lo = BigInteger.valueOf(Long.MAX_VALUE);
        BigInteger[] result = BigIntegerPairing.unpair(BigIntegerPairing.pair(hi, lo));
        assertEquals(hi, result[0]);
        assertEquals(lo, result[1]);
    }

    @Test
    void pairLongMin() {
        BigInteger hi = BigInteger.valueOf(Long.MIN_VALUE);
        BigInteger lo = BigInteger.valueOf(Long.MIN_VALUE);
        BigInteger[] result = BigIntegerPairing.unpair(BigIntegerPairing.pair(hi, lo));
        assertEquals(hi, result[0]);
        assertEquals(lo, result[1]);
    }

    @Test
    void pairNegativeOne() {
        BigInteger hi = BigInteger.valueOf(-1);
        BigInteger lo = BigInteger.valueOf(-1);
        BigInteger[] result = BigIntegerPairing.unpair(BigIntegerPairing.pair(hi, lo));
        assertEquals(hi, result[0]);
        assertEquals(lo, result[1]);
    }

    @Test
    void pairMixedSign() {
        BigInteger hi = BigInteger.valueOf(Long.MAX_VALUE);
        BigInteger lo = BigInteger.valueOf(Long.MIN_VALUE);
        BigInteger[] result = BigIntegerPairing.unpair(BigIntegerPairing.pair(hi, lo));
        assertEquals(hi, result[0]);
        assertEquals(lo, result[1]);
    }

    @Test
    void pairMixedSignReversed() {
        BigInteger hi = BigInteger.valueOf(Long.MIN_VALUE);
        BigInteger lo = BigInteger.valueOf(Long.MAX_VALUE);
        BigInteger[] result = BigIntegerPairing.unpair(BigIntegerPairing.pair(hi, lo));
        assertEquals(hi, result[0]);
        assertEquals(lo, result[1]);
    }

    @Test
    void pairHiOnly() {
        BigInteger hi = BigInteger.valueOf(42);
        BigInteger lo = BigInteger.ZERO;
        BigInteger[] result = BigIntegerPairing.unpair(BigIntegerPairing.pair(hi, lo));
        assertEquals(hi, result[0]);
        assertEquals(lo, result[1]);
    }

    @Test
    void pairLoOnly() {
        BigInteger hi = BigInteger.ZERO;
        BigInteger lo = BigInteger.valueOf(99);
        BigInteger[] result = BigIntegerPairing.unpair(BigIntegerPairing.pair(hi, lo));
        assertEquals(hi, result[0]);
        assertEquals(lo, result[1]);
    }

    @Test
    void pairRoundtripRandomValues() {
        for (int i = 0; i < 1000; i++) {
            BigInteger hi = BigInteger.valueOf((long) (Math.random() * Long.MAX_VALUE));
            BigInteger lo = BigInteger.valueOf((long) (Math.random() * Long.MAX_VALUE));
            if (i % 2 == 0) hi = hi.negate();
            if (i % 3 == 0) lo = lo.negate();
            BigInteger[] result = BigIntegerPairing.unpair(BigIntegerPairing.pair(hi, lo));
            assertEquals(hi, result[0]);
            assertEquals(lo, result[1]);
        }
    }

    @Test
    void pairedValueIsUnique() {
        BigInteger p1 = BigIntegerPairing.pair(BigInteger.ONE, BigInteger.TEN);
        BigInteger p2 = BigIntegerPairing.pair(BigInteger.TEN, BigInteger.ONE);
        assertNotEquals(p1, p2);
    }
}
