package org.aksw.shellgebra.registry.tool.model;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

//public class ToolInfoUnion
//    implements ToolInfo
//{
//    private String name;
//    private List<ToolInfo> toolInfos;
//
//    public ToolInfoUnion(String name, List<ToolInfo> toolInfos) {
//        this.name = name;
//        this.toolInfos = toolInfos;
//    }
//
//    @Override
//    public String getName() {
//        return name;
//    }
//
//    protected Stream<ToolInfo> streamToolInfos() {
//        return toolInfos.stream();
//    }
//
//    @Override
//    public Stream<String> getAbsenceInDockerImages() {
//        return streamToolInfos()
//            .flatMap(ToolInfo::getAbsenceInDockerImages)
//            .distinct();
//            // .flatMap(Collection::stream);
//    }
//
//    @Override
//    public Optional<Boolean> getAbsentOnHost() {
//        return streamToolInfos()
//            .map(ToolInfo::getAbsentOnHost)
//            .flatMap(Optional::stream)
//            .findFirst();
//    }
//
//    @Override
//    public CommandTargetInfo findCommandByImage(String imageName) {
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//    @Override
//    public CommandTargetInfo findCommandOnHost() {
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//    @Override
//    public Stream<CommandTargetInfo> list() {
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//    @Override
//    public Optional<CommandTargetInfo> getCommand(String commandPath) {
//        new CommandTargetInfoUnion();
//    }
//
//}
