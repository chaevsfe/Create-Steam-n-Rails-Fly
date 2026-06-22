/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.content.bogey_menu;

import com.google.common.collect.ImmutableList;
import com.railwayteam.railways.api.bogeymenu.v0.entry.BogeyEntry;
import java.util.Optional;
import com.railwayteam.railways.api.bogeymenu.v0.entry.CategoryEntry;
import com.railwayteam.railways.content.bogey_menu.handler.BogeyMenuHandlerClient;
import com.railwayteam.railways.impl.bogeymenu.v0.BogeyMenuManagerImpl;
import com.railwayteam.railways.registry.CRGuiTextures;
import com.railwayteam.railways.registry.CRIcons;
import com.railwayteam.railways.registry.CRPackets;
import com.railwayteam.railways.util.packet.BogeyStyleSelectionPacket;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.gui.AbstractSimiScreen;
import com.zurrtum.create.client.catnip.gui.element.GuiGameElement;
import com.zurrtum.create.client.catnip.gui.render.EntityBlockRenderState;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.AllIcons;
import com.zurrtum.create.client.foundation.gui.widget.IconButton;
import com.zurrtum.create.client.foundation.gui.widget.Indicator;
import com.zurrtum.create.client.foundation.gui.widget.Label;
import com.zurrtum.create.client.foundation.gui.widget.ScrollInput;
import com.zurrtum.create.client.foundation.gui.widget.SelectionScrollInput;
import com.zurrtum.create.client.foundation.gui.widget.TooltipArea;
import com.zurrtum.create.content.trains.bogey.AbstractBogeyBlock;
import com.zurrtum.create.content.trains.bogey.AbstractBogeyBlockEntity;
import com.zurrtum.create.content.trains.bogey.BogeySize;
import com.zurrtum.create.content.trains.bogey.BogeyStyle;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.Comparator;
import java.util.List;

public class BogeyMenuScreen extends AbstractSimiScreen {
	private final CRGuiTextures background = CRGuiTextures.BOGEY_MENU;
	private final List<Component> categoryComponentList = BogeyMenuManagerImpl.CATEGORIES.stream()
		.map(CategoryEntry::getName)
		.toList();

	private CategoryEntry selectedCategory = firstCategory();
	private int categoryIndex;
	private final BogeyEntry[] bogeyList = new BogeyEntry[6];
	private final MenuButton[] bogeyMenuButtons = new MenuButton[6];
	private @Nullable BogeyEntry selectedBogey;
	private float scrollOffs;
	private boolean scrolling;
	private int ticksOpen;
	private boolean soundPlayed;
	private TooltipArea longBogeyTooltipArea;
	private IconButton favouriteButton;

	public BogeyMenuScreen() {
		super(Component.translatable("railways.gui.bogey_menu"));
	}

	@Override
	protected void init() {
		setWindowSize(background.width, background.height);
		super.init();
		clearWidgets();

		int x = guiLeft;
		int y = guiTop;
		categoryIndex = Math.max(0, BogeyMenuManagerImpl.CATEGORIES.indexOf(selectedCategory));

		for (int i = 0; i < 6; i++)
			addRenderableWidget(bogeyMenuButtons[i] = new MenuButton(x + 19, y + 41 + (i * 18), 82, 17, bogeySelection(i)));

		setupList(selectedCategory);
		selectedBogey = BogeyMenuHandlerClient.getLastSelected()
			.map(style -> BogeyEntry.STYLE_TO_ENTRY.get(style))
			.orElseGet(() -> firstBogey(selectedCategory));
		if (selectedBogey == null)
			selectedBogey = firstBogey(selectedCategory);
		scrollOffs = 0;
		scrollTo(0);

		Label categoryLabel = new Label(x + 14, y + 25, Component.empty()).withShadow();
		ScrollInput categoryScrollInput = new SelectionScrollInput(x + 9, y + 20, 77, 18)
			.forOptions(categoryComponentList)
			.writingTo(categoryLabel)
			.setState(categoryIndex)
			.calling(categoryIndex -> {
				scrollOffs = 0;
				scrollTo(0);
				this.categoryIndex = categoryIndex;
				selectedCategory = BogeyMenuManagerImpl.CATEGORIES.get(categoryIndex);
				setupList(selectedCategory);
				selectedBogey = firstBogey(selectedCategory);
				updateFavorites();
			});

		addRenderableWidget(categoryLabel);
		addRenderableWidget(categoryScrollInput);

		favouriteButton = new IconButton(x + background.width - 167, y + background.height - 49, CRIcons.I_FAVORITE);
		favouriteButton.withCallback(this::onFavorite);
		addRenderableWidget(favouriteButton);

		IconButton closeButton = new IconButton(x + background.width - 33, y + background.height - 24, AllIcons.I_CONFIRM);
		closeButton.withCallback(this::onMenuClose);
		addRenderableWidget(closeButton);

		String[] gaugeText = new String[] { "narrow", "standard", "wide" };
		for (int i = 0; i < 3; i++) {
			addRenderableOnly(new TooltipArea(x + 163 + (i * 18), y + 135, 18, 18)
				.withTooltip(ImmutableList.of(
					Component.translatable("railways.gui.bogey_menu.gauge_description")
						.withStyle(style -> style.withColor(0x5391E1)),
					Component.translatable("railways.gui.bogey_menu." + gaugeText[i] + "_gauge")
						.withStyle(ChatFormatting.GRAY)
				)));
		}

		longBogeyTooltipArea = new TooltipArea(x + 122, y + 20, 136, 18);
		addRenderableOnly(longBogeyTooltipArea);
		updateFavorites();
	}

