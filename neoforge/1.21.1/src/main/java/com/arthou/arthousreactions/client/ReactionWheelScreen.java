package com.arthou.arthousreactions.client;

import com.arthou.arthousreactions.reaction.ReactionType;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public final class ReactionWheelScreen extends Screen {
    private static final ReactionType[] REACTIONS = ReactionType.values();
    private static final int PAGE_SIZE = 12;
    private static final int SLOT_SIZE = 60;
    private static final int SLOT_DISTANCE = 136;
    private static final int OUTER_RING = 176;
    private static final int INNER_RING = 72;
    private static final int PAGE_BUTTON_SIZE = 24;
    private static final int[][] CIRCLE_SPANS = new int[OUTER_RING + 1][];
    private static final List<List<Slot>> PAGES = createPages();
    private final WheelPreferences preferences = PreferencesHolder.INSTANCE;
    private final long openedAtMs = Util.getMillis();
    private final WheelMotion motion = new WheelMotion(REACTIONS.length, PAGE_SIZE, openedAtMs,
        preferences.page(PAGES.size()), preferences.previewEnabled());
    private final float[] itemHover = new float[REACTIONS.length];
    private final ReactionPlayerPreview preview = new ReactionPlayerPreview();
    private ReactionType selected = PAGES.get(motion.page()).getFirst().reaction();
    private boolean previewToggleHeld;

    public ReactionWheelScreen() {
        super(Component.translatable("screen.arthousreactions.title"));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT || button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            if (isOverPageButton(mouseX, mouseY, false)) {
                changePage(-1);
                return true;
            }
            if (isOverPageButton(mouseX, mouseY, true)) {
                changePage(1);
                return true;
            }
            ReactionType hovered = findHoveredReaction(mouseX, mouseY);
            if (hovered != null) {
                if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT
                    && (hovered == ReactionType.EMOTE_HI || hovered == ReactionType.SCUBA)) {
                    ReactionClientBootstrap.sendReactionWithAlternateSound(hovered);
                    onClose();
                } else if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                    selectReaction(hovered);
                }
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || isOpenWheelKey(keyCode)) {
            onClose();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_P) {
            if (!previewToggleHeld) {
                preferences.togglePreview();
                if (!preferences.previewEnabled()) preview.close();
            }
            previewToggleHeld = true;
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_LEFT || keyCode == GLFW.GLFW_KEY_A || keyCode == GLFW.GLFW_KEY_PAGE_UP) {
            changePage(-1);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_RIGHT || keyCode == GLFW.GLFW_KEY_D || keyCode == GLFW.GLFW_KEY_PAGE_DOWN) {
            changePage(1);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_SPACE) {
            ReactionType hovered = findHoveredReaction(scaledMouseX(), scaledMouseY());
            if (hovered != null) {
                selectReaction(hovered);
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_P) {
            previewToggleHeld = false;
            return true;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY > 0.0D) {
            changePage(-1);
            return true;
        }
        if (scrollY < 0.0D) {
            changePage(1);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        long now = Util.getMillis();
        float opacity = motion.opacity(now);
        WheelLayout layout = layoutAt(now);
        int centerX = WheelLayout.WHEEL_X;
        int centerY = WheelLayout.WHEEL_Y;
        ReactionType hovered = findHoveredReaction(mouseX, mouseY);
        if (hovered != null) selected = hovered;
        for (ReactionType reaction : REACTIONS) {
            itemHover[reaction.index()] = motion.hover(reaction.index(), reaction == hovered, now);
        }
        setOpacity(graphics, 1.0F);
        graphics.fill(0, 0, this.width, this.height, WheelMotion.mix(0x0005070A, 0x7D05070A, opacity));
        graphics.pose().pushPose();
        try {
            graphics.pose().translate(layout.left(), layout.top(), 0.0F);
            graphics.pose().scale(layout.scale(), layout.scale(), 1.0F);
            setOpacity(graphics, opacity);
            fillCircle(graphics, centerX, centerY, OUTER_RING, 0x32FFFFFF);
            fillCircle(graphics, centerX, centerY, OUTER_RING - 1, 0xE61C2630);
            fillCircle(graphics, centerX, centerY, 82, 0xE5161F29);
            for (int page = 0; page < PAGES.size(); page++) {
                float pageOpacity = motion.pageOpacity(page, now);
                if (pageOpacity <= 0.001F) continue;
                setOpacity(graphics, opacity * pageOpacity);
                graphics.pose().pushPose();
                scaleAround(graphics, centerX, centerY, motion.pageScale(page, now));
                for (Slot slot : PAGES.get(page)) {
                    float hover = itemHover[slot.reaction().index()];
                    float highlight = slot.reaction() == selected ? 1.0F : hover;
                    graphics.pose().pushPose();
                    graphics.pose().translate(centerX + slot.x(), centerY + slot.y(), 0.0F);
                    graphics.pose().scale(1.0F + hover * 0.10F, 1.0F + hover * 0.10F, 1.0F);
                    fillCircle(graphics, 0, 0, 31, WheelMotion.mix(0x40FFFFFF, 0xFFF4D08B, highlight));
                    fillCircle(graphics, 0, 0, 29, WheelMotion.mix(0xE638434F, 0xFFEEE4D4, highlight));
                    drawReaction(graphics, slot.reaction(), 0, 0, 42, now);
                    graphics.pose().popPose();
                }
                graphics.pose().popPose();
            }
            setOpacity(graphics, opacity);
            drawCenterText(graphics, Component.translatable(selected.translationKey()), centerX, centerY - 10, 0xFFF6F8FB);
            if (motion.pageCount() > 1) {
                renderPageButton(graphics, centerX - 38, centerY + 16, "<",
                    motion.arrow(0, isOverPageButton(mouseX, mouseY, false), now));
                renderPageButton(graphics, centerX + 14, centerY + 16, ">",
                    motion.arrow(1, isOverPageButton(mouseX, mouseY, true), now));
            }
            if (preferences.previewEnabled()) {
                setOpacity(graphics, opacity * motion.previewVisibility(true, now));
                preview.render(graphics, layout, selected, Screen.hasShiftDown(), now);
                renderCombinations(graphics);
            }
        } finally {
            setOpacity(graphics, 1.0F);
            graphics.pose().popPose();
        }
    }

    private void renderCombinations(GuiGraphics graphics) {
        String[] keys = selected == ReactionType.SCUBA
            ? new String[]{"scuba_left", "scuba_right", "scuba_shift_left", "scuba_shift_right"}
            : selected == ReactionType.EMOTE_HI ? new String[]{"hi_left", "hi_right"} : new String[]{"left"};
        for (int line = 0; line < keys.length; line++) {
            Component text = Component.translatable("screen.arthousreactions.combo." + keys[line]);
            float fit = Math.min(1.0F, (WheelLayout.PREVIEW_WIDTH - 8.0F) / Math.max(1, this.font.width(text)));
            graphics.pose().pushPose();
            graphics.pose().translate(WheelLayout.PREVIEW_X + 4, 298 + line * 14, 0.0F);
            graphics.pose().scale(fit, fit, 1.0F);
            graphics.drawString(this.font, text, 0, 0,
                selected == ReactionType.SCUBA && line >= 2 && Screen.hasShiftDown() ? 0xFFF1CE8F : 0xFFD5DDE3, false);
            graphics.pose().popPose();
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        preview.close();
        if (this.minecraft != null) this.minecraft.setScreen(null);
    }

    @Override
    public void removed() {
        preview.close();
        super.removed();
    }

    private void drawReaction(GuiGraphics graphics, ReactionType reaction, int centerX, int centerY, int size, long now) {
        graphics.flush(); // Submit the batched circles before this immediate texture draw.
        if (!reaction.hasEmoji()) {
            graphics.drawCenteredString(this.font, "CHEER", centerX, centerY - 5, 0xFFF6F8FB);
            graphics.drawCenteredString(this.font, "✦", centerX, centerY + 8, 0xFFE0B96D);
            return;
        }
        long elapsedMs = now - openedAtMs;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        try {
            graphics.blit(reaction.texture(), centerX - size / 2, centerY - size / 2, size, size,
                reaction.frameX(elapsedMs), reaction.frameY(elapsedMs), reaction.frameSize(), reaction.frameSize(),
                reaction.textureWidth(), reaction.textureHeight());
        } finally {
            RenderSystem.disableBlend();
        }
    }

    private void drawCenterText(GuiGraphics graphics, Component text, int centerX, int y, int color) {
        float fit = Math.min(1.0F, (INNER_RING * 2.0F - 8.0F) / Math.max(1, this.font.width(text)));
        graphics.pose().pushPose();
        graphics.pose().translate(centerX, y, 0.0F);
        graphics.pose().scale(fit, fit, 1.0F);
        graphics.drawCenteredString(this.font, text, 0, 0, color);
        graphics.pose().popPose();
    }

    private ReactionType findHoveredReaction(double mouseX, double mouseY) {
        long now = Util.getMillis();
        WheelLayout layout = layoutAt(now);
        float pageScale = motion.pageScale(motion.page(), now);
        double x = (layout.localX(mouseX) - WheelLayout.WHEEL_X) / pageScale;
        double y = (layout.localY(mouseY) - WheelLayout.WHEEL_Y) / pageScale;
        ReactionType closest = null;
        double closestDistance = Double.MAX_VALUE;
        for (Slot slot : PAGES.get(motion.page())) {
            double distance = Math.hypot(x - slot.x(), y - slot.y());
            if (distance <= SLOT_SIZE * 0.72D && distance < closestDistance) {
                closest = slot.reaction();
                closestDistance = distance;
            }
        }
        return closest;
    }

    private void changePage(int delta) {
        motion.navigate(delta, Util.getMillis());
        preferences.setPage(motion.page());
        selected = PAGES.get(motion.page()).getFirst().reaction();
    }

    private void renderPageButton(GuiGraphics graphics, int x, int y, String label, float hover) {
        graphics.pose().pushPose();
        scaleAround(graphics, x + PAGE_BUTTON_SIZE / 2, y + PAGE_BUTTON_SIZE / 2, 1.0F + hover * 0.06F);
        fillCircle(graphics, x + PAGE_BUTTON_SIZE / 2, y + PAGE_BUTTON_SIZE / 2, PAGE_BUTTON_SIZE / 2,
            WheelMotion.mix(0x40FFFFFF, 0xFFF4D08B, hover));
        fillCircle(graphics, x + PAGE_BUTTON_SIZE / 2, y + PAGE_BUTTON_SIZE / 2, PAGE_BUTTON_SIZE / 2 - 1,
            WheelMotion.mix(0xFF2A3138, 0xFFE0B96D, hover));
        graphics.flush();
        graphics.drawCenteredString(this.font, label, x + PAGE_BUTTON_SIZE / 2, y + 8,
            WheelMotion.mix(0xFFF3F5F8, 0xFF1A1E24, hover));
        graphics.pose().popPose();
    }

    private boolean isOverPageButton(double mouseX, double mouseY, boolean next) {
        WheelLayout layout = layoutAt(Util.getMillis());
        double x = layout.localX(mouseX);
        double y = layout.localY(mouseY);
        int left = WheelLayout.WHEEL_X + (next ? 14 : -38);
        int top = WheelLayout.WHEEL_Y + 16;
        return x >= left && x < left + PAGE_BUTTON_SIZE && y >= top && y < top + PAGE_BUTTON_SIZE;
    }

    private boolean isOpenWheelKey(int keyCode) {
        return ReactionClientBootstrap.openWheelKey() != null && keyCode == ReactionClientBootstrap.openWheelKey().getKey().getValue();
    }

    private WheelLayout layoutAt(long now) {
        return WheelLayout.at(this.width, this.height, motion, now,
            motion.previewVisibility(preferences.previewEnabled(), now));
    }

    private double scaledMouseX() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.mouseHandler.xpos() * this.width / minecraft.getWindow().getScreenWidth();
    }

    private double scaledMouseY() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.mouseHandler.ypos() * this.height / minecraft.getWindow().getScreenHeight();
    }

    private static void setOpacity(GuiGraphics graphics, float opacity) {
        graphics.flush();
        graphics.setColor(1.0F, 1.0F, 1.0F, opacity);
    }

    private static void scaleAround(GuiGraphics graphics, int x, int y, float scale) {
        graphics.pose().translate(x, y, 0.0F);
        graphics.pose().scale(scale, scale, 1.0F);
        graphics.pose().translate(-x, -y, 0.0F);
    }

    private static void fillCircle(GuiGraphics graphics, int centerX, int centerY, int radius, int color) {
        int[] spans = CIRCLE_SPANS[radius];
        if (spans == null) {
            spans = new int[radius * 2 + 1];
            for (int dx = -radius; dx <= radius; dx++) spans[dx + radius] = (int) Math.floor(Math.sqrt(radius * radius - dx * dx));
            CIRCLE_SPANS[radius] = spans;
        }
        Matrix4f matrix = graphics.pose().last().pose();
        VertexConsumer vertices = graphics.bufferSource().getBuffer(RenderType.gui());
        for (int dx = -radius; dx <= radius; dx++) {
            int height = spans[dx + radius];
            int x = centerX + dx;
            int top = centerY - height;
            int bottom = centerY + height + 1;
            vertices.addVertex(matrix, x, top, 0).setColor(color);
            vertices.addVertex(matrix, x, bottom, 0).setColor(color);
            vertices.addVertex(matrix, x + 1, bottom, 0).setColor(color);
            vertices.addVertex(matrix, x + 1, top, 0).setColor(color);
        }
    }

    private static List<List<Slot>> createPages() {
        ReactionType[] reactions = REACTIONS;
        List<List<Slot>> pages = new ArrayList<>();
        for (int start = 0; start < reactions.length; start += PAGE_SIZE) {
            int count = Math.min(PAGE_SIZE, reactions.length - start);
            List<Slot> slots = new ArrayList<>();
            for (int slot = 0; slot < count; slot++) {
                double angle = Math.toRadians(-90.0D + slot * (360.0D / count));
                slots.add(new Slot(reactions[start + slot], (int) Math.round(Math.cos(angle) * SLOT_DISTANCE),
                    (int) Math.round(Math.sin(angle) * SLOT_DISTANCE)));
            }
            pages.add(List.copyOf(slots));
        }
        return List.copyOf(pages);
    }

    private void selectReaction(ReactionType reaction) {
        ReactionClientBootstrap.sendReaction(reaction);
        onClose();
    }

    private record Slot(ReactionType reaction, int x, int y) {
    }

    private static final class PreferencesHolder {
        private static final WheelPreferences INSTANCE = new WheelPreferences(
            net.neoforged.fml.loading.FMLPaths.CONFIGDIR.get().resolve("arthousreactions-wheel.properties"));
    }
}
