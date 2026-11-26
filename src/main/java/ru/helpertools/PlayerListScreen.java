package ru.helpertools;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class PlayerListScreen extends Screen {
    private EditBox search;
    private final List<String> filtered = new ArrayList<>();
    private String selected = null;
    private int scroll = 0;
    private int panelLeft, panelTop, panelWidth, panelHeight;

    public PlayerListScreen() {
        super(Component.literal("Helper Tools"));
    }

    @Override
    protected void init() {
        panelWidth = Math.min(430, this.width - 30);
        panelHeight = Math.min(330, this.height - 30);
        panelLeft = (this.width - panelWidth) / 2;
        panelTop = (this.height - panelHeight) / 2;

        search = new EditBox(this.font, panelLeft + 18, panelTop + 48, panelWidth - 36, 20,
                Component.literal("Поиск игрока"));
        search.setHint(Component.literal("Введи часть ника..."));
        search.setMaxLength(32);
        search.setResponder(s -> {
            scroll = 0;
            refreshPlayers();
        });
        addRenderableWidget(search);

        addRenderableWidget(net.minecraft.client.gui.components.Button.builder(
                Component.literal("Выдать мут"), b -> {
                    if (selected != null) Minecraft.getInstance().setScreen(new MuteScreen(selected));
                }).bounds(panelLeft + 18, panelTop + panelHeight - 38, panelWidth - 36, 22).build());

        refreshPlayers();
        setInitialFocus(search);
    }

    private void refreshPlayers() {
        filtered.clear();
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() == null) return;
        String query = search == null ? "" : search.getValue().trim().toLowerCase(Locale.ROOT);
        mc.getConnection().getOnlinePlayers().stream()
                .map(PlayerInfo::getProfile)
                .map(profile -> profile.getName())
                .filter(name -> name != null && name.toLowerCase(Locale.ROOT).contains(query))
                .sorted(Comparator.naturalOrder())
                .forEach(filtered::add);
        if (scroll > Math.max(0, filtered.size() - visibleRows())) {
            scroll = Math.max(0, filtered.size() - visibleRows());
        }
    }

    private int visibleRows() {
        return Math.max(1, (panelHeight - 112) / 22);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        g.fill(0, 0, this.width, this.height, 0x99000000);
        g.fill(panelLeft, panelTop, panelLeft + panelWidth, panelTop + panelHeight, 0xF0181D2A);
        g.fill(panelLeft, panelTop, panelLeft + panelWidth, panelTop + 3, 0xFF6C8CFF);
        g.drawString(font, "HELPER TOOLS", panelLeft + 18, panelTop + 15, 0xFFFFFFFF, true);
        g.drawString(font, "Игроков найдено: " + filtered.size(), panelLeft + 18, panelTop + 34, 0xFFB9C4D8, false);

        int y = panelTop + 76;
        int rows = visibleRows();
        for (int i = scroll; i < Math.min(filtered.size(), scroll + rows); i++) {
            String name = filtered.get(i);
            int rowY = y + (i - scroll) * 22;
            boolean active = name.equals(selected);
            int bg = active ? 0xFF354A75 : (mouseX >= panelLeft + 16 && mouseX <= panelLeft + panelWidth - 16
                    && mouseY >= rowY && mouseY < rowY + 20 ? 0xFF29364D : 0xFF202838);
            g.fill(panelLeft + 16, rowY, panelLeft + panelWidth - 16, rowY + 20, bg);
            g.fill(panelLeft + 23, rowY + 7, panelLeft + 29, rowY + 13, 0xFF59D98E);
            g.drawString(font, name, panelLeft + 38, rowY + 6, 0xFFFFFFFF, false);
        }
        if (filtered.isEmpty()) {
            g.drawCenteredString(font, "Совпадений нет", width / 2, panelTop + 105, 0xFFB9C4D8);
        }
        if (selected != null) {
            g.drawString(font, "Выбран: " + selected, panelLeft + 18, panelTop + panelHeight - 54, 0xFF9FB5FF, false);
        } else {
            g.drawString(font, "Выбери игрока из списка", panelLeft + 18, panelTop + panelHeight - 54, 0xFF9AA6BA, false);
        }
        super.render(g, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && mouseX >= panelLeft + 16 && mouseX <= panelLeft + panelWidth - 16
                && mouseY >= panelTop + 76 && mouseY < panelTop + panelHeight - 60) {
            int index = scroll + (int) ((mouseY - (panelTop + 76)) / 22);
            if (index >= 0 && index < filtered.size()) {
                selected = filtered.get(index);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scroll = Math.max(0, Math.min(Math.max(0, filtered.size() - visibleRows()), scroll + (verticalAmount > 0 ? -2 : 2)));
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(null);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
