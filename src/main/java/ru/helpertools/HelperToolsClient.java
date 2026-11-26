package ru.helpertools;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public final class HelperToolsClient implements ClientModInitializer {
    private static KeyMapping openMenuKey;

    @Override
    public void onInitializeClient() {
        openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.helpertools.open",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                KeyMapping.Category.MISC
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openMenuKey.consumeClick()) {
                if (client.player != null && client.screen == null) {
                    client.setScreen(new PlayerListScreen());
                } else if (client.player != null && client.screen instanceof PlayerListScreen) {
                    client.setScreen(null);
                }
            }
        });
    }

    public static void sendMute(String player, String duration, String reason) {
        Minecraft client = Minecraft.getInstance();
        if (client.getConnection() == null || client.player == null) return;

        String safePlayer = player.replaceAll("[^A-Za-z0-9_]", "");
        if (safePlayer.isBlank()) return;

        client.getConnection().sendCommand("mute " + safePlayer + " " + duration + " " + reason);
    }
}
