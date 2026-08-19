package net.optifine.util;

import java.lang.reflect.Method;

public class NativeMemory {
	private static LongSupplier bufferAllocatedSupplier = makeLongSupplier(new String[][] {{"sun.misc.SharedSecrets", "getJavaNioAccess", "getDirectBufferPool", "getMemoryUsed"}, {"jdk.internal.misc.SharedSecrets", "getJavaNioAccess", "getDirectBufferPool", "getMemoryUsed"}});
	private static LongSupplier bufferMaximumSupplier = makeLongSupplier(new String[][] {{"sun.misc.VM", "maxDirectMemory"}, {"jdk.internal.misc.VM", "maxDirectMemory"}});

	public static long getBufferAllocated() {
		return bufferAllocatedSupplier == null ? -1L : bufferAllocatedSupplier.getAsLong();
	}

	public static long getBufferMaximum() {
		return bufferMaximumSupplier == null ? -1L : bufferMaximumSupplier.getAsLong();
	}

	private static LongSupplier makeLongSupplier(String[][] paths) {
		for (int pathIndex = 0; pathIndex < paths.length; ++pathIndex) {
			String[] path = paths[pathIndex];

			try {
				LongSupplier supplier = makeLongSupplier(path);
				return supplier;
			} catch (Throwable throwable) {
				;
			}
		}

		return null;
	}

	private static LongSupplier makeLongSupplier(String[] path) throws Exception {
		if (path.length < 2) {
			return null;
		} else {
			Class ownerClass = Class.forName(path[0]);
			Method method = ownerClass.getMethod(path[1], new Class[0]);
			method.setAccessible(true);
			Object targetObject = null;

			for (int methodIndex = 2; methodIndex < path.length; ++methodIndex) {
				String methodName = path[methodIndex];
				targetObject = method.invoke(targetObject, new Object[0]);
				method = targetObject.getClass().getMethod(methodName, new Class[0]);
				method.setAccessible(true);
			}

			Method finalMethod = method;
			Object finalObject = targetObject;
			LongSupplier supplier = new LongSupplier() {
				private boolean disabled = false;

				public long getAsLong() {
					if (this.disabled) {
						return -1L;
					} else {
						try {
							return ((Long) finalMethod.invoke(finalObject, new Object[0])).longValue();
						} catch (Throwable throwable) {
							this.disabled = true;
							return -1L;
						}
					}
				}
			};
			return supplier;
		}
	}
}