	@Override
	protected void renderWindow(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		int x = guiLeft;
		int y = guiTop;

		background.render(guiGraphics, x, y, 512, 512);

		MutableComponent header = Component.translatable("railways.gui.bogey_menu.title");
		guiGraphics.drawString(font, header, x + background.width / 2 - font.width(header) / 2, y + 4, 0xFF582424, false);

		// Match the original corner-pivot transform while giving the rotated block room to render.
		float casingScale = 2.5f;
		int casingPadding = 20;
		float casingTextureSize = casingScale * 16 + casingPadding;
		float casingCenterX = x + background.width + 31;
		float casingCenterY = y + background.height - 11;
		GuiGameElement.of(AllBlocks.RAILWAY_CASING.defaultBlockState())
			.scale(casingScale)
			.padding(casingPadding)
			.rotate(-22, 63, 0)
			.at(casingCenterX - casingTextureSize / 2, casingCenterY - casingTextureSize / 2)
			.render(guiGraphics);

		int scrollBarPos = (int) (41 + (134 - 41) * scrollOffs);
		CRGuiTextures barTexture = canScroll() ? CRGuiTextures.BOGEY_MENU_SCROLL_BAR : CRGuiTextures.BOGEY_MENU_SCROLL_BAR_DISABLED;
		barTexture.render(guiGraphics, x + 11, y + scrollBarPos, 512, 512);

		for (int i = 0; i < 6; i++) {
			BogeyEntry bogeyEntry = bogeyList[i];
			if (bogeyEntry == null)
				continue;

			Identifier icon = bogeyEntry.iconLocation();
			if (icon != null)
				renderIcon(guiGraphics, icon, x + 20, y + 42 + (i * 18));

			Component bogeyName = trimToWidth(bogeyEntry.bogeyStyle().displayName, 55);
			guiGraphics.drawString(font, bogeyName, x + 40, y + 46 + (i * 18), 0xFFFFFFFF);
		}

		if (selectedBogey == null)
			return;

		Component displayName = selectedBogey.bogeyStyle().displayName;
		Component bogeyName = trimToWidth(displayName, 126);
		guiGraphics.drawCenteredString(font, bogeyName, x + 190, y + 25, 0xFFFFFFFF);

		if (font.width(displayName) > 126) {
			longBogeyTooltipArea.withTooltip(ImmutableList.of(displayName));
			longBogeyTooltipArea.visible = true;
		} else {
			longBogeyTooltipArea.visible = false;
		}

		Indicator.State[] states = BogeyMenuHandlerClient.getTrackCompat(selectedBogey);
		for (int i = 0; i < 3; i++)
			indicatorTexture(states[i]).render(guiGraphics, x + 163 + (i * 18), y + 128);

		renderBogeyPreview(guiGraphics, x, y, partialTicks);
	}

