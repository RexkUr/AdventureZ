package net.adventurez.mixin.compat;



import java.util.List;
import java.util.Set;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import net.fabricmc.loader.api.FabricLoader;

public class AdventurezMixinPlugin implements IMixinConfigPlugin {

    private static boolean isSinytraEnvironment = false;
    private static boolean isApotheosisPresent = false;

    @Override
    public void onLoad(String mixinPackage) {
        // Detectar si estamos ejecutando en Sinytra Connector
        try {
            Class.forName("org.sinytra.connector.ConnectorEarlyLoader");
            isSinytraEnvironment = true;
            Class.forName("dev.shadowsoffire.apothic_enchanting.mixin.AnvilMenuMixin");
            isApotheosisPresent = true;
        } catch (ClassNotFoundException e) {
            isSinytraEnvironment = false;
            isApotheosisPresent = false;
        }
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        // Deshabilitar AnvilScreenHandlerCompatMixin en entorno Sinytra (Fabric sobre NeoForge)
        if (mixinClassName.contains("AnvilScreenHandlerCompatMixin")) {
            if (isSinytraEnvironment) {
                return false; // No aplicar en Sinytra
            }
            if (FabricLoader.getInstance().isModLoaded("overenchanted")) {
                return false; // No aplicar si overenchanted está presente
            }
        }

        if (mixinClassName.equals("net.adventurez.mixin.compat.AnvilScreenHandlerCompatMixin")) {
            return !isApotheosisPresent;
        }

        // Lógica original para EnchantedBookItemMixin
        return !mixinClassName.contains("EnchantedBookItemMixin") || FabricLoader.getInstance().isModLoaded("overenchanted");
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

}
