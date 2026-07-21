package org.ultra.rcrs.utils;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

class Base62Test {

    @Test
    void encodeZero() {
        assertEquals("0", Base62.encode(BigInteger.ZERO));
    }

    @Test
    void encodeOne() {
        assertEquals("1", Base62.encode(BigInteger.ONE));
    }

    @Test
    void encodeMaxDigit() {
        assertEquals("z", Base62.encode(BigInteger.valueOf(61)));
    }

    @Test
    void encodeTwoDigits() {
        assertEquals("10", Base62.encode(BigInteger.valueOf(62)));
    }

    @Test
    void encodeThreeDigits() {
        assertEquals("100", Base62.encode(BigInteger.valueOf(3844)));
    }

    @Test
    void encodeLargeValue() {
        BigInteger val = BigInteger.valueOf(62).pow(5).subtract(BigInteger.ONE);
        String encoded = Base62.encode(val);
        assertEquals("zzzzz", encoded);
    }

    @Test
    void encodeMaxUUID() {
        BigInteger max = BigInteger.ONE.shiftLeft(128).subtract(BigInteger.ONE);
        String encoded = Base62.encode(max);
        assertNotNull(encoded);
        assertFalse(encoded.isEmpty());
    }

    @Test
    void encodeNegativeThrows() {
        assertThrows(IllegalArgumentException.class, () -> Base62.encode(BigInteger.valueOf(-1)));
    }

    @Test
    void decodeZero() {
        assertEquals(BigInteger.ZERO, Base62.decode("0"));
    }

    @Test
    void decodeOne() {
        assertEquals(BigInteger.ONE, Base62.decode("1"));
    }

    @Test
    void decodeMaxDigit() {
        assertEquals(BigInteger.valueOf(61), Base62.decode("z"));
    }

    @Test
    void decodeTwoDigits() {
        assertEquals(BigInteger.valueOf(62), Base62.decode("10"));
    }

    @Test
    void decodeUpperCase() {
        assertEquals(BigInteger.valueOf(10), Base62.decode("A"));
    }

    @Test
    void decodeLowerCase() {
        assertEquals(BigInteger.valueOf(36), Base62.decode("a"));
    }

    @Test
    void decodeNullThrows() {
        assertThrows(NullPointerException.class, () -> Base62.decode(null));
    }

    @Test
    void decodeEmptyThrows() {
        assertThrows(IllegalArgumentException.class, () -> Base62.decode(""));
    }

    @Test
    void decodeInvalidCharsThrows() {
        assertThrows(IllegalArgumentException.class, () -> Base62.decode("hello!"));
    }

    @Test
    void decodeSpecialCharsThrows() {
        assertThrows(IllegalArgumentException.class, () -> Base62.decode("+123"));
    }

    @Test
    void roundtripSmallNumbers() {
        for (long i = 0; i < 10_000; i++) {
            BigInteger val = BigInteger.valueOf(i);
            assertEquals(val, Base62.decode(Base62.encode(val)));
        }
    }

    @Test
    void roundtripLargeNumber() {
        BigInteger val = new BigInteger("123456789012345678901234567890");
        assertEquals(val, Base62.decode(Base62.encode(val)));
    }

    @Test
    void roundtripMax128Bit() {
        BigInteger val = BigInteger.ONE.shiftLeft(128).subtract(BigInteger.ONE);
        assertEquals(val, Base62.decode(Base62.encode(val)));
    }

    @Test
    void decodeExceeds128BitThrows() {
        String encoded = Base62.encode(BigInteger.ONE.shiftLeft(128));
        assertThrows(IllegalArgumentException.class, () -> Base62.decode(encoded));
    }

    @Test
    void decodeWithCustomBitLimit() {
        String encoded = Base62.encode(BigInteger.valueOf(255));
        assertEquals(BigInteger.valueOf(255), Base62.decode(encoded, 8));
    }

    @Test
    void decodeWithBitLimitExceededThrows() {
        String encoded = Base62.encode(BigInteger.valueOf(256));
        assertThrows(IllegalArgumentException.class, () -> Base62.decode(encoded, 8));
    }
}
