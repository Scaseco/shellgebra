package org.aksw.vshell.registry;

// Caching is better handled on the locator level instead of the catalog level.

//public class CommandCatalogCached
//    implements CommandCatalog
//{
//    private CommandCatalog baseRegistry;
//    private CommandRegistry dynamicRegistry;
//
//    public CommandCatalogCached(CommandCatalog baseRegistry, CommandRegistry dynamicRegistry) {
//        super();
//        this.baseRegistry = Objects.requireNonNull(baseRegistry);
//        this.dynamicRegistry = Objects.requireNonNull(dynamicRegistry);
//    }
//
//    @Override
//    public Multimap<ExecSite, CommandBinding> get(String virtualCommandName) {
//        Multimap<ExecSite, CommandBinding> result = dynamicRegistry.getKnownExecSites(virtualCommandName).orElse(null);
//        if (result == null) {
//            result = baseRegistry.get(virtualCommandName);
//            dynamicRegistry.putAll(virtualCommandName, result);
//        }
//        return result;
//    }
//}
