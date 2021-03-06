package sonar.logistics.client.gui.generic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;

import com.google.common.collect.Lists;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.text.TextFormatting;
import sonar.core.client.gui.GuiHelpOverlay;
import sonar.core.client.gui.SonarTextField;
import sonar.core.helpers.FontHelper;
import sonar.core.helpers.RenderHelper;
import sonar.core.inventory.ContainerMultipartSync;
import sonar.core.network.sync.ObjectType;
import sonar.core.utils.Pair;
import sonar.logistics.PL2;
import sonar.logistics.PL2Translate;
import sonar.logistics.api.info.IComparableInfo;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.INameableInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.tiles.readers.IInfoProvider;
import sonar.logistics.api.tiles.readers.INetworkReader;
import sonar.logistics.api.tiles.signaller.ComparableObject;
import sonar.logistics.api.tiles.signaller.EmitterStatement;
import sonar.logistics.api.tiles.signaller.InputTypes;
import sonar.logistics.api.tiles.signaller.LogicOperator;
import sonar.logistics.api.utils.ListPacket;
import sonar.logistics.client.LogisticsButton;
import sonar.logistics.client.LogisticsColours;
import sonar.logistics.client.RenderBlockSelection;
import sonar.logistics.common.multiparts.misc.RedstoneSignallerPart;
import sonar.logistics.helpers.InfoRenderer;
import sonar.logistics.info.types.MonitoredBlockCoords;
import sonar.logistics.info.types.MonitoredItemStack;
import sonar.logistics.network.PacketEmitterStatement;

public class GuiStatementList extends GuiSelectionList<Object> {

	public RedstoneSignallerPart tile;
	public GuiState state = GuiState.LIST;
	public int coolDown = 0;
	public EmitterStatement currentFilter;
	public EmitterStatement lastFilter;
	public int infoPos;
	public boolean currentBool = false;

	public SonarTextField inputField;

	public enum GuiState {
		LIST(true), STATEMENT(false), CHANNELS(true), STRING(true);

		boolean hasScroller;

		GuiState(boolean hasScroller) {
			this.hasScroller = hasScroller;
		}
	}

	public GuiStatementList(EntityPlayer player, RedstoneSignallerPart tile) {
		super(new ContainerMultipartSync(tile), tile);
		this.tile = tile;
		this.xSize = 182 + 66;
		this.ySize = ySize + 22;
	}

