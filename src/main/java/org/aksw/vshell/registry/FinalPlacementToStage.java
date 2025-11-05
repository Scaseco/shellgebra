package org.aksw.vshell.registry;

import java.util.Objects;

import org.aksw.shellgebra.algebra.cmd.op.CmdOp;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpExec;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpGroup;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpPipeline;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpVar;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpVisitor;
import org.aksw.shellgebra.algebra.cmd.transform.FileMapper;
import org.aksw.shellgebra.exec.Stage;
import org.aksw.shellgebra.exec.Stages;
import org.aksw.shellgebra.exec.model.ExecSiteCurrentHost;
import org.aksw.shellgebra.exec.model.ExecSiteCurrentJvm;
import org.aksw.shellgebra.exec.model.ExecSiteDockerImage;
import org.aksw.shellgebra.exec.model.ExecSiteVisitor;

public class FinalPlacementToStage {
    private ExecSiteResolver execSiteResolver;

    public FinalPlacementToStage(ExecSiteResolver execSiteResolver) {
        super();
        this.execSiteResolver = Objects.requireNonNull(execSiteResolver);
    }

    public static FinalPlacementToStage create(ExecSiteResolver execSiteResolver) {
        return new FinalPlacementToStage(execSiteResolver);
    }

    public Stage toStage(FinalPlacement placement) {
        Worker worker = new Worker(execSiteResolver);



        return null;
    }

    public static class Worker
        implements CmdOpVisitor<Stage>
    {
        private ExecSiteResolver execSiteResolver;

        public Worker(ExecSiteResolver execSiteResolver) {
            super();
            this.execSiteResolver = execSiteResolver;
        }

        @Override
        public Stage visit(CmdOpExec op) {


            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Stage visit(CmdOpPipeline op) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Stage visit(CmdOpGroup op) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Stage visit(CmdOpVar op) {
            // TODO Auto-generated method stub
            return null;
        }

    }

    static class ExecSiteToStage
        implements ExecSiteVisitor<Stage> {

        private CmdOp cmdOp;
        private FileMapper fileMapper;

        public ExecSiteToStage(CmdOp op) {
            super();
            this.cmdOp = op;
        }

        @Override
        public Stage visit(ExecSiteDockerImage execSite) {
            String imageRef = execSite.imageRef();
            Stages.docker(imageRef, cmdOp, fileMapper);

        }

        @Override
        public Stage visit(ExecSiteCurrentHost execSite) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Stage visit(ExecSiteCurrentJvm execSite) {
            // TODO Auto-generated method stub
            return null;
        }
    }
}
