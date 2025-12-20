package org.aksw.vshell.registry;

//public class FinalPlacementToStage {
//    private ExecSiteResolver execSiteResolver;
//
//    public FinalPlacementToStage(ExecSiteResolver execSiteResolver) {
//        super();
//        this.execSiteResolver = Objects.requireNonNull(execSiteResolver);
//    }
//
//    public static FinalPlacementToStage create(ExecSiteResolver execSiteResolver) {
//        return new FinalPlacementToStage(execSiteResolver);
//    }
//
//    public Stage toStage(FinalPlacement placement) {
//        CmdOpVisitorFinalPlacementToStage worker = new CmdOpVisitorFinalPlacementToStage(execSiteResolver);
//
//
//        return null;
//    }
//
//    static class ExecSiteToStage
//        implements ExecSiteVisitor<Stage> {
//
//        private CmdOp cmdOp;
//        private FileMapper fileMapper;
//
//        public ExecSiteToStage(CmdOp op) {
//            super();
//            this.cmdOp = op;
//        }
//
//        @Override
//        public Stage visit(ExecSiteDockerImage execSite) {
//            String imageRef = execSite.imageRef();
//            Stage r = Stages.docker(imageRef, cmdOp, fileMapper);
//            return r;
//        }
//
//        @Override
//        public Stage visit(ExecSiteCurrentHost execSite) {
//            Stage r = Stages.host(cmdOp);
//            return r;
//        }
//
//        @Override
//        public Stage visit(ExecSiteCurrentJvm execSite) {
//
//            // JvmCommandRegistry.get().
//            Stage r = Stages.javaIn(null);
//            return r;
//        }
//    }
//
//
////    public static class CmdOpVisitorFinalPlacementToStage
////        implements CmdOpVisitor<Stage>
////    {
////        private ExecSiteResolver execSiteResolver;
////
////        public CmdOpVisitorFinalPlacementToStage(ExecSiteResolver execSiteResolver) {
////            super();
////            this.execSiteResolver = execSiteResolver;
////        }
////
////        @Override
////        public Stage visit(CmdOpExec op) {
////
////
////            // TODO Auto-generated method stub
////            return null;
////        }
////
////        @Override
////        public Stage visit(CmdOpPipeline op) {
////            // TODO Auto-generated method stub
////            return null;
////        }
////
////        @Override
////        public Stage visit(CmdOpGroup op) {
////            // TODO Auto-generated method stub
////            return null;
////        }
////
////        @Override
////        public Stage visit(CmdOpVar op) {
////            // TODO Auto-generated method stub
////            return null;
////        }
////
////    }
//}
