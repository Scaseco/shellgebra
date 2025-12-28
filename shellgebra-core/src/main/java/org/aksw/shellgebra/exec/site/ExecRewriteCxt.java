package org.aksw.shellgebra.exec.site;

import java.nio.file.Path;
import java.util.Map;

public class ExecRewriteCxt {
    int processCounter = 0;
    Path processCtl; // allocated when needed.

    String basePath;
    String processCtlPath;
    Map<String, ExecStage> externalExecutions;

    public String allocateOutPipePath(int cmdId) {
        return basePath + "/outPipe" + cmdId;
    }
}
