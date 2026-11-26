package ru.helpertools;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class PlayerListScreen extends Screen {
    private static final int ROWS = 8;
    private EditBox search;
    private final List<String> filtered = new ArrayList<>();
    private final List<Button> playerButtons = new ArrayList<>();
    private String selected;
    private int page;
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

        search = new EditBox(font, panelLeft + 18, panelTop + 48, panelWidth - 36, 20,
                Component.literal("Поиск игрока"));
        search.setHint(Component.literal("Введи часть ника..."));
        search.setMaxLength(32);
        search.setResponder(s -> {
            page = 0;
            refreshPlayers();
        });
        addRenderableWidget(search);

        for (int row = 0; row < ROWS; row++) {
            final int index = row;
            Button button = Button.builder(Component.literal(""), b -> {
                int playerIndex = page * ROWS + index;
                if (playerIndex < filtered.size()) {
                    selected = filtered.get(playerIndex);
                    refreshPlayers();
                }
            }).bounds(panelLeft + 16, panelTop + 76 + row * 22, panelWidth - 32, 20).build();
            playerButtons.add(addRenderableWidget(button));
        }

        addRenderableWidget(Button.builder(Component.literal("‹"), b -> {
            if (page > 0) page--;
            refreshPlayers();
        }).bounds(panelLeft + 18, panelTop + panelHeight - 39, 32, 22).build());

        addRenderableWidget(Button.builder(Component.literal("›"), b -> {
            if ((page + 1) * ROWS < filtered.size()) page++;
            refreshPlayers();
        }).bounds(panelLeft + 56, panelTop + panelHeight - 39, 32, 22).build());

        addRenderableWidget(Button.builder(Component.literal("Выдать мут"), b -> {
            if (selected != null) Minecraft.getInstance().setScreen(new MuteScreen(selected));
        }).bounds(panelLeft + panelWidth - 148, panelTop + panelHeight - 39, 130, 22).build());

        refreshPlayers();
        setInitialFocus(search);
    }

    private void refreshPlayers() {
        filtered.clear();
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() != null) {
            String query = search == null ? "" : search.getValue().trim().toLowerCase(Locale.ROOT);
            mc.getConnection().getOnlinePlayers().stream()
                    .map(PlayerInfo::getProfile)
                    .map(profile -> profile.name())
                    .filter(name -> name != null && name.toLowerCase(Locale.ROOT).contains(query))
                    .sorted(Comparator.naturalOrder())
                    .forEach(filtered::add);
        }

        int maxPage = Math.max(0, (filtered.size() - 1) / ROWS);
        page = Math.min(page, maxPage);
        for (int row = 0; row < playerButtons.size(); row++) {
            int index = page * ROWS + row;
            Button button = playerButtons.get(row);
            if (index < filtered.size()) {
                String name = filtered.get(index);
                button.setMessage(Component.literal((name.equals(selected) ? "✓ " : "") + name));
                button.visible = true;
                button.active = true;
            } else {
                button.setMessage(Component.literal(""));
                button.visible = false;
                button.active = false;
            }
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        g.fill(0, 0, width, height, 0x99000000);
        g.fill(panelLeft, panelTop, panelLeft + panelWidth, panelTop + panelHeight, 0xF0181D2A);
        g.fill(panelLeft, panelTop, panelLeft + panelWidth, panelTop + 3, 0xFF6C8CFF);
        g.drawString(font, "HELPER TOOLS", panelLeft + 18, panelTop + 15, 0xFFFFFFFF, true);
        g.drawString(font, "Игроков найдено: " + filtered.size(), panelLeft + 18, panelTop + 34, 0xFFB9C4D8, false);
        g.drawString(font, "Страница: " + (page + 1) + " / " + Math.max(1, (filtered.size() + ROWS - 1) / ROWS),
                panelLeft + 100, panelTop + panelHeight - 32, 0xFFB9C4D8, false);
        if (filtered.isEmpty()) {
            g.drawCenteredString(font, "Совпадений нет", width / 2, panelTop + 130, 0xFFB9C4D8);
        }
        if (selected != null) {
            g.drawString(font, "Выбран: " + selected, panelLeft + 18, panelTop + panelHeight - 57, 0xFF9FB5FF, false);
        }
        super.render(g, mouseX, mouseY, delta);
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
