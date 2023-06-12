package withicality.gamemodedetector.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.world.GameMode;
import withicality.gamemodedetector.GamemodeDetectorClient;

import java.util.Collection;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;
import static dev.xpple.clientarguments.arguments.CGameProfileArgumentType.*;

public class CheckGamemodeCommand {

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("checkgamemode")
                .executes(context -> {
                    GamemodeDetectorClient.send(GamemodeDetectorClient.getMessage(), false);
                    return 1;
                })
                .then(argument("player", gameProfile())
                        .executes(context -> {
                            Collection<GameProfile> profiles = getCProfileArgument(context, "player");
                            for (GameProfile profile : profiles) {
                                GameMode gamemode = GamemodeDetectorClient.getGamemode(profile.getId());
                                GamemodeDetectorClient.send(GamemodeDetectorClient.getMessage(profile, gamemode), false);
                            }
                            return 1;
                        })
                )
        );
    }
}