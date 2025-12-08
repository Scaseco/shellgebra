package org.aksw.vshell.registry;

/**
 * Helper class for wrapping entities with a close action for use with try-with-resources blocks.
 *
 * Usage example:
 * <pre>{@code
 * try (ClosePolicyWrapper<T> wrapper = ClosePolicyWrapper.dontClose(entity)) { // Or use .doClose
 *   // ...
 * }
 * }</pre>
 *
 * @param <T>
 */
public record ClosePolicyWrapper<T>(T entity, AutoCloseable closeAction, boolean doClose) implements AutoCloseable {
    public static <T extends AutoCloseable> ClosePolicyWrapper<T> doClose(T entity) {
        return new ClosePolicyWrapper<>(entity, entity, true);
    }

    public static <T extends AutoCloseable> ClosePolicyWrapper<T> dontClose(T entity) {
        return new ClosePolicyWrapper<>(entity, entity, false);
    }

    @Override
    public void close() throws Exception {
        if (doClose && closeAction != null) {
            closeAction.close();
        }
    }
}