	public void initGui() {
		this.listHeight = GuiState.LIST == state ? 24 : 12;
		super.initGui();
		switch (state) {
		case LIST:
			int start = 42;
			this.buttonList.add(new LogisticsButton(this, 0, guiLeft + start, guiTop + 6, 48, 0 + 16 * tile.emitterMode().getObject().ordinal(), "Emit If: " + tile.emitterMode().getObject().name(), "button.EmitterMode"));
			this.buttonList.add(new LogisticsButton(this, 1, guiLeft + start + 20 * 1, guiTop + 6, 32, 128, "New Statement", "button.NewStatement"));
			this.buttonList.add(new LogisticsButton(this, 2, guiLeft + start + 20 * 2, guiTop + 6, 32, 0, PL2Translate.BUTTON_MOVE_UP.t(), "button.MoveUpStatement"));
			this.buttonList.add(new LogisticsButton(this, 3, guiLeft + start + 20 * 3, guiTop + 6, 32, 16, PL2Translate.BUTTON_MOVE_DOWN.t(), "button.MoveDownStatement"));
			this.buttonList.add(new LogisticsButton(this, 4, guiLeft + start + 20 * 4, guiTop + 6, 32, 32, PL2Translate.BUTTON_DELETE.t(), "button.DeleteStatement"));
			this.buttonList.add(new LogisticsButton(this, 5, guiLeft + start + 20 * 5, guiTop + 6, 32, 96, PL2Translate.BUTTON_CLEAR_ALL.t(), "button.ClearAllStatements"));
			this.buttonList.add(new LogisticsButton(this, 6, guiLeft + start + 20 * 6, guiTop + 6, 32, 128 + 16, "Refresh", "button.RedstoneSignallerRefresh"));
			this.buttonList.add(new LogisticsButton.HELP(this, 7, guiLeft + start + 20 * 7, guiTop + 6));
			break;
		case STATEMENT:
			this.buttonList.add(new GuiButton(0, guiLeft + xSize / 2 - 60, guiTop + 116, 120, 20, "Input Type: " + currentFilter.getInputType()));
			this.buttonList.add(new LogisticsButton(this, 1, guiLeft + 6, guiTop + 16, 32, 48, "Info Source", "button.InfoSignallerSource"));
			this.buttonList.add(new LogisticsButton(this, 3, guiLeft + 6, guiTop + 16 + 20 * 1, 32, 80, "Object Selection", "button.ObjectSelectionSource"));
			if (currentFilter.getInputType().usesInfo()) {
				this.buttonList.add(new LogisticsButton(this, 2, guiLeft + 6, guiTop + 72, 32, 48, "Info Source", "button.InfoSignallerSource"));
				this.buttonList.add(new LogisticsButton(this, 4, guiLeft + 6, guiTop + 92, 32, 80, "Object Selection", "button.ObjectSelectionSource"));
			} else if (currentFilter.getInputType() == InputTypes.BOOLEAN) {
				this.buttonList.add(new GuiButton(8, guiLeft + xSize / 2 - 60, guiTop + 80, 120, 20, "" + currentBool));
			} else {
				Keyboard.enableRepeatEvents(true);
				inputField = new SonarTextField(0, this.fontRendererObj, 8, 96, xSize - 16, 12);
				inputField.setDigitsOnly(currentFilter.getInputType() == InputTypes.NUMBER);
				inputField.setMaxStringLength(20);
				inputField.setText(currentFilter.obj.get() == null ? "" : currentFilter.obj.get().toString());
				fieldList.add(inputField);
			}
			this.buttonList.add(new GuiButton(5, guiLeft + xSize / 2 - 25, guiTop + 141, 50, 20, ((LogicOperator) currentFilter.operator.getObject()).operation));

			this.buttonList.add(new GuiButton(6, guiLeft + xSize / 2, guiTop + 164, xSize / 2 - 4, 20, "SAVE"));
			this.buttonList.add(new GuiButton(7, guiLeft + 4, guiTop + 164, xSize / 2 - 4, 20, "RESET"));

			break;
		default:
			break;
		}
	}

	public void actionPerformed(GuiButton button) {
		super.actionPerformed(button);
		switch (state) {
		case LIST:
			switch (button.id) {
			case 0:
				tile.mode.incrementEnum();
				tile.sendByteBufPacket(1);
				reset();
				break;
			case 1:
				EmitterStatement statement = new EmitterStatement();
				lastFilter = statement;
				currentFilter = statement;
				this.changeState(GuiState.STATEMENT);
				break;
			case 2:
				if (currentFilter != null) {
					PL2.network.sendToServer(new PacketEmitterStatement(tile.getUUID(), tile.getCoords().getBlockPos(), ListPacket.MOVE_UP, currentFilter));
				}
				break;
			case 3:
				if (currentFilter != null) {
					PL2.network.sendToServer(new PacketEmitterStatement(tile.getUUID(), tile.getCoords().getBlockPos(), ListPacket.MOVE_DOWN, currentFilter));
				}
				break;
			case 4:
				if (currentFilter != null) {
					PL2.network.sendToServer(new PacketEmitterStatement(tile.getUUID(), tile.getCoords().getBlockPos(), ListPacket.REMOVE, currentFilter));
				}
				break;
			case 5:
				PL2.network.sendToServer(new PacketEmitterStatement(tile.getUUID(), tile.getCoords().getBlockPos(), ListPacket.CLEAR));
				break;
			case 6:
				tile.requestSyncPacket();
				break;
			case 7:
				GuiHelpOverlay.enableHelp = !GuiHelpOverlay.enableHelp;
				reset();
				break;
			}
			break;
		case STATEMENT:
			switch (button.id) {
			case 0:
				currentFilter.incrementInputType();
				inputField.setText("");
				currentFilter.obj.set("", ObjectType.STRING);
				currentFilter.comparatorID.setObject(PL2.getComparatorRegistry().getObjectID(currentFilter.getInputType().comparatorID));
				currentFilter.comparator = PL2.getComparatorRegistry().getRegisteredObject(currentFilter.getInputType().comparatorID);
				if (!currentFilter.validOperators().contains(currentFilter.getOperator())) {
					currentFilter.operator.setObject((LogicOperator) currentFilter.validOperators().get(0));
				}
				if (currentFilter.getInputType() == InputTypes.BOOLEAN) {
					currentBool = false;
					currentFilter.obj.set(false, ObjectType.BOOLEAN);
				}
				reset();
				break;
			case 1:
			case 2:
				this.changeState(GuiState.CHANNELS);
				this.infoPos = button.id - 1;
				break;
			case 3:
			case 4:
				this.changeState(GuiState.STRING);
				this.infoPos = button.id - 3;
				break;
			case 5:
				currentFilter.incrementOperator();
				this.reset();
				break;
			case 6:
				PL2.network.sendToServer(new PacketEmitterStatement(tile.getUUID(), tile.getCoords().getBlockPos(), ListPacket.ADD, currentFilter));
				this.changeState(GuiState.LIST);
				break;
			case 7:
				// DON'T CHANGE. RESET!
				this.changeState(GuiState.LIST);
				break;
			case 8:
				currentBool = !currentBool;
				currentFilter.obj.set(currentBool, ObjectType.BOOLEAN);
				reset();
				break;
			}
			break;
		default:
			break;
		}
	}

