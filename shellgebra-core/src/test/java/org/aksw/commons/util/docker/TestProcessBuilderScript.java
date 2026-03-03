package org.aksw.commons.util.docker;

import org.junit.jupiter.api.Test;

import org.aksw.shellgebra.exec.graph.ProcessIoWrapper;
import org.aksw.shellgebra.exec.graph.ProcessIoWrapper.ExecResult;
import org.aksw.shellgebra.exec.invocation.InvokableProcessBuilderHost;
import org.aksw.shellgebra.exec.invocation.ScriptContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestProcessBuilderScript {
    private static final Logger logger = LoggerFactory.getLogger(TestProcessBuilderScript.class);

    @Test
    public void test01() throws Exception {
        InvokableProcessBuilderHost pb = new InvokableProcessBuilderHost();
        pb.script("echo 'hello world'; echo 'it worked';", ScriptContent.contentTypeBash);
//        pb.redirectInput(new JRedirectJava(Redirect.INHERIT));
//        pb.redirectOutput(new JRedirectJava(Redirect.INHERIT));
//        pb.redirectError(new JRedirectJava(Redirect.INHERIT));

        Process p = pb.start();
        ExecResult er = ProcessIoWrapper.builder(p).consume();
        System.out.println(er.out());
        System.out.println(er.err());
        // p.waitFor();
    }
}
