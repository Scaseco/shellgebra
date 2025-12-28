package org.aksw.shellgebra.exec;

import java.util.List;
import java.util.function.Function;

import org.testcontainers.containers.GenericContainer;

//public class CommandRunnerDocker
//    implements CommandRunner
//{
//    private String imageName;
//    private String entrypoint;
//    // private String[] commandPrefix;
//    private Function<List<String>, List<String>> shellCallTransform;
//    // private String[] command;
//
//
//
//	@Override
//	public Object call(String... argv) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//
//    public static int runCommand(String imageName, String entrypoint, String commandPrefix, String command) {
//        try (GenericContainer<?> container = new GenericContainer<>(imageName)
//                .withCreateContainerCmdModifier(cmd -> cmd.withEntrypoint(entrypoint))
//                .withCommand(commandPrefix, command)) {
//
//            container.start();
//            int exitCode = container.getCurrentContainerInfo().getState().getExitCodeLong().intValue();
//            return exitCode;
//        } catch (Exception e) {
//            return -1;
//        }
//    }
//}
