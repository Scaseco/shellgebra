package org.aksw.shellgebra.algebra.cmd.op;

import java.util.Objects;

// Model in not ideal: Redirects are not operators but modifiers on a command:
// Command(String[] argv, List<Redirect> redirects)
//public class CmdOpRedirectRight
//    extends CmdOp1
//{
//    protected String fileName;
//
//    public CmdOpRedirectRight(String fileName, CmdOp subOp) {
//        super(subOp);
//        this.fileName = Objects.requireNonNull(fileName);
//    }
//
//    public String getFileName() {
//        return fileName;
//    }
//
//    @Override
//    public <T> T accept(CmdOpVisitor<T> visitor) {
//        T result = visitor.visit(this);
//        return result;
//    }
//
//    @Override
//    public String toString() {
//        return "(redirect " + fileName + " " + subOp + ")";
//    }
//}
