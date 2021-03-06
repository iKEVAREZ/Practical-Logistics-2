package sonar.logistics.common.items;

import mcmultipart.item.ItemMultiPart;
import mcmultipart.multipart.IMultipart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import sonar.logistics.common.multiparts.AbstractDisplayPart;

public class ItemScreenMultipart extends ItemMultiPart {
	public final Class<? extends AbstractDisplayPart> type;

	public ItemScreenMultipart(Class<? extends AbstractDisplayPart> type) {
		this.type = type;
	}

	@Override
	public IMultipart createPart(World world, BlockPos pos, EnumFacing side, Vec3d hit, ItemStack stack, EntityPlayer player) {
		try {
			EnumFacing facing = side;
			EnumFacing rotation = EnumFacing.NORTH;
			if(player.rotationPitch>75 || player.rotationPitch<-75){
				rotation = player.getHorizontalFacing().getOpposite();
			}else{
				facing = player.getHorizontalFacing().getOpposite();
			}
			
			return type.getConstructor(EnumFacing.class, EnumFacing.class).newInstance(facing, rotation);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
