package org.fungsi.concurrent;

import org.fungsi.Either;

public interface Promise<T> extends Future<T> {
	void set(Either<T, Throwable> e);

    default void complete(T o) {
        set(Either.success(o));
    }

    default void fail(Throwable cause) {
        set(Either.failure(cause));
    }
}
