package withicality.gamemodedetector.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;
import withicality.gamemodedetector.ChatColor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Environment(EnvType.CLIENT)
public class GamemodeDetectorClient implements ClientModInitializer {
    private final Map<UUID, GameMode> gamemodes = new HashMap<>();
    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ClientPlayNetworkHandler network = client.getNetworkHandler();
            if (network == null) {
                gamemodes.clear();
                return;
            }

            network.getPlayerList().forEach(p -> {
                LocalDateTime t = LocalDateTime.now();
                String time = String.format("%02d:%02d:%02d", t.getHour(), t.getMinute(), t.getSecond());
                UUID uuid = p.getProfile().getId();

                GameMode now = p.getGameMode();
                GameMode last = gamemodes.get(uuid);
                gamemodes.put(uuid, now);

                if (last != null && last.equals(now)) return;
                send("&b["+ time + "] &fPlayer &7" + p.getProfile().getName() + " &fis in &7" + now.name() + "&f.");
            });
        });
    }

    private void send(String message) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return;

        player.sendMessage(Text.of(ChatColor.translateAlternateColorCodes('&', message)));
    }
}
