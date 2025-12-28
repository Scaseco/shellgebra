package org.aksw.shellgebra.algebra.cmd.transform;

import org.aksw.shellgebra.algebra.cmd.op.CmdOp;
import org.aksw.shellgebra.algebra.cmd.redirect.RedirectTarget.RedirectTargetFile;
import org.aksw.shellgebra.algebra.cmd.redirect.RedirectTarget.RedirectTargetProcessSubstitution;
import org.aksw.shellgebra.algebra.cmd.redirect.RedirectTargetVisitor;
import org.aksw.shellgebra.exec.CmdStrOps;

public class RedirectTargetVisitorToString
    implements RedirectTargetVisitor<String> {

    protected CmdStrOps strOps;

    public RedirectTargetVisitorToString(CmdStrOps strOps) {
        super();
        this.strOps = strOps;
    }

    @Override
    public String visit(RedirectTargetFile redirect) {
        String file = redirect.file();
        String result = strOps.quoteArg(file);
        return result;
    }

    @Override
    public String visit(RedirectTargetProcessSubstitution redirect) {
        CmdOp cmdOp = redirect.cmdOp();
        CmdOpVisitorToCmdString visitor = new CmdOpVisitorToCmdString(strOps);
        String str = CmdOpVisitorToCmdString.toString(cmdOp, visitor);
        String result = "<(" + str + ")";
        return result;
    }
}
