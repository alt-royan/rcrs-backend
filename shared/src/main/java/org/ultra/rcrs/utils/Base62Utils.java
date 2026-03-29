package org.ultra.rcrs.utils;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.UUID;

public class Base62Utils {
    private static final String ALPHABET =
            "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    private static final BigInteger BASE =
            BigInteger.valueOf(62);

    public static String encode(UUID uuid) {
        ByteBuffer buffer = ByteBuffer.wrap(new byte[16]);

        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());

        BigInteger number = new BigInteger(1, buffer.array());

        StringBuilder result = new StringBuilder();

        while (number.compareTo(BigInteger.ZERO) > 0) {
            BigInteger[] divmod = number.divideAndRemainder(BASE);
            result.insert(0, ALPHABET.charAt(divmod[1].intValue()));
            number = divmod[0];
        }

        return result.toString();
    }

    public static UUID decode(String base62) {
        BigInteger number = BigInteger.ZERO;
        for (char c : base62.toCharArray()) {
            number = number.multiply(BASE);
            number = number.add(BigInteger.valueOf(ALPHABET.indexOf(c)));
        }

        byte[] bytes = number.toByteArray();

        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        long high = buffer.getLong();
        long low = buffer.getLong();

        return new UUID(high, low);
    }

    public static boolean isValid(String s) {
        if (s == null || s.length() != 22) {
            return false;
        }

        for (char c : s.toCharArray()) {

            boolean isDigit = c >= '0' && c <= '9';

            boolean isUpper = c >= 'A' && c <= 'Z';

            boolean isLower = c >= 'a' && c <= 'z';

            if (!(isDigit || isUpper || isLower)) {
                return false;
            }
        }

        return true;
    }
}
