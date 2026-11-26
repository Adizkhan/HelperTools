package ru.helpertools;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public final class MuteScreen extends Screen {
    private static final List<String> DURATIONS = List.of("3h", "4h", "6h", "12h", "3d", "5d", "15d");
    private static final List<String> REASONS = List.of(
            "Оск пов",
            "Аморальное поведение",
            "Флуд",
            "Оскорбление родных",
            "Некорректное упоминание родных",
            "Критика проекта",
            "flood$",
            "Капс",
            "Организация флуда",
            "Организация массового флуда",
            "Участник массового флуда",
            "Музыка и посторонние звуки в голосовом чате"
    );

    private final String playerName;
    private int durationIndex = 0;
    private int reasonIndex = 0;
    private int left, top, widthPanel;

    public MuteScreen(String playerName) {
        super(Component.literal("Выдача мута"));
        this.playerName = playerName;
    }

    @Override
    protected void init() {
        widthPanel = Math.min(420, width - 30);
        left = (width - widthPanel) / 2;
        top = (height - 230) / 2;

        addRenderableWidget(Button.builder(Component.literal("← Назад"),
                b -> Minecraft.getInstance().setScreen(new PlayerListScreen()))
                .bounds(left + 18, top + 18, 90, 20).build());

        addRenderableWidget(Button.builder(Component.literal("−"), b -> {
            durationIndex = (durationIndex + DURATIONS.size() - 1) % DURATIONS.size();
            rebuildWidgets();
        }).bounds(left + 18, top + 92, 34, 24).build());

        addRenderableWidget(Button.builder(Component.literal("+"), b -> {
            durationIndex = (durationIndex + 1) % DURATIONS.size();
            rebuildWidgets();
        }).bounds(left + widthPanel - 52, top + 92, 34, 24).build());

        addRenderableWidget(Button.builder(Component.literal("−"), b -> {
            reasonIndex = (reasonIndex + REASONS.size() - 1) % REASONS.size();
            rebuildWidgets();
        }).bounds(left + 18, top + 158, 34, 24).build());

        addRenderableWidget(Button.builder(Component.literal("+"), b -> {
            reasonIndex = (reasonIndex + 1) % REASONS.size();
            rebuildWidgets();
        }).bounds(left + widthPanel - 52, top + 158, 34, 24).build());

        addRenderableWidget(Button.builder(Component.literal("ВЫДАТЬ МУТ"), b -> {
            HelperToolsClient.sendMute(playerName, DURATIONS.get(durationIndex), REASONS.get(reasonIndex));
            Minecraft.getInstance().setScreen(null);
        }).bounds(left + 18, top + 194, widthPanel - 36, 24).build());
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        g.fill(0, 0, this.width, this.height, 0x99000000);
        g.fill(left, top, left + widthPanel, top + 230, 0xF0181D2A);
        g.fill(left, top, left + widthPanel, top + 3, 0xFF6C8CFF);
        g.drawString(font, "ВЫДАЧА НАКАЗАНИЯ", left + 18, top + 52, 0xFFFFFFFF, true);
        g.drawString(font, "Игрок: " + playerName, left + 18, top + 72, 0xFFB9C4D8, false);
        g.drawString(font, "Срок мута", left + 18, top + 82, 0xFF9FB5FF, false);
        g.drawCenteredString(font, DURATIONS.get(durationIndex), width / 2, top + 100, 0xFFFFFFFF);
        g.drawString(font, "Причина", left + 18, top + 137, 0xFF9FB5FF, false);
        String reason = REASONS.get(reasonIndex);
        String shown = font.width(reason) > widthPanel - 110 ? font.substrByWidth(reason, widthPanel - 120) : reason;
        g.drawCenteredString(font, shown, width / 2, top + 166, 0xFFFFFFFF);
        super.render(g, mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            Minecraft.getInstance().setScreen(new PlayerListScreen());
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
