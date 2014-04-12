package org.fungsi.concurrent;

import org.fungsi.Either;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public final class Futures {
	private Futures() {}

	public static <T> Future<List<T>> collect(List<Future<T>> futures) {
		Promise<List<T>> p = Promises.create();

		final AtomicInteger count = new AtomicInteger(futures.size());
		final List<T> values = new ArrayList<>();

		for (Future<T> fut : futures) {
			fut.onSuccess(value -> {
				values.add(value);

				if (count.decrementAndGet() == 0) {
					p.set(Either.success(values));
				}
			}).onFailure(cause -> {
				count.set(-1);
				p.set(Either.failure(cause));
			});
		}

		return p;
	}

	public static <T> Collector<Future<T>, ?, Future<List<T>>> collect() {
		return new Collector<Future<T>, List<Future<T>>, Future<List<T>>>() {
			@Override
			public Supplier<List<Future<T>>> supplier() {
				return ArrayList::new;
			}

			@Override
			public BiConsumer<List<Future<T>>, Future<T>> accumulator() {
				return List::add;
			}

			@Override
			public BinaryOperator<List<Future<T>>> combiner() {
				return (a, b) -> {
					a.addAll(b);
					return a;
				};
			}

			@Override
			public Function<List<Future<T>>, Future<List<T>>> finisher() {
				return Futures::collect;
			}

			@Override
			public Set<Characteristics> characteristics() {
				return EnumSet.of(Characteristics.UNORDERED);
			}
		};
	}
}
