package sonar.logistics.client.gui;

import java.io.IOException;
import java.util.List;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import sonar.core.SonarCore;
import sonar.core.api.inventories.StoredItemStack;
import sonar.core.client.gui.GuiHelpOverlay;
import sonar.core.client.gui.SonarTextField;
import sonar.core.helpers.RenderHelper;
import sonar.core.network.FlexibleGuiHandler;
import sonar.logistics.PL2;
import sonar.logistics.PL2Constants;
import sonar.logistics.PL2Translate;
import sonar.logistics.api.tiles.readers.InventoryReader;
import sonar.logistics.api.tiles.readers.InventoryReader.Modes;
import sonar.logistics.api.utils.ListPacket;
import sonar.logistics.api.utils.MonitoredList;
import sonar.logistics.client.LogisticsButton;
import sonar.logistics.client.gui.generic.GuiSelectionGrid;
import sonar.logistics.common.containers.ContainerInventoryReader;
import sonar.logistics.common.multiparts.readers.InventoryReaderPart;
import sonar.logistics.info.types.MonitoredItemStack;
import sonar.logistics.network.PacketInventoryReader;
import sonar.logistics.network.PacketNodeFilter;

public class GuiInventoryReader extends GuiSelectionGrid<MonitoredItemStack> {

	// public static final ResourceLocation stackBGround = new ResourceLocation("PracticalLogistics:textures/gui/inventoryReader_stack.png");
	// public static final ResourceLocation clearBGround = new ResourceLocation("PracticalLogistics:textures/gui/inventoryReader_clear.png");

	public static final ResourceLocation sorting_icons = new ResourceLocation(PL2Constants.MODID + ":textures/gui/sorting_icons.png");

	private InventoryReaderPart part;
	private SonarTextField slotField;
	private SonarTextField searchField;
	public EntityPlayer player;

	public GuiInventoryReader(InventoryReaderPart part, EntityPlayer player) {
		super(new ContainerInventoryReader(part, player), part);
		this.part = part;
		this.player = player;
	}

	public Modes getSetting() {
		return part.setting.getObject();
	}

	public void initGui() {
		super.initGui();
		initButtons();
		switch (getSetting()) {
		case SLOT:
		case POS:
			slotField = new SonarTextField(0, this.fontRendererObj, 63, 10, 32, 14);
			slotField.setMaxStringLength(7);
			slotField.setDigitsOnly(true);
			if (getSetting() == Modes.SLOT)
				slotField.setText("" + part.targetSlot.getObject());
			else if (getSetting() == Modes.POS)
				slotField.setText("" + part.posSlot.getObject());
			fieldList.add(slotField);
			break;
		default:
			break;
		}
		searchField = new SonarTextField(1, this.fontRendererObj, 135, 10, 104, 14);
		searchField.setMaxStringLength(20);
		fieldList.add(searchField);
	}

	public void initButtons() {
		super.initButtons();
		int start = 8;
		this.buttonList.add(new LogisticsButton.CHANNELS(this, 2, guiLeft + start, guiTop + 9));
		this.buttonList.add(new LogisticsButton.HELP(this, 5, guiLeft + start + 18 * 1, guiTop + 9));
		this.buttonList.add(new LogisticsButton(this, -1, guiLeft + start + 18 * 2, guiTop + 9, 64 + 32, 16 * part.setting.getObject().ordinal(), getSettingsString(), ""));
		this.buttonList.add(new LogisticsButton(this, 0, guiLeft + xSize - 168 + 18, guiTop + 9, 32, 16 * part.sortingOrder.getObject().ordinal(), PL2Translate.BUTTON_SORTING_ORDER.t(), ""));
		this.buttonList.add(new LogisticsButton(this, 1, guiLeft + xSize - 168 + 18 * 2, guiTop + 9, 64 + 48, 16 * part.sortingType.getObject().ordinal(), part.sortingType.getObject().getClientName(), ""));
		this.buttonList.add(new LogisticsButton(this, 3, guiLeft + 203, guiTop + 174, 32, 0, PL2Translate.BUTTON_DUMP_PLAYER.t(), ""));
		this.buttonList.add(new LogisticsButton(this, 4, guiLeft + 203 + 18, guiTop + 174, 32, 16, PL2Translate.BUTTON_DUMP_NETWORK.t(), ""));
		if (part.setting.getObject() == Modes.FILTERED) {
			this.buttonList.add(new LogisticsButton(this, 6, guiLeft + start + 18 * 3, guiTop + 9, 32, 64, PL2Translate.BUTTON_CONFIGURE_FILTERS.t(), "button.TileFilters"));
			this.buttonList.add(new LogisticsButton(this, 7, guiLeft + start + 18 * 4, guiTop + 9, 32, 96, PL2Translate.BUTTON_CLEAR_ALL.t(), "button.ClearAllFilter"));
		}
	}