	private void renderBogeyPreview(GuiGraphics guiGraphics, int x, int y, float partialTicks) {
		BogeyStyle style = selectedBogey.bogeyStyle();
		List<Pair<BogeyStyle, BogeySize>> renderCycle = BogeyMenuHandlerClient.getRenderCycle(style);
		if (renderCycle.isEmpty())
			return;

		Pair<BogeyStyle, BogeySize> renderPair = renderCycle.get((ticksOpen / 40) % renderCycle.size());
		BogeyStyle renderStyle = renderPair.getFirst();
		BogeySize renderSize = renderPair.getSecond();
		Block renderBlock = style.getBlockForSize(renderSize);
		if (!(renderBlock instanceof AbstractBogeyBlock<?> bogeyBlock) || minecraft == null || minecraft.level == null)
			return;

		float bogeyScale = selectedBogey.scale();
		if (BogeyMenuManagerImpl.SIZES_TO_SCALE.containsKey(renderPair))
			bogeyScale = BogeyMenuManagerImpl.SIZES_TO_SCALE.get(renderPair);

		BlockState bogeyState = renderBlock.defaultBlockState().setValue(AbstractBogeyBlock.AXIS, Direction.Axis.Z);
		if (!(bogeyBlock.newBlockEntity(BlockPos.ZERO, bogeyState) instanceof AbstractBogeyBlockEntity sourceBogeyBE))
			return;
		float wheelAngle = 3 * AnimationTickHolder.getRenderTime(minecraft.level);
		AbstractBogeyBlockEntity bogeyBE = new AbstractBogeyBlockEntity(sourceBogeyBE.getType(), BlockPos.ZERO, bogeyState) {
			@Override
			public BogeyStyle getDefaultStyle() {
				return renderStyle;
			}

			@Override
			public float getVirtualAngle(float ignoredPartialTicks) {
				return wheelAngle;
			}
		};
		bogeyBE.setBogeyStyle(renderStyle);

		// EntityBlockRenderState multiplies modelScale by 16. Keep the original
		// pixels-per-block scale, but give long bogeys a larger texture to avoid clipping.
		float previewSize = 96;
		float modelScale = bogeyScale / 16f;
		int padding = Mth.ceil(previewSize - bogeyScale);
		int previewX = Mth.floor(x + 189.5f - previewSize / 2);
		int previewY = Mth.floor(y + 86 - previewSize / 2);
		int previewId = 31 * renderStyle.id.hashCode() + renderSize.id().hashCode();
		guiGraphics.guiRenderState.submitPicturesInPictureState(EntityBlockRenderState.create(
			previewId, guiGraphics, minecraft.level, BlockPos.ZERO, bogeyBE, bogeyState,
			previewX, previewY, modelScale, padding, 20, 45, 0
		));
	}

	private AllGuiTextures indicatorTexture(Indicator.State state) {
		return switch (state) {
			case ON -> AllGuiTextures.INDICATOR_WHITE;
			case OFF -> AllGuiTextures.INDICATOR;
			case RED -> AllGuiTextures.INDICATOR_RED;
			case YELLOW -> AllGuiTextures.INDICATOR_YELLOW;
			case GREEN -> AllGuiTextures.INDICATOR_GREEN;
		};
	}

