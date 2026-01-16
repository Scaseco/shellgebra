package org.aksw.shellgebra.algebra.cmd.op;

import java.util.Objects;

public sealed interface RedirectTarget {

    <T> T accept(RedirectTargetVisitor<T> visitor);

    public record RedirectTargetFile(String file) implements RedirectTarget {

        public RedirectTargetFile {
            Objects.requireNonNull(file);
        }

        public static RedirectTargetFile fileFromStdIn(String file) {
            return new RedirectTargetFile(file);
        }
//
//        public static RedirectTargetFile fileToStdOut(String file, OpenMode openMode) {
//            return new RedirectTargetFile(file, openMode, 1);
//        }
//
//        public static RedirectTargetFile fileToStdErr(String file, OpenMode openMode) {
//            return new RedirectTargetFile(file, openMode, 2);
//        }
//
        @Override
        public <T> T accept(RedirectTargetVisitor<T> visitor) {
            T result = visitor.visit(this);
            return result;
        }
    }

    public record RedirectTargetProcessSubstitution(CmdOp cmdOp) implements RedirectTarget {

        public RedirectTargetProcessSubstitution {
            Objects.requireNonNull(cmdOp);
        }

        public static RedirectTargetFile fileFromStdIn(String file) {
            return new RedirectTargetFile(file);
        }
//
//        public static RedirectTargetFile fileToStdOut(String file, OpenMode openMode) {
//            return new RedirectTargetFile(file, openMode, 1);
//        }
//
//        public static RedirectTargetFile fileToStdErr(String file, OpenMode openMode) {
//            return new RedirectTargetFile(file, openMode, 2);
//        }
//
        @Override
        public <T> T accept(RedirectTargetVisitor<T> visitor) {
            T result = visitor.visit(this);
            return result;
        }
    }

}
