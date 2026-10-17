package com.github.ibanetchep.msquests.bukkit.document;

import java.security.SecureRandom;

/** ULIDs (26 chars, Crockford base32, time-ordered): the {@code uid} of groups and quests. */
public final class Ulid {

    private static final char[] ALPHABET = "0123456789ABCDEFGHJKMNPQRSTVWXYZ".toCharArray();
    private static final SecureRandom RANDOM = new SecureRandom();

    private Ulid() {
    }

    public static String generate() {
        return generate(System.currentTimeMillis());
    }

    static String generate(long timeMillis) {
        char[] out = new char[26];
        long time = timeMillis;
        for (int i = 9; i >= 0; i--) {
            out[i] = ALPHABET[(int) (time & 31)];
            time >>>= 5;
        }
        byte[] random = new byte[10];
        RANDOM.nextBytes(random);
        // 80 random bits → 16 base32 chars.
        long hi = 0;
        for (int i = 0; i < 5; i++) hi = (hi << 8) | (random[i] & 0xFF);
        long lo = 0;
        for (int i = 5; i < 10; i++) lo = (lo << 8) | (random[i] & 0xFF);
        for (int i = 17; i >= 10; i--) {
            out[i] = ALPHABET[(int) (hi & 31)];
            hi >>>= 5;
        }
        for (int i = 25; i >= 18; i--) {
            out[i] = ALPHABET[(int) (lo & 31)];
            lo >>>= 5;
        }
        return new String(out);
    }
}
