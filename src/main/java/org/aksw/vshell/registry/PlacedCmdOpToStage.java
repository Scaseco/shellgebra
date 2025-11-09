package org.aksw.vshell.registry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.aksw.shellgebra.algebra.cmd.arg.CmdArg;
import org.aksw.shellgebra.algebra.cmd.arg.CmdArgCmdOp;
import org.aksw.shellgebra.algebra.cmd.arg.CmdArgWord;
import org.aksw.shellgebra.algebra.cmd.arg.StringEscapeType;
import org.aksw.shellgebra.algebra.cmd.arg.Token;
import org.aksw.shellgebra.algebra.cmd.arg.Token.TokenPath;
import org.aksw.shellgebra.algebra.cmd.op.CmdOp;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpVar;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpVisitor;
import org.aksw.shellgebra.algebra.cmd.op.CmdOps;
import org.aksw.shellgebra.algebra.cmd.op.placed.PlacedCmdOp;
import org.aksw.shellgebra.algebra.cmd.op.placed.PlacedCmdOp.PlacedCmd;
import org.aksw.shellgebra.algebra.cmd.op.placed.PlacedCmdOp.PlacedCmdOpVisitor;
import org.aksw.shellgebra.algebra.cmd.op.placed.PlacedCmdOp.PlacedGroup;
import org.aksw.shellgebra.algebra.cmd.op.placed.PlacedCmdOp.PlacedPipeline;
import org.aksw.shellgebra.algebra.cmd.transform.FileMapper;
import org.aksw.shellgebra.algebra.cmd.transformer.CmdArgTransformBase;
import org.aksw.shellgebra.algebra.cmd.transformer.CmdOpTransformer;
import org.aksw.shellgebra.exec.Stage;
import org.aksw.shellgebra.exec.Stages;
import org.aksw.shellgebra.exec.model.ExecSite;
import org.aksw.shellgebra.exec.model.ExecSiteCurrentHost;
import org.aksw.shellgebra.exec.model.ExecSiteCurrentJvm;
import org.aksw.shellgebra.exec.model.ExecSiteDockerImage;
import org.aksw.shellgebra.exec.model.ExecSiteVisitor;

import com.google.common.io.ByteSource;

public class PlacedCmdOpToStage {

    private FileMapper fileMapper;
    private ExecSiteResolver resolver;

    public PlacedCmdOpToStage(FileMapper fileMapper, ExecSiteResolver resolver) {
        super();
        this.fileMapper = Objects.requireNonNull(fileMapper);
        this.resolver = Objects.requireNonNull(resolver);
    }

//    private JvmCommandRegistry jvmCmdRegistry;

//    public PlacedCmdOpToStage(JvmCommandRegistry jvmCmdRegistry, FileMapper fileMapper) {
//        super();
//        this.jvmCmdRegistry = jvmCmdRegistry;
//        this.fileMapper = fileMapper;
//    }

    public static PlacedCmdOpToStage of(FileMapper fileMapper, ExecSiteResolver resolver) {
        return new PlacedCmdOpToStage(fileMapper, resolver);
    }

    public Stage toStage(FinalPlacement placement) {
        Map<CmdOpVar, PlacedCmd> varToPlacement = placement.placements();
        PlacedCmdOpVisitorToStage visitor = new PlacedCmdOpVisitorToStage(varToPlacement, fileMapper, resolver);
        PlacedCmd root = placement.cmdOp();
        Stage result = root.accept(visitor);
        return result;
    }

