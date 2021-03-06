package sonar.logistics.info.types;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;
import sonar.core.api.nbt.INBTSyncable;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.logistics.PL2Constants;
import sonar.logistics.api.asm.LogicInfoType;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.INameableInfo;
import sonar.logistics.api.info.ISuffixable;
import sonar.logistics.api.info.render.IDisplayInfo;
import sonar.logistics.api.info.render.InfoContainer;
import sonar.logistics.api.networks.INetworkHandler;
import sonar.logistics.api.register.LogicPath;
import sonar.logistics.helpers.InfoHelper;
import sonar.logistics.helpers.InfoRenderer;

@LogicInfoType(id = ProgressInfo.id, modid = PL2Constants.MODID)
public class ProgressInfo implements IInfo<ProgressInfo>, INBTSyncable, INameableInfo<ProgressInfo>, ISuffixable {

	public static final String id = "progress";
	public LogicInfo first, second;
	public int compare;
	public double firstNum, secondNum;

	public ProgressInfo() {}

	public ProgressInfo(IInfo first, IInfo second) {
		this.first = (LogicInfo) first;
		this.second = (LogicInfo) second;
		checkInfo();
	}

	public static boolean isStorableInfo(IInfo info) {
		return info != null && info instanceof LogicInfo;
	}

	public void checkInfo() {
		if (isValid() && first.getInfoType().isNumber() && second.getInfoType().isNumber()) {
			firstNum = Double.valueOf(first.getInfo().toString());
			secondNum = Double.valueOf(second.getInfo().toString());
			compare = Double.compare(firstNum, secondNum);
		}
	}

	@Override
	public String getClientIdentifier() {
		if (!isValid()) {
			return "ERROR";
		}
		return (compare == 1 ? second : first).getClientIdentifier();
	}

	@Override
	public String getRawData() {
		if (!isValid()) {
			return "ERROR";
		}
		return (compare == 1 ? second : first).getRawData();
	}

	@Override
	public String getClientObject() {
		if (!isValid()) {
			return "ERROR";
		}
		return (compare == 1 ? second : first).getClientObject(); // + "/" + (compare != 1 ? second : first).getClientObject();
	}

	@Override
	public String getClientType() {
		return "Progress";
	}

	@Override
	public String getSuffix() {
		if (!isValid()) {
			return "ERROR";
		}
		return (compare == 1 ? second : first).getSuffix();
	}

	@Override
	public String getPrefix() {
		if (!isValid()) {
			return "ERROR";
		}
		return (compare == 1 ? second : first).getPrefix();
	}

	@Override
	public void readData(NBTTagCompound nbt, SyncType type) {
		first = (LogicInfo) InfoHelper.loadInfo(InfoHelper.getName(LogicInfo.id), nbt.getCompoundTag("first"));
		second = (LogicInfo) InfoHelper.loadInfo(InfoHelper.getName(LogicInfo.id), nbt.getCompoundTag("second"));
		checkInfo();
	}

	@Override
	public NBTTagCompound writeData(NBTTagCompound nbt, SyncType type) {
		nbt.setTag("first", InfoHelper.writeInfoToNBT(new NBTTagCompound(), first, type));
		nbt.setTag("second", InfoHelper.writeInfoToNBT(new NBTTagCompound(), second, type));
		return nbt;
	}

	@Override
	public boolean isIdenticalInfo(ProgressInfo info) {
		return info.first.isIdenticalInfo(first) && info.second.isIdenticalInfo(second);
	}

	@Override
	public boolean isMatchingInfo(ProgressInfo info) {
		return info.first.isMatchingInfo(first) && info.second.isMatchingInfo(second);
	}

	@Override
	public boolean isMatchingType(IInfo info) {
		return info instanceof ProgressInfo;
	}

	@Override
	public boolean isHeader() {
		return false;
	}

	@Override
	public INetworkHandler getHandler() {
		return null;
	}

	@Override
	public boolean isValid() {
		return first != null && second != null && first.isValid() && second.isValid();
	}

	@Override
	public String getID() {
		return id;
	}

	@Override
	public ProgressInfo copy() {
		return new ProgressInfo(first.copy(), second.copy());
	}

	@Override
	public void renderInfo(InfoContainer container, IDisplayInfo displayInfo, double displayWidth, double displayHeight, double displayScale, int infoPos) {
		GL11.glPushMatrix();
		GL11.glPushMatrix();
		GlStateManager.disableLighting();
		GL11.glTranslated(-1, -+0.0625 * 12, +0.004);
		Minecraft.getMinecraft().getTextureManager().bindTexture(InfoContainer.getColour(infoPos));
		double num1 = (compare == 1 ? secondNum : firstNum);
		double num2 = (compare == 1 ? firstNum : secondNum);
		InfoRenderer.renderProgressBar(displayWidth, displayHeight, displayScale, num1 < 0 ? 0 : num1, num2);
		GlStateManager.enableLighting();
		GL11.glTranslated(0, 0, -0.001);
		GL11.glPopMatrix();
		InfoRenderer.renderNormalInfo(container.display.getDisplayType(), displayWidth, displayHeight, displayScale, displayInfo.getFormattedStrings());
		GL11.glPopMatrix();

	}

	@Override
	public void identifyChanges(ProgressInfo newInfo) {
		first.identifyChanges(newInfo.first);
		second.identifyChanges(newInfo.second);
	}

	@Override
	public LogicPath getPath() {
		return null;
	}

	@Override
	public ProgressInfo setPath(LogicPath path) {
		return this;
	}

	@Override
	public void renderSizeChanged(InfoContainer container, IDisplayInfo displayInfo, double width, double height, double scale, int infoPos) {}

	@Override
	public void onInfoStored() {
		first.onInfoStored();
		second.onInfoStored();
	}

}