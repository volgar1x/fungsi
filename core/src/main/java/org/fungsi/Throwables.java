package org.fungsi;

public final class Throwables {
    private Throwables() {}

    public static RuntimeException propagate(Throwable t) {
        Throwables.<RuntimeException>sneakyThrow0(t);
        return null;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void sneakyThrow0(Throwable t) throws T {
        throw (T)t;
    }
}
