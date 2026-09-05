package com.example.urlshortener.util;

public class Base62Encoder {

    private static final char[] CHARACTERS =
            "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
                    .toCharArray();

    public static String encode(long number) {

        if (number == 0) {
            return "0";
        }

        StringBuilder result = new StringBuilder();

        while (number > 0) {

            int remainder = (int) (number % 62);

            result.append(CHARACTERS[remainder]);

            number /= 62;
        }

        return result.reverse().toString();
    }
}
