package org.ultra.rcrs.utils;

import org.junit.jupiter.api.Test;
import org.ultra.rcrs.exceptions.DecodeFromBase62Exception;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class Url62Test {

    @Test
    void encodeDecodeRoundtrip() {
        UUID uuid = UUID.fromString("8428f981-c55d-47fc-8f80-e68daa311dcb");
        String encoded = Url62.encode(uuid);
        UUID decoded = Url62.decode(encoded);
        assertEquals(uuid, decoded);
    }

    @Test
    void encodeDecodeZeroUUID() {
        UUID uuid = new UUID(0, 0);
        String encoded = Url62.encode(uuid);
        UUID decoded = Url62.decode(encoded);
        assertEquals(uuid, decoded);
    }

    @Test
    void encodeDecodeMaxUUID() {
        UUID uuid = new UUID(-1, -1);
        String encoded = Url62.encode(uuid);
        UUID decoded = Url62.decode(encoded);
        assertEquals(uuid, decoded);
    }

    @Test
    void encodeDecodeRandomUUIDs() {
        for (int i = 0; i < 1000; i++) {
            UUID uuid = UUID.randomUUID();
            String encoded = Url62.encode(uuid);
            UUID decoded = Url62.decode(encoded);
            assertEquals(uuid, decoded);
        }
    }

    @Test
    void encodedStringIsUrlSafe() {
        UUID uuid = UUID.randomUUID();
        String encoded = Url62.encode(uuid);
        assertTrue(encoded.matches("[0-9A-Za-z]+"));
    }

    @Test
    void encodedLengthIsShort() {
        UUID uuid = UUID.randomUUID();
        String encoded = Url62.encode(uuid);
        assertTrue(encoded.length() <= 22);
    }

    @Test
    void decodeNullThrows() {
        assertThrows(DecodeFromBase62Exception.class, () -> Url62.decode(null));
    }

    @Test
    void decodeEmptyStringThrows() {
        assertThrows(DecodeFromBase62Exception.class, () -> Url62.decode(""));
    }

    @Test
    void decodeInvalidCharsThrows() {
        assertThrows(DecodeFromBase62Exception.class, () -> Url62.decode("hello!"));
    }

    @Test
    void decodeSpecialCharsThrows() {
        assertThrows(DecodeFromBase62Exception.class, () -> Url62.decode("+abc/def="));
    }

    @Test
    void decodeRandomGarbageThrows() {
        assertThrows(DecodeFromBase62Exception.class, () -> Url62.decode("not-a-valid-base62"));
    }

    @Test
    void encodeResultIsDeterministic() {
        UUID uuid = UUID.randomUUID();
        String first = Url62.encode(uuid);
        String second = Url62.encode(uuid);
        assertEquals(first, second);
    }

    @Test
    void differentUuidsProduceDifferentEncodings() {
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();
        assertNotEquals(Url62.encode(uuid1), Url62.encode(uuid2));
    }

    @Test
    void decodeKnownEncodedValue() {
        UUID uuid = UUID.fromString("8428f981-c55d-47fc-8f80-e68daa311dcb");
        String encoded = Url62.encode(uuid);
        assertDoesNotThrow(() -> Url62.decode(encoded));
    }
}
