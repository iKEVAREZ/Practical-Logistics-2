package sonar.logistics.api.info.render;

import java.util.ArrayList;

import com.google.common.collect.Lists;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import sonar.core.helpers.NBTHelper;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.network.sync.IDirtyPart;
import sonar.core.network.sync.ISyncableListener;
import sonar.core.network.sync.SyncNBTAbstract;
import sonar.core.network.sync.SyncPart;
import sonar.core.network.sync.SyncTagTypeList;
import sonar.core.network.sync.SyncableList;
import sonar.core.utils.CustomColour;
import sonar.logistics.PL2;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.INameableInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.render.RenderInfoProperties;
import sonar.logistics.api.tiles.displays.DisplayConstants;
import sonar.logistics.client.LogisticsColours;

/** default implementation of the Display Info used on displays */
public class DisplayInfo extends SyncPart implements IDisplayInfo, ISyncableListener {

	public RenderInfoProperties renderInfo;
	public IInfo cachedInfo = null;
	public SyncTagTypeList<String> formatList = new SyncTagTypeList(NBT.TAG_STRING, 0);
	public SyncNBTAbstract<InfoUUID> uuid = new SyncNBTAbstract<InfoUUID>(InfoUUID.class, 1);
	public SyncNBTAbstract<CustomColour> textColour = new SyncNBTAbstract<CustomColour>(CustomColour.class, 2), backgroundColour = new SyncNBTAbstract<CustomColour>(CustomColour.class, 3);
	public InfoContainer container;
	public boolean isList = false;

	public SyncableList syncParts = new SyncableList(this);
	{
		textColour.setObject(LogisticsColours.white_text);
		backgroundColour.setObject(LogisticsColours.grey_base);
		syncParts.addParts(formatList, uuid, textColour, backgroundColour);
		MinecraftForge.EVENT_BUS.register(this);
	}

	public DisplayInfo(InfoContainer container, int id) {
		super(id);
		this.container = container;
	}

	public RenderInfoProperties setRenderInfoProperties(RenderInfoProperties renderInfo, int pos) {
		this.renderInfo = renderInfo;
		if (cachedInfo != null) {
			cachedInfo.renderSizeChanged(container, this, renderInfo.scaling[0], renderInfo.scaling[1], renderInfo.scaling[2], pos);
		}
		return renderInfo;
	}

	public void setUUID(InfoUUID infoUUID) {
		uuid.setObject(infoUUID);
	}

	@Override
	public IInfo getSidedCachedInfo(boolean isClient) {
		InfoUUID id = getInfoUUID();
		if (id == null)
			return null;
		if (cachedInfo == null) {
			cachedInfo = PL2.getInfoManager(isClient).getInfoList().get(id);
			cachedInfo = cachedInfo != null ? cachedInfo.copy() : null;
		}
		return cachedInfo;
	}

	public void setCachedInfo(IInfo info) {
		this.cachedInfo = info.copy();//we copy it so page counts work and to avoid stuff being altered
	}

	@Override
	public CustomColour getTextColour() {
		return textColour.getObject();
	}

	@Override
	public CustomColour getBackgroundColour() {
		return backgroundColour.getObject();
	}

	@Override
	public InfoUUID getInfoUUID() {
		return uuid.getObject();
	}

	@Override
	public void readData(NBTTagCompound nbt, SyncType type) {
		NBTTagCompound tag = nbt.getCompoundTag(this.getTagName());
		if (!tag.hasNoTags())
			NBTHelper.readSyncParts(tag, type, syncParts);
	}

	@Override
	public NBTTagCompound writeData(NBTTagCompound nbt, SyncType type) {
		NBTTagCompound tag = NBTHelper.writeSyncParts(new NBTTagCompound(), type, syncParts, type == SyncType.SYNC_OVERRIDE);
		if (!tag.hasNoTags())
			nbt.setTag(this.getTagName(), tag);
		return nbt;
	}

	@Override
	public RenderInfoProperties getRenderProperties() {
		return renderInfo;
	}

	@Override
	public void setFormatStrings(ArrayList<String> strings) {
		formatList.setObjects(strings);
	}

	@Override
	public ArrayList<String> getUnformattedStrings() {
		return formatList.getObjects();
	}

	@Override
	public ArrayList<String> getFormattedStrings() {
		ArrayList<String> format = Lists.newArrayList();
		boolean empty = true;
		for (String string : formatList.getObjects()) {
			if (!string.isEmpty()) {
				empty = false;
				format.add(DisplayConstants.formatText(string, this));
			}
		}

		if (empty) {
			IInfo info = getSidedCachedInfo(true);
			if (info == null) {
				info = cachedInfo;
			}
			if (info != null && info instanceof INameableInfo) {
				INameableInfo cachedInfo = (INameableInfo) info;
				if (!cachedInfo.getClientIdentifier().isEmpty())
					format.add(cachedInfo.getClientIdentifier());
				format.add(cachedInfo.getClientObject());
			}
		}
		return format;
	}

	@Override
	public void markChanged(IDirtyPart part) {
		syncParts.markSyncPartChanged(part);
		container.markChanged(this);

	}

	@Override
	public void writeToBuf(ByteBuf buf) {
		ByteBufUtils.writeTag(buf, this.writeData(new NBTTagCompound(), SyncType.SAVE));
	}

	@Override
	public void readFromBuf(ByteBuf buf) {
		readData(ByteBufUtils.readTag(buf), SyncType.SAVE);
	}

}
