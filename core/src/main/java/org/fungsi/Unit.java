package org.fungsi;

import org.fungsi.function.UnsafeConsumer;
import org.fungsi.function.UnsafeFunction;
import org.fungsi.function.UnsafeRunnable;
import org.fungsi.function.UnsafeSupplier;

import java.io.Serializable;

public final class Unit
		implements Comparable<Unit>, Serializable,
		UnsafeRunnable, UnsafeSupplier<Unit>, UnsafeFunction<Object, Unit>, UnsafeConsumer<Object>
{
	private Unit() { }

	@Override
	public int hashCode() {
		return 0;
	}

	@Override
	public boolean equals(Object obj) {
		return obj == this;
	}

	@Override
	public String toString() {
		return "org.fungsi.Unit";
	}

	@Override
	public int compareTo(Unit o) {
		return 0;
	}

	@Override
	public Unit apply(Object o) {
		return this;
	}

	@Override
	public void run() { }

	@Override
	public Unit get() {
		return this;
	}

    @Override
    public void accept(Object o) {
    }

    private static final Unit UNIT = new Unit();
	private static final Either<Unit, Object> LEFT = Either.left(UNIT);
	private static final Either<Object, Unit> RIGHT = Either.right(UNIT);

	public static Unit instance() { return UNIT; }
	public static Unit unit() { return instance(); }

	@SuppressWarnings("unchecked")
	public static <T> Either<Unit, T> left() { return (Either<Unit, T>) LEFT; }
	@SuppressWarnings("unchecked")
	public static <T> Either<T, Unit> right() { return (Either<T, Unit>) RIGHT; }
}
