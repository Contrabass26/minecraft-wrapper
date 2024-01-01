package me.jsedwards.util;

public class MathUtils {

    private MathUtils() {}

    public static double scale(double srcStart, double srcEnd, double destStart, double destEnd, double src) {
        return destStart + (src - srcStart) / (srcEnd - srcStart) * (destEnd - destStart);
    }

    public static double roundToNearestMultiple(double n, double round) {
        return Math.round(n / round) * round;
    }

    public static double quadraticFunction(double x, double a, double b, double c) {
        return a * Math.pow(x, 2) + b * x + c;
    }
}
