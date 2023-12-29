package me.jsedwards.util;

public class MathUtils {

    private MathUtils() {}

    public static double scale(double srcStart, double srcEnd, double destStart, double destEnd, double src) {
        return destStart + (src - srcStart) / (srcEnd - srcStart) * (destEnd - destStart);
    }
}
