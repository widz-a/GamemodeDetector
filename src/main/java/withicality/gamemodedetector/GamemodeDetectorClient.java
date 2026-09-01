package withicality.gamemodedetector;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.CommandDispatcher;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.AutoConfigClient;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.GameType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import withicality.gamemodedetector.commands.CheckGamemodeCommand;
import withicality.gamemodedetector.menu.TheConfig;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Environment(EnvType.CLIENT)
public class GamemodeDetectorClient implements ClientModInitializer {
    private static GamemodeDetectorClient INSTANCE;
    private TheConfig config;
    public static final Logger LOGGER = LoggerFactory.getLogger("Gamemode Detector");

    // KeyMapping categories are no longer free-form strings; they are registered
    // Identifier-keyed records whose label comes from Identifier.toLanguageKey("key.categories").
    private static final KeyMapping.Category KEY_CATEGORY =
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath("gamemodedetector", "main"));

    private final KeyMapping kb = KeyMappingHelper.registerKeyMapping(
            new KeyMapping("key.gamemodedetector", InputConstants.UNKNOWN.getValue(), KEY_CATEGORY));

    private static final Map<UUID, GameType> gamemodes = new HashMap<>();

    @Override
    public void onInitializeClient() {
        AutoConfig.register(TheConfig.class, JanksonConfigSerializer::new);
        config = AutoConfig.getConfigHolder(TheConfig.class).getConfig();
        INSTANCE = this;

        ClientCommandRegistrationCallback.EVENT.register(this::registerCommand);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (kb.consumeClick()) {
                client.setScreen(AutoConfigClient.getConfigScreen(TheConfig.class, client.screen).get());
            }
            ClientPacketListener network = client.getConnection();
            if (network == null) {
                gamemodes.clear();
                return;
            }

            network.getOnlinePlayers().forEach(p -> {
                UUID uuid = p.getProfile().id();
                GameType now = p.getGameMode();
                GameType last = gamemodes.get(uuid);
                gamemodes.put(uuid, now);

                if (last != null && last.equals(now)) return;
                if (!config.gamemode.enabled) return;

                send(getMessage(p.getProfile(), now), config.gamemode.actionbar);
            });
        });
    }


    public static GameType getGamemode(UUID uuid) {
        return gamemodes.get(uuid);
    }

    public static String getMessage(GameProfile profile, GameType gameMode) {
        return getINSTANCE().config.gamemode.message
                .replaceAll("%player%", profile.name())
                .replaceAll("%gamemode%", gameMode.name());
    }

    public static String getMessage() {
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.player == null) return "";
        GameProfile profile = client.player.getGameProfile();
        return getMessage(profile, getGamemode(profile.id()));
    }

    public static GamemodeDetectorClient getINSTANCE() {
        return INSTANCE;
    }

    private void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        CheckGamemodeCommand.register(dispatcher);
    }

    public static Component format(String og) {
        LocalDateTime t = LocalDateTime.now();
        String message = ChatColor.translateAlternateColorCodes('&', og)

                .replaceAll("%time%", String.format("%02d:%02d:%02d", t.getHour(), t.getMinute(), t.getSecond()));
        return Component.literal(message);
    }

    public static void send(String message, boolean actionbar) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;

        // Yarn's ClientPlayerEntity.sendMessage(Text, boolean) is gone: the actionbar and
        // the chat log are now two distinct HUD calls.
        if (actionbar) {
            client.gui.setOverlayMessage(format(message), false);
        } else {
            client.gui.getChat().addClientSystemMessage(format(message));
        }
    }
}
