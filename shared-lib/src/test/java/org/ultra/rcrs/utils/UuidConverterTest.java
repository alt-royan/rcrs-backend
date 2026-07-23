package org.ultra.rcrs.utils;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UuidConverterTest {

    @Test
    void roundtripZeroUUID() {
        UUID uuid = new UUID(0, 0);
        assertEquals(uuid, UuidConverter.toUuid(UuidConverter.toBigInteger(uuid)));
    }

    @Test
    void roundtripMaxUUID() {
        UUID uuid = new UUID(-1, -1);
        assertEquals(uuid, UuidConverter.toUuid(UuidConverter.toBigInteger(uuid)));
    }

    @Test
    void roundtripRandomUUID() {
        UUID uuid = UUID.fromString("8428f981-c55d-47fc-8f80-e68daa311dcb");
        assertEquals(uuid, UuidConverter.toUuid(UuidConverter.toBigInteger(uuid)));
    }

    @Test
    void roundtripManyRandomUUIDs() {
        for (int i = 0; i < 1000; i++) {
            UUID uuid = UUID.randomUUID();
            assertEquals(uuid, UuidConverter.toUuid(UuidConverter.toBigInteger(uuid)));
        }
    }

    @Test
    void roundtripFromBigInteger() {
        BigInteger[] values = {
                BigInteger.ZERO,
                BigInteger.ONE,
                BigInteger.valueOf(Long.MAX_VALUE),
                BigInteger.ONE.shiftLeft(64).subtract(BigInteger.ONE),
                BigInteger.ONE.shiftLeft(64),
                BigInteger.ONE.shiftLeft(127),
                BigInteger.ONE.shiftLeft(128).subtract(BigInteger.ONE),
        };
        for (BigInteger val : values) {
            assertEquals(val, UuidConverter.toBigInteger(UuidConverter.toUuid(val)));
        }
    }

    @Test
    void uuidComponentsPreserved() {
        UUID uuid = UUID.randomUUID();
        BigInteger bigInt = UuidConverter.toBigInteger(uuid);
        UUID restored = UuidConverter.toUuid(bigInt);
        assertEquals(uuid.getMostSignificantBits(), restored.getMostSignificantBits());
        assertEquals(uuid.getLeastSignificantBits(), restored.getLeastSignificantBits());
    }

    @Test
    void specificUuidMapsToKnownBigInteger() {
        UUID uuid = new UUID(0, 1);
        BigInteger bigInt = UuidConverter.toBigInteger(uuid);
        assertEquals(BigInteger.ONE, bigInt);
    }

    @Test
    void producesPositiveBigInteger() {
        UUID uuid = new UUID(Long.MIN_VALUE, Long.MIN_VALUE);
        BigInteger bigInt = UuidConverter.toBigInteger(uuid);
        assertTrue(bigInt.signum() >= 0);
    }
}
