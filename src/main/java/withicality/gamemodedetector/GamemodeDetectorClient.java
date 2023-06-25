package withicality.gamemodedetector;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;
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
    private final KeyBinding kb = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.gamemodedetector", InputUtil.UNKNOWN_KEY.getCode(), "key.gamemodedetector"));

    private static final Map<UUID, GameMode> gamemodes = new HashMap<>();

    @Override
    public void onInitializeClient() {
        AutoConfig.register(TheConfig.class, JanksonConfigSerializer::new);
        config = AutoConfig.getConfigHolder(TheConfig.class).getConfig();
        INSTANCE = this;

        ClientCommandRegistrationCallback.EVENT.register(this::registerCommand);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (kb.isPressed()) {
                client.setScreen(AutoConfig.getConfigScreen(TheConfig.class, client.currentScreen).get());
            }
            ClientPlayNetworkHandler network = client.getNetworkHandler();
            if (network == null) {
                gamemodes.clear();
                return;
            }

            network.getPlayerList().forEach(p -> {
                UUID uuid = p.getProfile().getId();
                GameMode now = p.getGameMode();
                GameMode last = gamemodes.get(uuid);
                gamemodes.put(uuid, now);

                if (last != null && last.equals(now)) return;
                if (!config.gamemode.enabled) return;

                send(getMessage(p.getProfile(), now), config.gamemode.actionbar);
            });
        });
    }


    public static GameMode getGamemode(UUID uuid) {
        return gamemodes.get(uuid);
    }

    public static String getMessage(GameProfile profile, GameMode gameMode) {
        return getINSTANCE().config.gamemode.message
                .replaceAll("%player%", profile.getName())
                .replaceAll("%gamemode%", gameMode.name());
    }

    public static String getMessage() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) return "";
        GameProfile profile = client.player.getGameProfile();
        return getMessage(profile, getGamemode(profile.getId()));
    }

    public static GamemodeDetectorClient getINSTANCE() {
        return INSTANCE;
    }

    private void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        CheckGamemodeCommand.register(dispatcher);
    }

    public static Text format(String og) {
        LocalDateTime t = LocalDateTime.now();
        String message = ChatColor.translateAlternateColorCodes('&', og)

                .replaceAll("%time%", String.format("%02d:%02d:%02d", t.getHour(), t.getMinute(), t.getSecond()));
        return Text.of(message);
    }

    public static void send(String message, boolean actionbar) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return;

        player.sendMessage(format(message), actionbar);
    }
}
