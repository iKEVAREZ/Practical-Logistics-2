package sonar.logistics.network;

import java.util.UUID;

import io.netty.buffer.ByteBuf;
import mcmultipart.multipart.IMultipart;
import mcmultipart.multipart.IMultipartContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import sonar.core.SonarCore;
import sonar.core.network.PacketMultipart;
import sonar.core.network.PacketMultipartHandler;
import sonar.logistics.api.networks.ILogisticsNetwork;
import sonar.logistics.common.multiparts.readers.InventoryReaderPart;
import sonar.logistics.helpers.ItemHelper;

/** called when the player clicks an item in the inventory reader */
public class PacketInventoryReader extends PacketMultipart {

	public ItemStack selected;
	public int button;

	public PacketInventoryReader() {}

	public PacketInventoryReader(UUID partUUID, BlockPos pos, ItemStack selected, int button) {
		super(partUUID, pos);
		this.selected = selected;
		this.button = button;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		super.fromBytes(buf);
		if (buf.readBoolean()) {
			this.selected = ByteBufUtils.readItemStack(buf);
		}
		this.button = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		super.toBytes(buf);
		if (selected != null) {
			buf.writeBoolean(true);
			ByteBufUtils.writeItemStack(buf, selected);
		} else {
			buf.writeBoolean(false);
		}
		buf.writeInt(button);
	}

	public static class Handler extends PacketMultipartHandler<PacketInventoryReader> {
		@Override
		public IMessage processMessage(PacketInventoryReader message, IMultipartContainer target, IMultipart part, MessageContext ctx) {
			SonarCore.proxy.getThreadListener(ctx).addScheduledTask(new Runnable() {
				@Override
				public void run() {
					EntityPlayer player = SonarCore.proxy.getPlayerEntity(ctx);
					if (player == null || player.getEntityWorld().isRemote || !(part instanceof InventoryReaderPart)) {
						return;
					}
					InventoryReaderPart reader = (InventoryReaderPart) part;
					ILogisticsNetwork network = reader.getNetwork();
					if (network.isValid())
						ItemHelper.onNetworkItemInteraction(reader, network, reader.getMonitoredList(), player, message.selected, message.button);
				}
			});
			return null;
		}
	}
}