    public static class PlacedCmdOpVisitorToStage
        implements PlacedCmdOpVisitor<Stage>
    {
        private Map<CmdOpVar, PlacedCmd> varToPlacement;
        private FileMapper fileMapper;
        private ExecSiteResolver resolver;

        public PlacedCmdOpVisitorToStage(Map<CmdOpVar, PlacedCmd> varToPlacement, FileMapper fileMapper, ExecSiteResolver resolver) {
            super();
            this.varToPlacement = varToPlacement;
            this.fileMapper = fileMapper;
            this.resolver = resolver;
        }

//        private JvmCommandRegistry jvmCmdRegistry;
//        private FileMapper fileMapper;

//        public PlacedCmdOpVisitorToStage(Map<CmdOpVar, PlacedCmd> varToPlacement, JvmCommandRegistry jvmCmdRegistry, FileMapper fileMapper) {
//            super();
//            this.varToPlacement = varToPlacement;
//            this.jvmCmdRegistry = jvmCmdRegistry;
//            this.fileMapper = fileMapper;
//        }

        public Stage resolveVar(CmdOpVar cmdOpVar) {
            PlacedCmd placedCmd = varToPlacement.get(cmdOpVar);
            if (placedCmd == null) {
                throw new RuntimeException("Unresolvable variable: " + cmdOpVar);
            }

            Stage result = placedCmd.accept(this);
            return result;
        }

        @Override
        public Stage visit(PlacedCmd op) {
            CmdOp cmdOp = op.cmdOp();
            Set<CmdOpVar> cmdOpVars = CmdOps.accVars(cmdOp);

            // For each variable: Generate the stage for its definition.
            // Then substitute the variable with a named pipe for that stage.

            Map<CmdOpVar, Stage> varToStage = new HashMap<>();
            // Map<CmdOpVar, String> varToFileName = new HashMap<>();
            // Map<CmdOpVar, FileWriterTask> varToFileWriterTask = new HashMap<>();
            Map<CmdOpVar, TokenPath> tokenToPath = new HashMap<>();
            for (CmdOpVar v : cmdOpVars) {
                PlacedCmd childPlacement = varToPlacement.get(v);

                // PlacedCmdOpToStage subVisitor = new PlacedCmdOpToStage();
                Stage stage = childPlacement.accept(this);

                String pathStr = fileMapper.allocateTempFilename("tmpfile", "pipe");
                tokenToPath.put(v, new TokenPath(pathStr));
                // CmdArg cmdArgPath = CmdArg.ofPathString(pathStr);

                // stage.for
                varToStage.put(v, stage);
                // varToPlacement.put(v, op)
            }

            CmdOp finalCmdOp = CmdOpTransformer.transform(cmdOp, null, new CmdArgTransformBase() {
                @Override
                public CmdArg transform(CmdArgCmdOp token, CmdOp subOp) {
                    Token t = subOp instanceof CmdOpVar v
                        ? tokenToPath.get(v)
                        : null;
                    CmdArg r = t == null
                        ? super.transform(token, subOp)
                        : new CmdArgWord(StringEscapeType.ESCAPED, t);
                    return r;
                }
            }, null);

            ExecSite execSite = op.getExecSite();
            Stage stage = execSite.accept(new ExecSiteVisitor<Stage>() {
                @Override
                public Stage visit(ExecSiteDockerImage execSite) {



                    String imageRef = execSite.imageRef();
                    Stage r = Stages.docker(imageRef, finalCmdOp, fileMapper, PlacedCmdOpVisitorToStage.this::resolveVar);
                    return r;
                }

                @Override
                public Stage visit(ExecSiteCurrentHost execSite) {
                    Stage r = Stages.host(finalCmdOp);
                    return r;
                }

                @Override
                public Stage visit(ExecSiteCurrentJvm execSite) {
                    // CommandCatalog cmdCatalog = resolver.getCommandCatalog();
                    JvmCommandRegistry jvmCmdRegistry = resolver.getJvmCmdRegistry();
                    // cmdCatalog.get(finalCmdOp

                    CmdOpVisitor<Stage> cmdOpVisitorToStage = new CmdOpVisitorExecJvm(resolver, PlacedCmdOpVisitorToStage.this::resolveVar);
                    Stage r = finalCmdOp.accept(cmdOpVisitorToStage);
                    return r;
                }
            });
            return stage;
        }

        /**
         * Produces a stage that concatenates the output of all stages.
         * Only supports ExecSiteCurrentJvm which concats the ByteSources.
         * To use host or docker, groups and pipelines should be transformed to PlaceCmd where the cmd is a bash command
         * that implements the group/pipeline.
         */
        @Override
        public Stage visit(PlacedGroup op) {
            List<PlacedCmdOp> subOps = op.subOps();
            List<Stage> stages = toStages(subOps);

            ExecSite execSite = op.getExecSite();
            Stage stage = execSite.accept(new ExecSiteVisitor<Stage>() {
                @Override
                public Stage visit(ExecSiteDockerImage execSite) {
                    throw new UnsupportedOperationException();
    //                String imageRef = execSite.imageRef();
    //                Stage r = Stages.docker(imageRef, cmdOp, fileMapper);
    //                return r;
                }

                // Could turn all output into named pipes and use bash process to concat it.
                // /bin/cat < namedPipe1 < namedPipe2
                @Override
                public Stage visit(ExecSiteCurrentHost execSite) {
                    throw new UnsupportedOperationException();
                    // Stage r = Stages.host(cmdOp);
                    // return r;
                }

                @Override
                public Stage visit(ExecSiteCurrentJvm execSite) {
                    List<ByteSource> byteSources = new ArrayList<>(stages.size());
                    for (Stage stage : stages) {
                        ByteSource bs = stage.fromNull().toByteSource();
                        byteSources.add(bs);
                    }
                    ByteSource concat = ByteSource.concat(byteSources);
                    // Note: This stage ignores input data and serves the group.
                    //   TODO: Perhaps attach in to first member of the group? - It's a corner case.
                    Stage r = Stages.javaIn(in -> concat.openStream());
                    return r;
                }
            });
            return stage;
        }

        protected List<Stage> toStages(List<PlacedCmdOp> subOps) {
            List<Stage> result = new ArrayList<>(subOps.size());
            for (PlacedCmdOp subOp : subOps) {
                Stage stage = subOp.accept(this);
                result.add(stage);
            }
            return result;
        }

        @Override
        public Stage visit(PlacedPipeline op) {
            List<PlacedCmdOp> subOps = op.subOps();
            List<Stage> stages = toStages(subOps);
            Stage result = Stages.pipeline(stages);
            return result;
        }
    }
}
