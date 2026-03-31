package org.ultra.rcrs.utils;

import org.ultra.rcrs.exceptions.DecodeFromBase62Exception;
import org.ultra.rcrs.exceptions.EncodeToBase62Exception;

import java.math.BigInteger;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Class to convert UUID to Url62 IDs
 */
public class Url62 {

    private static final Logger LOGGER = Logger.getLogger(Url62.class.getName());


    /**
     * Encode UUID to Url62 id
     *
     * @param uuid UUID to be encoded
     * @return url62 encoded UUID
     */
    public static String encode(UUID uuid) {
        try {
            BigInteger pair = UuidConverter.toBigInteger(uuid);
            return Base62.encode(pair);
        } catch (IllegalArgumentException ex) {
            throw new EncodeToBase62Exception("UUID cannot be encode to Base62", ex);
        }
    }

    /**
     * Decode url62 id to UUID
     *
     * @param id url62 encoded id
     * @return decoded UUID
     */
    public static UUID decode(String id) {
        try {
            BigInteger decoded = Base62.decode(id);
            return UuidConverter.toUuid(decoded);
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new DecodeFromBase62Exception("Invalid base62 id", ex);
        }
    }

}