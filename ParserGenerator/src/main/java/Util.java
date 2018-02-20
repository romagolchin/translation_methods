import org.jetbrains.annotations.NotNull;

/**
 * @author Roman Golchin (romagolchin@gmail.com)
 */
class Util {

    static String capitalize(@NotNull String s) {
        if (s.isEmpty()) return s;
        return String.valueOf(Character.toUpperCase(s.charAt(0))) + (s.length() > 1 ? s.substring(1) : "");
    }

    static class Constants {
        static final int END = -1;

        static final int EPS = -2;

        static final int NONE = Integer.MIN_VALUE;
    }
}
