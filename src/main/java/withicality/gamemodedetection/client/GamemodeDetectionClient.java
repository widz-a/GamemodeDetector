package withicality.gamemodedetection.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;
import withicality.gamemodedetection.ChatColor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Environment(EnvType.CLIENT)
public class GamemodeDetectionClient implements ClientModInitializer {
    private  final Map<UUID, GameMode> gamemodes = new HashMap<>();
    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ClientPlayNetworkHandler network = client.getNetworkHandler();
            if (network == null) {
                gamemodes.clear();
                return;
            }
            network.getPlayerList().forEach(p -> {
                UUID uuid = p.getProfile().getId();
                GameMode n = p.getGameMode();
                GameMode last = gamemodes.get(uuid);

                LocalDateTime now = LocalDateTime.now();
                gamemodes.put(uuid, n);
                if (last != null && last.equals(n)) return;

                ClientPlayerEntity player = client.player;
                if (player == null) return;
                String time = String.join(":", new String[] {String.valueOf(now.getHour()), String.valueOf(now.getMinute()), String.valueOf(now.getSecond())});

                player.sendMessage(Text.of(ChatColor.translateAlternateColorCodes('&',
                        "&b["+ time + "] &fPlayer &7" + p.getProfile().getName() + " &fis in &7" + n.name() + "&f."
                )));
            });
        });
    }
}
