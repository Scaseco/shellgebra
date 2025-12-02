package org.aksw.shellgebra.exec.graph;

import java.lang.ProcessBuilder.Redirect;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.aksw.shellgebra.exec.graph.JRedirect.PRedirectJava;

// TODO Consolidate with IProcessBuilderBase
public class ProcessBuilder2 {
    private List<String> command;
    private String directory;
    private Map<String, String> environment = new LinkedHashMap<>();
    private JRedirect[] redirects = new JRedirect[3];

    public ProcessBuilder2() {
        super();
        this.redirects[0] = new PRedirectJava(Redirect.INHERIT);
        this.redirects[1] = new PRedirectJava(Redirect.INHERIT);
        this.redirects[2] = new PRedirectJava(Redirect.INHERIT);
    }

    public List<String> command() {
        return command;
    }

    public void setCommand(List<String> command) {
        this.command = command;
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public Map<String, String> getEnvironment() {
        return environment;
    }

    public void setEnvironment(Map<String, String> environment) {
        this.environment = environment;
    }

    public JRedirect redirectInput() {
        return redirects[0];
    }

    public JRedirect redirectOutput() {
        return redirects[1];
    }

    public JRedirect redirectError() {
        return redirects[2];
    }

    public ProcessBuilder2 redirectInput(JRedirect redirect) {
        redirects[0] = redirect;
        return this;
    }

    public ProcessBuilder2 redirectOutput(JRedirect redirect) {
        redirects[1] = redirect;
        return this;
    }

    public ProcessBuilder2 redirectError(JRedirect redirect) {
        redirects[2] = redirect;
        return this;
    }

//    public JRedirect[] getRedirects() {
//        return redirects;
//    }
//    public void setRedirects(JRedirect[] redirects) {
//        this.redirects = redirects;
//    }


}
