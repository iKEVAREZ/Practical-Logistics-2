package sonar.logistics.common.multiparts.generic;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import mcmultipart.MCMultiPartMod;
import mcmultipart.multipart.ISlottedPart;
import mcmultipart.multipart.MultipartHelper;
import mcmultipart.multipart.PartSlot;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import sonar.core.network.sync.SyncEnum;
import sonar.core.utils.Pair;
import sonar.logistics.PL2Multiparts;
import sonar.logistics.api.cabling.NetworkConnectionType;

public abstract class SidedMultipart extends LogisticsMultipart implements ISlottedPart {
	public double width, heightMin, heightMax;

	public SyncEnum<EnumFacing> face = new SyncEnum(EnumFacing.values(), -1);
	{
		syncList.addPart(face);
	}

	public SidedMultipart() {
		super();
		PL2Multiparts multipart = getMultipart();
		this.width = multipart.width;
		this.heightMin = multipart.heightMin;
		this.heightMax = multipart.heightMax;
	}

	public SidedMultipart setCableFace(EnumFacing face) {
		this.face.setObject(face);
		return this;
	}

	public EnumFacing getCableFace() {
		return face.getObject();
	}

	@Override
	public NetworkConnectionType canConnect(EnumFacing dir) {
		return dir != face.getObject() ? NetworkConnectionType.NETWORK : NetworkConnectionType.NONE;
	}

	//// MULTIPART \\\\

	@Override
	public EnumSet<PartSlot> getSlotMask() {
		return EnumSet.of(PartSlot.getFaceSlot(face.getObject()));
	}

	@Override
	public void addCollisionBoxes(AxisAlignedBB mask, List<AxisAlignedBB> list, Entity collidingEntity) {
		ArrayList<AxisAlignedBB> boxes = new ArrayList();
		addSelectionBoxes(boxes);
		boxes.forEach(box -> {
			if (box.intersectsWith(mask)) {
				list.add(box);
			}
		});
	}

	public void addSelectionBoxes(List<AxisAlignedBB> list) {
		double p = 0.0625;
		double w = (1 - width) / 2;
		switch (face.getObject()) {
		case DOWN:
			list.add(new AxisAlignedBB(w, heightMin, w, 1 - w, heightMax, 1 - w));
			break;
		case EAST:
			list.add(new AxisAlignedBB(1 - heightMax, w, w, 1 - heightMin, 1 - w, 1 - w));
			break;
		case NORTH:
			list.add(new AxisAlignedBB(w, w, heightMin, 1 - w, 1 - w, heightMax));
			break;
		case SOUTH:
			list.add(new AxisAlignedBB(w, w, 1 - heightMax, 1 - w, 1 - w, 1 - heightMin));
			break;
		case UP:
			list.add(new AxisAlignedBB(w, 1 - heightMax, w, 1 - w, 1 - heightMin, 1 - w));
			break;
		case WEST:
			list.add(new AxisAlignedBB(heightMin, w, w, heightMax, 1 - w, 1 - w));
			break;
		default:
			list.add(new AxisAlignedBB(w, heightMin, w, 1 - w, heightMax, 1 - w));
			break;
		}
	}

	@Override
	public EnumFacing[] getValidRotations() {
		return EnumFacing.VALUES;
	}

	@Override
	public boolean rotatePart(EnumFacing axis) {
		Pair<Boolean, EnumFacing> rotate = rotatePart(face.getObject(), axis);
		if (rotate.a) {
			if (isServer()) {
				UUID uuid = getUUID();
				BlockPos pos = getPos();
				World world = getWorld();
				getContainer().removePart(this);
				face.setObject(rotate.b);
				firstTick = false;
				MultipartHelper.addPart(world, pos, this, uuid);
				sendUpdatePacket(true);
			}
		}
		return rotate.a;
	}

	//// STATE \\\\

	@Override
	public IBlockState getActualState(IBlockState state) {
		return state.withProperty(ORIENTATION, face.getObject());
	}

	public BlockStateContainer createBlockState() {
		return new BlockStateContainer(MCMultiPartMod.multipart, new IProperty[] { ORIENTATION });
	}

	//// PACKETS \\\\

	@Override
	public void writeUpdatePacket(PacketBuffer buf) {
		super.writeUpdatePacket(buf);
		face.writeToBuf(buf);
	}

	@Override
	public void readUpdatePacket(PacketBuffer buf) {
		super.readUpdatePacket(buf);
		face.readFromBuf(buf);
	}
}