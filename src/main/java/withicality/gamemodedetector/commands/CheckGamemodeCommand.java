package withicality.gamemodedetector.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.world.level.GameType;
import withicality.gamemodedetector.GamemodeDetectorClient;

import java.util.Collection;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.*;
import static dev.xpple.clientarguments.arguments.CGameProfileArgument.*;

public class CheckGamemodeCommand {

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("checkgamemode")
                .executes(context -> {
                    GamemodeDetectorClient.send(GamemodeDetectorClient.getMessage(), false);
                    return 1;
                })
                .then(argument("player", gameProfile())
                        .executes(context -> {
                            Collection<GameProfile> profiles = getProfileArgument(context, "player");
                            for (GameProfile profile : profiles) {
                                GameType gamemode = GamemodeDetectorClient.getGamemode(profile.id());
                                GamemodeDetectorClient.send(GamemodeDetectorClient.getMessage(profile, gamemode), false);
                            }
                            return 1;
                        })
                )
        );
    }
}