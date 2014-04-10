package org.fungsi.concurrent;

public final class Promises {
	private Promises() {}

	public static <T> Promise<T> create() {
		return new PromiseImpl<>();
	}
}
