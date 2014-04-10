package org.fungsi.concurrent;

import org.fungsi.Either;

public interface Promise<T> extends Future<T> {
	void set(Either<T, Throwable> e);
}