	private void renderIcon(GuiGraphics guiGraphics, Identifier icon, int x, int y) {
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, icon, x, y, 0, 0, 16, 16, 16, 16);
	}

	private Component trimToWidth(Component component, int maxWidth) {
		if (font.width(component) <= maxWidth)
			return component;

		String text = font.plainSubstrByWidth(component.getString(), maxWidth);
		if (text.endsWith(" "))
			text = text.substring(0, text.length() - 1);
		return Component.literal(text + "...");
	}

	private void setupList(CategoryEntry categoryEntry) {
		setupList(categoryEntry, 0);
	}

	private void setupList(CategoryEntry categoryEntry, int offset) {
		List<BogeyEntry> bogies = categoryEntry.getBogeyEntryList();
		for (int i = 0; i < 6; i++) {
			if (i + offset < bogies.size()) {
				bogeyList[i] = bogies.get(i + offset);
				bogeyMenuButtons[i].active = true;
			} else {
				bogeyList[i] = null;
				bogeyMenuButtons[i].active = false;
			}
		}
	}

	@Override
	public void tick() {
		ticksOpen++;
		soundPlayed = false;
		super.tick();
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (event.key() == GLFW.GLFW_KEY_ESCAPE) {
			if (selectedBogey == null)
				onClose();
			else
				onMenuClose();
			return true;
		}
		if (event.key() == GLFW.GLFW_KEY_ENTER) {
			onMenuClose();
			return true;
		}
		return super.keyPressed(event);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (event.button() == 0 && insideScrollbar(event.x(), event.y())) {
			scrolling = canScroll();
			return true;
		}
		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		if (event.button() == 0)
			scrolling = false;
		return super.mouseReleased(event);
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
		if (!scrolling)
			return super.mouseDragged(event, dragX, dragY);

		int scrollbarTop = guiTop + 41;
		int scrollbarBottom = scrollbarTop + 108;
		float scrollFactor = (float) ((event.y() - scrollbarTop - 7.5F) / (scrollbarBottom - scrollbarTop - 15.0F));
		scrollOffs = Mth.clamp(scrollFactor, 0, 1);
		scrollTo(scrollOffs);
		return true;
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
		if (!canScroll() || insideCategorySelector(mouseX, mouseY) || selectedCategory.getBogeyEntryList().size() < 6)
			return false;

		double listSize = selectedCategory.getBogeyEntryList().size() - 6;
		float scrollFactor = (float) (scrollY / listSize);
		float oldScrollOffs = scrollOffs;
		scrollOffs = Mth.clamp(scrollOffs - scrollFactor, 0, 1);
		scrollTo(scrollOffs);

		if (!soundPlayed && scrollOffs != oldScrollOffs)
			Minecraft.getInstance().getSoundManager()
				.play(SimpleSoundInstance.forUI(AllSoundEvents.SCROLL_VALUE.getMainEvent(), 1.5f + 0.1f * scrollOffs));
		soundPlayed = true;
		return true;
	}

	private void scrollTo(float pos) {
		List<BogeyEntry> bogies = selectedCategory.getBogeyEntryList();
		float listSize = bogies.size() - 6;
		int index = (int) (pos * listSize + 0.5);
		setupList(selectedCategory, index);
	}

	private boolean canScroll() {
		return selectedCategory.getBogeyEntryList().size() > 6;
	}

	private boolean insideCategorySelector(double mouseX, double mouseY) {
		int left = guiLeft + 11;
		int top = guiTop + 20;
		return mouseX >= left && mouseY >= top && mouseX < left + 90 && mouseY < top + 34;
	}

	private boolean insideScrollbar(double mouseX, double mouseY) {
		int left = guiLeft + 11;
		int top = guiTop + 41;
		return mouseX >= left && mouseY >= top && mouseX < left + 8 && mouseY < top + 108;
	}

	private Runnable bogeySelection(int index) {
		return () -> {
			selectedBogey = bogeyList[index];
			updateFavorites();
		};
	}

	private void onFavorite() {
		if (selectedBogey == null)
			return;
		BogeyMenuHandlerClient.toggleFavorite(selectedBogey.bogeyStyle());
		updateFavorites();
	}

	private void updateFavorites() {
		if (favouriteButton == null || selectedBogey == null)
			return;
		favouriteButton.setIcon(BogeyMenuHandlerClient.isFavorited(selectedBogey.bogeyStyle())
			? CRIcons.I_FAVORITED
			: CRIcons.I_FAVORITE);

		if (selectedCategory == CategoryEntry.FavoritesCategory.INSTANCE) {
			scrollTo(scrollOffs);
			if (!selectedCategory.getBogeyEntryList().contains(selectedBogey))
				selectedBogey = firstBogey(selectedCategory);
		}
	}

	private void onMenuClose() {
		if (selectedBogey == null)
			return;

		BogeyStyle style = selectedBogey.bogeyStyle();
		BogeySize size = BogeyMenuHandlerClient.getSize(style);
		BogeyMenuHandlerClient.setLastSelected(style);
		CRPackets.PACKETS.send(new BogeyStyleSelectionPacket(style, size));
		onClose();
	}

	private CategoryEntry firstCategory() {
		return BogeyMenuManagerImpl.CATEGORIES.stream()
			.min(Comparator.comparingInt(category -> category == CategoryEntry.FavoritesCategory.INSTANCE ? 1 : 0))
			.orElse(CategoryEntry.FavoritesCategory.INSTANCE);
	}

	private @Nullable BogeyEntry firstBogey(CategoryEntry category) {
		List<BogeyEntry> entries = category.getBogeyEntryList();
		return entries.isEmpty() ? null : entries.get(0);
	}

	private static class MenuButton extends AbstractWidget {
		private final Runnable onPress;

		public MenuButton(int x, int y, int width, int height, Runnable onPress) {
			super(x, y, width, height, Component.empty());
			this.onPress = onPress;
		}

		@Override
		public void onClick(MouseButtonEvent event, boolean doubleClick) {
			onPress.run();
		}

		@Override
		protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		}

		@Override
		protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
		}
	}
}
