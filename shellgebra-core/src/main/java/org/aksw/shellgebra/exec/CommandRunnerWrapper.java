package org.aksw.shellgebra.exec;

public class CommandRunnerWrapper<T>
    implements CommandRunner<T>
{
    private final CommandRunner<T> delegate;

    public CommandRunnerWrapper(CommandRunner<T> delegate) {
        super();
        this.delegate = delegate;
    }

    public CommandRunner<T> getDelegate() {
        return delegate;
    }

    @Override
    public T call(String... argv) {
        T result = getDelegate().call(argv);
        return result;
    }
}
