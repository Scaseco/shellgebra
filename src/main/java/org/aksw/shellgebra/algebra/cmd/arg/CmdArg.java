package org.aksw.shellgebra.algebra.cmd.arg;

/**
 * Argument to a CmdOpExec.
 *
 * Path arguments may require quoting before they can be turned to the final strings.
 */
public interface CmdArg {
    <T> T accept(CmdArgVisitor<T> visitor);
}