	public void changeState(GuiState state) {
		if (state == GuiState.LIST && currentFilter != null) {
			// Logistics.network.sendToServer(new PacketNodeFilter(tile.getIdentity(), tile.getCoords().getBlockPos(), FilterPacket.ADD, currentFilter));
		}
		this.state = state;
		// this.xSize = 182 + 66;
		// this.ySize = state.ySize;
		this.enableListRendering = state.hasScroller;
		if (scroller != null)
			this.scroller.renderScroller = state.hasScroller;

		coolDown = state != GuiState.LIST ? 15 : 0;
		this.reset();
	}

	public void drawScreen(int x, int y, float var) {
		super.drawScreen(x, y, var);
		if (coolDown != 0) {
			coolDown--;
		}
	}

	@Override
	public void drawGuiContainerForegroundLayer(int x, int y) {
		switch (state) {
		case CHANNELS:
			FontHelper.textCentre(FontHelper.translate("Info Selection"), xSize, 6, LogisticsColours.white_text);
			FontHelper.textCentre(String.format("Select the Info you wish to check against"), xSize, 18, LogisticsColours.grey_text);
			break;
		case LIST:
			break;
		case STATEMENT:

			InfoUUID uuid1 = (InfoUUID) currentFilter.uuid1.getObject();
			InfoUUID uuid2 = (InfoUUID) currentFilter.uuid2.getObject();
			String info1 = "<- Select the info source", info2 = info1;
			String obj1 = "<- Select the object to compare", obj2 = obj1;
			boolean has1 = false, has2 = false;

			if (uuid1 != null) {
				IInfo monitorInfo = PL2.getClientManager().info.get(uuid1).copy();
				if (monitorInfo != null && monitorInfo instanceof IComparableInfo) {
					info1 = monitorInfo.getID().toUpperCase() + " - " + monitorInfo.toString();
					ComparableObject obj = ComparableObject.getComparableObject(((IComparableInfo) monitorInfo).getComparableObjects(Lists.newArrayList()), currentFilter.key1.getObject());
					if (obj != null) {
						has1 = true;
						obj1 = currentFilter.key1.getObject() + " - " + obj.object.toString();
					}
				}
			}
			if (currentFilter.getInputType().usesInfo()) {
				if (uuid2 != null) {
					IInfo monitorInfo = PL2.getClientManager().info.get(uuid2).copy();
					if (monitorInfo != null && monitorInfo instanceof IComparableInfo) {
						info2 = monitorInfo.getID().toUpperCase() + " - " + monitorInfo.toString();
						ComparableObject obj = ComparableObject.getComparableObject(((IComparableInfo) monitorInfo).getComparableObjects(Lists.newArrayList()), currentFilter.key2.getObject());
						if (obj != null) {
							has2 = true;
							obj2 = currentFilter.key2.getObject() + " - " + obj.object.toString();
						}
					}
				}
				FontHelper.text(info2, 26, 76, LogisticsColours.white_text.getRGB());
				FontHelper.text(obj2, 26, 96, LogisticsColours.white_text.getRGB());
			} else if (currentFilter.getInputType() == InputTypes.BOOLEAN) {
				has2 = true;
				obj2 = "" + currentBool;
			} else {
				FontHelper.text("Input Field", 12, 80, LogisticsColours.white_text.getRGB());

				if (!inputField.getText().isEmpty()) {
					has2 = true;
					obj2 = inputField.getText();
				}
			}

			FontHelper.textOffsetCentre(has1 ? obj1 : "Info 1", 48, 148, LogisticsColours.white_text);
			FontHelper.textOffsetCentre(has2 ? obj2 : "Info 2", xSize - 48, 148, LogisticsColours.white_text);

			FontHelper.text(info1, 26, 20, LogisticsColours.white_text.getRGB());
			FontHelper.text(obj1, 26, 40, LogisticsColours.white_text.getRGB());

			FontHelper.textCentre("Info 1", xSize, 6, LogisticsColours.white_text);
			FontHelper.textCentre("Info 2", xSize, 60, LogisticsColours.white_text);

			break;
		case STRING:
			uuid1 = (InfoUUID) (infoPos == 0 ? currentFilter.uuid1.getObject() : currentFilter.uuid2.getObject());
			if (uuid1 != null) {
				IInfo monitorInfo = PL2.getClientManager().info.get(uuid1);
				if (monitorInfo != null) {
					FontHelper.textCentre("Info Type: " + monitorInfo.getID().toUpperCase(), xSize, 6, LogisticsColours.white_text.getRGB());
					GlStateManager.scale(0.75, 0.75, 0.75);
					FontHelper.textCentre(monitorInfo.toString(), (int) (xSize * (1 / 0.75)), (int) (18 * (1 / 0.75)), LogisticsColours.white_text.getRGB());
					GlStateManager.scale(1 / 0.75, 1 / 0.75, 1 / 0.75);
				}
			}
			break;
		default:
			break;

		}
		super.drawGuiContainerForegroundLayer(x, y);
		// FontHelper.textCentre(FontHelper.translate("Channel Selection"), xSize, 6, LogisticsColours.white_text);
		// FontHelper.textCentre(String.format("Select the channels you wish to monitor"), xSize, 18, LogisticsColours.grey_text);
	}

