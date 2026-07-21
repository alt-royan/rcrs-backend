package org.ultra.rcrs.mediaservice.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HashTest {

    @Test
    void sha1Base64EmptyString() {
        String result = Hash.sha1Base64("");
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(27, result.length());
    }

    @Test
    void sha1Base64SimpleString() {
        String result = Hash.sha1Base64("hello");
        assertNotNull(result);
        assertTrue(result.matches("[A-Za-z0-9_-]+"));
    }

    @Test
    void sha1Base64Deterministic() {
        String input = "test-file-name.mp3";
        assertEquals(Hash.sha1Base64(input), Hash.sha1Base64(input));
    }

    @Test
    void sha1Base64DifferentInputs() {
        assertNotEquals(Hash.sha1Base64("file1.mp3"), Hash.sha1Base64("file2.mp3"));
    }

    @Test
    void sha1Base64SpecialCharacters() {
        String result = Hash.sha1Base64("file name with spaces!@#.mp3");
        assertNotNull(result);
        assertTrue(result.matches("[A-Za-z0-9_-]+"));
    }

    @Test
    void sha1Base64Unicode() {
        String result = Hash.sha1Base64("résumé.mp3");
        assertNotNull(result);
        assertEquals(27, result.length());
    }

    @Test
    void sha1Base64IsUrlSafe() {
        String result = Hash.sha1Base64("https://example.com/file.mp3");
        assertTrue(result.matches("[A-Za-z0-9_-]+"));
    }

    @Test
    void sha1HexSimple() {
        String result = Hash.sha1("hello".getBytes());
        assertEquals(40, result.length());
        assertTrue(result.matches("[0-9a-f]+"));
    }

    @Test
    void sha1HexDeterministic() {
        byte[] input = "test".getBytes();
        assertEquals(Hash.sha1(input), Hash.sha1(input));
    }

    @Test
    void sha1HexDifferentInputs() {
        assertNotEquals(Hash.sha1("a".getBytes()), Hash.sha1("b".getBytes()));
    }
}
