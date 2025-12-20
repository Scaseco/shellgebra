package org.aksw.vshell.registry;

//public class JvmCmdTest
//    implements JvmCommand
//{
//    @Override
//    public ArgsCommand parseArgs(String... args) {
//        ArgsCommand model = ArgsCommand.parse(args).model();
//        return model;
//    }
//
//    @Override
//    public int run(JvmExecCxt cxt, Argv argv) {
//        ArgsCommand model;
//
//        int exitValue = 0;
//        try {
//            model = parseArgs(argv.argsToArray());
//        } catch (Exception e) {
//            e.printStackTrace(cxt.err().printStream());
//            exitValue = 2;
//            return exitValue;
//        }
//
//        String testName = model.getE();
//        if (testName == null) {
//            throw new RuntimeException("Only -e option supported yet.");
//        }
//
//        JvmCommandRegistry reg = cxt.context().getJvmCmdRegistry();
//        boolean isPresent = reg.get(testName).isPresent();
//        exitValue = isPresent ? 0 : 1;
//        return exitValue;
//    }
//}