	@Override
	public void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
		super.drawGuiContainerBackgroundLayer(var1, var2, var3);
		if (state == GuiState.STATEMENT) {
			// this.drawHorizontalLine(guiLeft + 2, guiLeft + xSize - 3, guiTop + ySize / 2, LogisticsColours.blue_overlay.getRGB());

			drawTransparentRect(this.guiLeft + 4, this.guiTop + 4, this.guiLeft + xSize - 4, this.guiTop + 56, LogisticsColours.layers[2].getRGB());

			drawTransparentRect(this.guiLeft + 4, this.guiTop + 140, this.guiLeft + xSize - 4, this.guiTop + 162, LogisticsColours.layers[1].getRGB());

			drawTransparentRect(this.guiLeft + 4, this.guiTop + 58, this.guiLeft + xSize - 4, this.guiTop + 138, LogisticsColours.layers[2].getRGB());
		}

		RenderHelper.restoreBlendState();

	}

	@Override
	public int getColour(int i, int type) {
		return LogisticsColours.getDefaultSelection().getRGB();
	}

	@Override
	public boolean isPairedInfo(Object info) {
		if (info instanceof INetworkReader) {
			if (!RenderBlockSelection.positions.isEmpty()) {
				if (RenderBlockSelection.isPositionRenderered(((INetworkReader) info).getCoords())) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean isSelectedInfo(Object info) {
		switch (state) {
		case CHANNELS:
			if (info != null && info instanceof InfoUUID && currentFilter != null) {
				if (infoPos == 0)
					return currentFilter.uuid1.getObject() != null && currentFilter.uuid1.getObject().equals(info);
				else
					return currentFilter.uuid2.getObject() != null && currentFilter.uuid2.getObject().equals(info);
			}
			break;
		case LIST:
			break;
		case STATEMENT:
			break;
		case STRING:
			if (info != null && info instanceof ComparableObject && currentFilter != null) {
				if (infoPos == 0)
					return currentFilter.key1.getObject() != null && currentFilter.key1.getObject().equals(((ComparableObject) info).string);
				else
					return currentFilter.key2.getObject() != null && currentFilter.key2.getObject().equals(((ComparableObject) info).string);
			}
			break;
		default:
			break;

		}

		return false;
	}

	@Override
	public boolean isCategoryHeader(Object info) {
		if (info instanceof EmitterStatement) {
			return info == currentFilter;
		}

		return info instanceof IInfoProvider;
	}

	@Override
	public void renderInfo(Object info, int yPos) {
		switch (state) {
		case CHANNELS:
			if (info instanceof InfoUUID) {
				IInfo monitorInfo = PL2.getClientManager().info.get((InfoUUID) info);
				if (monitorInfo != null) {
					InfoRenderer.renderMonitorInfoInGUI(monitorInfo, yPos + 1, LogisticsColours.white_text.getRGB());
				} else {

					FontHelper.text("-", InfoRenderer.identifierLeft, yPos, LogisticsColours.white_text.getRGB());
				}
			} else if (info instanceof IInfoProvider) {
				IInfoProvider monitor = (IInfoProvider) info;
				InfoRenderer.renderMonitorInfoInGUI(new MonitoredBlockCoords(monitor.getCoords(), monitor.getDisplayName()), yPos + 1, LogisticsColours.white_text.getRGB());
			}
			break;
		case LIST:
			if (info instanceof EmitterStatement) {
				EmitterStatement statement = (EmitterStatement) info;
				String infoType1 = "INFO";
				String infoType2 = "INFO";
				String infoObj1 = "NULL";
				String infoObj2 = "NULL";
				if (statement.uuid1.getObject() != null) {
					Pair<String, String> infoStrings = getInfoTypeAndObjectStrings((InfoUUID) statement.uuid1.getObject(), statement.key1.getObject());
					infoType1 = infoStrings.a;
					infoObj1 = infoStrings.b;
				}
				if (statement.getInputType().usesInfo()) {
					if (statement.uuid2.getObject() != null) {
						Pair<String, String> infoStrings = getInfoTypeAndObjectStrings((InfoUUID) statement.uuid2.getObject(), statement.key2.getObject());
						infoType2 = infoStrings.a;
						infoObj2 = infoStrings.b;
					}
				} else {
					if (statement.getInputType() != null) {
						infoType2 = statement.getInputType().name();
					}
					if (statement.obj.get() != null) {
						infoObj2 = statement.obj.get().toString();
					}
				}
				FontHelper.text(infoType1 + ": " + TextFormatting.WHITE + TextFormatting.ITALIC + infoObj1 + " " + TextFormatting.DARK_AQUA + ((LogicOperator) statement.operator.getObject()).operation + TextFormatting.RESET + " " + infoType2 + ": " + TextFormatting.WHITE + TextFormatting.ITALIC + infoObj2, InfoRenderer.identifierLeft, yPos, LogisticsColours.white_text.getRGB());
				FontHelper.text("Current State: " + (statement.wasTrue.getObject() ? TextFormatting.GREEN : TextFormatting.RED) + statement.wasTrue.getObject(), InfoRenderer.identifierLeft, yPos + 14, LogisticsColours.white_text.getRGB());

			}
			break;
		case STATEMENT:
			break;
		case STRING:
			if (info instanceof ComparableObject) {
				ComparableObject comparable = (ComparableObject) info;
				FontHelper.text(comparable.string, InfoRenderer.identifierLeft, yPos, LogisticsColours.white_text.getRGB());
				if (comparable.object != null) {
					if (comparable.object instanceof Item) {
						FontHelper.text(((MonitoredItemStack) comparable.source).getItemStack().getDisplayName(), InfoRenderer.objectLeft, yPos, LogisticsColours.white_text.getRGB());
					} else {
						FontHelper.text(comparable.object.toString(), InfoRenderer.objectLeft, yPos, LogisticsColours.white_text.getRGB());
					}
					FontHelper.text(ObjectType.getInfoType(comparable.object).toString().toLowerCase(), InfoRenderer.kindLeft, yPos, LogisticsColours.white_text.getRGB());

				} else {
					FontHelper.text("ERROR", InfoRenderer.objectLeft, yPos, LogisticsColours.white_text.getRGB());
				}
			}
			break;
		default:
			break;
		}
	}

	public Pair<String, String> getInfoTypeAndObjectStrings(InfoUUID id, String key) {
		String infoType = "INFO", infoObj = "NULL";
		IInfo monitorInfo = PL2.getClientManager().info.get(id);
		if (monitorInfo instanceof INameableInfo) {
			infoType = ((INameableInfo) monitorInfo).getClientIdentifier();
		} else {
			infoType = monitorInfo != null ? monitorInfo.toString() : "INFO 1";
		}
		if (monitorInfo instanceof IComparableInfo && key != null) {
			List<ComparableObject> infoList = ((IComparableInfo) monitorInfo).getComparableObjects(Lists.newArrayList());
			ComparableObject obj = ComparableObject.getComparableObject(infoList, key);
			if (obj != null && obj.object != null) {
				infoObj = obj.object.toString();
			}
		}
		return new Pair(infoType, infoObj);
	}

	@Override
	public void selectionPressed(GuiButton button, int pos, int buttonID, Object info) {
		switch (state) {
		case LIST:
			if (info instanceof EmitterStatement) {
				EmitterStatement statement = (EmitterStatement) info;
				if (buttonID == 1) {
					this.currentFilter = statement;
					lastFilter = statement;
					this.changeState(GuiState.STATEMENT);
				} else {
					currentFilter = info != currentFilter ? statement : null;
				}
			}
			break;
		case STATEMENT:
			break;
		case CHANNELS:
			if (buttonID == 0 && info instanceof InfoUUID) {
				if (infoPos == 0) {
					currentFilter.uuid1.setObject((InfoUUID) info);
				} else {
					currentFilter.uuid2.setObject((InfoUUID) info);
				}
				/* part.container().setUUID((InfoUUID) info, infoID); part.currentSelected = infoID; tile.sendByteBufPacket(0); */
			} else if (info instanceof INetworkReader) {
				RenderBlockSelection.addPosition(((INetworkReader) info).getCoords(), false);
			}
			break;
		case STRING:
			if (info instanceof ComparableObject) {
				ComparableObject obj = (ComparableObject) info;
				((infoPos == 0) ? currentFilter.key1 : currentFilter.key2).setObject(obj.string);
			}
			break;
		default:
			break;

		}
	}

	@Override
	public void mouseClicked(int x, int y, int button) throws IOException {
		if (coolDown != 0) {
			return;
		}
		super.mouseClicked(x, y, button);
	}

	@Override
	public void setInfo() {
		switch (state) {
		case LIST:
			infoList = Lists.newArrayList(tile.getStatements().getObjects());
			break;
		case STATEMENT:
			break;
		case CHANNELS:
			infoList = Lists.newArrayList(PL2.getClientManager().sortedLogicMonitors.getOrDefault(tile.getIdentity(), Lists.newArrayList()));
			break;
		case STRING:
			InfoUUID uuid = (InfoUUID) (infoPos == 0 ? currentFilter.uuid1.getObject() : currentFilter.uuid2.getObject());
			if (uuid != null) {
				IInfo monitorInfo = PL2.getClientManager().info.get(uuid);
				if (monitorInfo != null && monitorInfo instanceof IComparableInfo) {
					IComparableInfo comparable = (IComparableInfo) monitorInfo;
					List objects = new ArrayList<ComparableObject>();
					comparable.getComparableObjects(objects);
					infoList = objects;
					break;
				}
			}
			infoList = Lists.newArrayList();
			break;
		default:
			break;

		}
	}

	public void onTextFieldChanged(SonarTextField field) {
		super.onTextFieldChanged(field);
		if (field == inputField) {
			switch (currentFilter.getInputType()) {
			case INFO:
				break;
			case NUMBER:
				currentFilter.obj.set(field.getText().isEmpty() ? 0 : Double.valueOf(field.getText()), ObjectType.DOUBLE);
				break;
			case STRING:
				currentFilter.obj.set(field.getText(), ObjectType.STRING);
				break;
			default:

			}
		}

	}
	
	@Override
	protected void keyTyped(char c, int i) throws IOException {
		if (this.getFocusedField() == null && state != GuiState.LIST && (i == 1 || this.mc.gameSettings.keyBindInventory.isActiveAndMatches(i))) {
			if (state == GuiState.CHANNELS || state == GuiState.STRING) {
				changeState(GuiState.STATEMENT);
				return;
			}
			changeState(GuiState.LIST);
			return;
		} else {
			super.keyTyped(c, i);
		}
	}

}