	public void actionPerformed(GuiButton button) {
		if (button != null) {
			switch (button.id) {
			case -1:
				part.setting.incrementEnum();
				part.sendByteBufPacket(2);
				switchState();
				reset();
				break;
			case 0:
				part.sortingOrder.incrementEnum();
				part.sendByteBufPacket(5);
				initButtons();
				break;
			case 1:
				part.sortingType.incrementEnum();
				part.sendByteBufPacket(6);
				initButtons();
				break;
			case 2:
				FlexibleGuiHandler.changeGui(part, 1, 0, player.getEntityWorld(), player);
				break;
			case 3:
				PL2.network.sendToServer(new PacketInventoryReader(part.getUUID(), part.getPos(), null, 3));
				break;
			case 4:
				PL2.network.sendToServer(new PacketInventoryReader(part.getUUID(), part.getPos(), null, 4));
				break;
			case 5:
				GuiHelpOverlay.enableHelp = !GuiHelpOverlay.enableHelp;
				reset();
				break;
			case 6:
				FlexibleGuiHandler.changeGui(part, 2, 0, player.getEntityWorld(), player);
				break;
			case 7:
				PL2.network.sendToServer(new PacketNodeFilter(part.getUUID(), part.getCoords().getBlockPos(), ListPacket.CLEAR));
				break;
			}
		}
	}

	public void switchState() {
		SonarCore.refreshFlexibleContainer(player);
	}

	@Override
	public void mouseClicked(int i, int j, int k) throws IOException {
		super.mouseClicked(i, j, k);
		if (k == 1) {
			searchField.setText("");
		}
	}

	public void onTextFieldChanged(SonarTextField field) {
		if (field == slotField) {
			final String text = slotField.getText();
			int num = field.getIntegerFromText();
			if (getSetting() == Modes.SLOT) {
				part.targetSlot.setObject(num);
				part.sendByteBufPacket(part.targetSlot.id);
			}
			if (getSetting() == Modes.POS) {
				part.posSlot.setObject(num);
				part.sendByteBufPacket(part.posSlot.id);
			}
		}
	}

	public String getSettingsString() {
		return part.setting.getObject().getClientName();
	}

	@Override
	public List<MonitoredItemStack> getGridList() {
		String search = searchField.getText();
		if (search == null || search.isEmpty() || search.equals(" ")) {
			return part.getMonitoredList();
		} else {
			MonitoredList<MonitoredItemStack> searchList = MonitoredList.newMonitoredList(part.getNetworkID());
			for (MonitoredItemStack stack : (List<MonitoredItemStack>) part.getMonitoredList().clone()) {
				StoredItemStack item = stack.getStoredStack();
				if (stack != null && item != null && item.item.getDisplayName().toLowerCase().contains(search.toLowerCase())) {
					searchList.add(stack);
				}
			}
			return searchList;
		}
	}

	@Override
	public void onGridClicked(MonitoredItemStack selection, int pos, int button, boolean empty) {
		if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
			button = 2;
		}
		if (!empty) {
			PL2.network.sendToServer(new PacketInventoryReader(part.getUUID(), part.getPos(), selection.getStoredStack().item, button));
		} else {
			PL2.network.sendToServer(new PacketInventoryReader(part.getUUID(), part.getPos(), null, button));
		}
	}

	@Override
	public void renderStrings(int x, int y) {}

	@Override
	public void renderGridElement(MonitoredItemStack selection, int x, int y, int slot) {
		RenderHelper.saveBlendState();
		StoredItemStack storedStack = selection.getStoredStack();
		if (storedStack == null) {
			return;
		}
		ItemStack stack = storedStack.item;
		RenderHelper.renderItem(this, 13 + (x * 18), 32 + (y * 18), stack);
		RenderHelper.renderStoredItemStackOverlay(stack, storedStack.stored, 13 + (x * 18), 32 + (y * 18), null, true);
		RenderHelper.restoreBlendState();

	}

	@Override
	public void renderElementToolTip(MonitoredItemStack element, int x, int y) {
		StoredItemStack storedStack = element.getStoredStack();
		if (storedStack == null) {
			return;
		}
		List list = storedStack.item.getTooltip(this.mc.thePlayer, this.mc.gameSettings.advancedItemTooltips);
		list.add(1, PL2Translate.BUTTON_STORED + ": " + storedStack.stored);
		for (int k = 0; k < list.size(); ++k) {
			if (k == 0) {
				list.set(k, storedStack.item.getRarity().rarityColor + (String) list.get(k));
			} else {
				list.set(k, TextFormatting.GRAY + (String) list.get(k));
			}
		}

		FontRenderer font = storedStack.item.getItem().getFontRenderer(storedStack.item);
		drawHoveringText(list, x, y, (font == null ? fontRendererObj : font));

	}

	@Override
	public void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
		super.drawGuiContainerBackgroundLayer(var1, var2, var3);
		if (this.getSetting() == InventoryReader.Modes.STACK) {
			Minecraft.getMinecraft().getTextureManager().bindTexture(playerInv);
			drawTexturedModalRect(guiLeft + 62, guiTop + 8, 0, 0, 18, 18);
		}
	}
}
