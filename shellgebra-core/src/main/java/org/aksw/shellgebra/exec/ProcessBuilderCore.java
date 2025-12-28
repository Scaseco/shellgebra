package org.aksw.shellgebra.exec;

import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.aksw.shellgebra.exec.graph.JRedirect;
import org.aksw.shellgebra.exec.graph.JRedirect.JRedirectJava;

public abstract class ProcessBuilderCore<X extends ProcessBuilderCore<X>>
    implements IProcessBuilderCore<X>
{
    private Path directory;
    private Map<String,String> environment;
    private boolean redirectErrorStream;

    private List<JRedirect> redirects = new ArrayList<>(List.of(
        new JRedirectJava(Redirect.INHERIT),
        new JRedirectJava(Redirect.INHERIT),
        new JRedirectJava(Redirect.INHERIT)));

    @SuppressWarnings("unchecked")
    protected X self() {
        return (X)this;
    }

    public ProcessBuilderCore() {
        super();
        this.environment = new LinkedHashMap<>();
    }

    @Override
    public Path directory() {
        return directory;
    }

    @Override
    public X directory(Path directory) {
        this.directory = directory;
        return self();
    }

    @Override
    public Map<String, String> environment() {
        return environment;
    }

    @Override
    public boolean redirectErrorStream() {
        return redirectErrorStream;
    }

    @Override
    public X redirectErrorStream(boolean redirectErrorStream) {
        this.redirectErrorStream = redirectErrorStream;
        return self();
    }

    @Override
    public X redirectInput(JRedirect redirect) {
        Objects.requireNonNull(redirect, "redirectInput");
        redirects.set(0, redirect);
        return self();
    }

    @Override
    public JRedirect redirectInput() {
        return redirects.get(0);
    }

    @Override
    public X redirectOutput(JRedirect redirect) {
        Objects.requireNonNull(redirect, "redirectOutput");
        redirects.set(1, redirect);
        return self();
    }

    @Override
    public JRedirect redirectOutput() {
        return redirects.get(1);
    }

    @Override
    public X redirectError(JRedirect redirect) {
        Objects.requireNonNull(redirect, "redirectError");
        redirects.set(2, redirect);
        return self();
    }

    @Override
    public JRedirect redirectError() {
        return redirects.get(2);
    }

    @Override
    public X clone() {
        X result = cloneActual();
        applySettings(result);
        return result;
    }

    protected void applySettings(ProcessBuilderCore<?> target) {
        target.environment().putAll(environment());
        target.directory(directory());
        target.redirectErrorStream(redirectErrorStream());
        target.redirectInput(redirectInput());
        target.redirectOutput(redirectOutput());
        target.redirectError(redirectError());
    }

    protected abstract X cloneActual();
}
