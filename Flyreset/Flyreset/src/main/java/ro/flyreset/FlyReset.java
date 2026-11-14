package ro.flyreset;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public final class FlyReset implements ModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("flyreset");
    private static final int DELAY_TICKS = 40;
    private static final Map<UUID, Integer> PENDING = new HashMap<>();

    @Override
    public void onInitialize() {
        ServerPlayerEvents.JOIN.register(player ->
                PENDING.put(player.getUUID(), DELAY_TICKS));
        ServerPlayerEvents.LEAVE.register(player ->
                PENDING.remove(player.getUUID()));
        ServerTickEvents.END_SERVER_TICK.register(FlyReset::tick);
        LOGGER.info("FlyReset 2.0.0 loaded.");
    }

    private static void tick(MinecraftServer server) {
        Iterator<Map.Entry<UUID, Integer>> it = PENDING.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Integer> entry = it.next();
            int left = entry.getValue() - 1;
            if (left > 0) {
                entry.setValue(left);
                continue;
            }

            ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
            if (player != null) {
                String name = player.getName().getString();
                String command = "fly " + name + " false";
                try {
                    server.getCommands().performPrefixedCommand(
                            server.createCommandSourceStack(), command);
                    LOGGER.info("Executed '{}' for {}.", command, name);
                } catch (Exception e) {
                    LOGGER.error("Failed to execute '{}' for {}.", command, name, e);
                }
            }
            it.remove();
        }
    }
}
