package sonar.logistics.helpers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

import org.lwjgl.opengl.GL11;

import sonar.logistics.api.Info;
import sonar.logistics.api.wrappers.RenderWrapper;

public class InfoRenderer extends RenderWrapper {

	public void renderStandardInfo(Info info, FontRenderer rend, float minX, float minY, float maxX, float maxY, float zOffset, float scale) {
		GL11.glTranslatef(minX + (maxX - minX) / 2, minY + (maxY - minY) / 2, 0.01f);
		int sizing = Math.round(Math.min((maxX - minX), (maxY - minY) * 3));
		GL11.glTranslatef(0.0f, (float) (scale >= 120 ? -0.1F : -0.2F + ((sizing - 1) * -0.01)), zOffset);
		double itemScale = sizing >= 2 ? getScale(sizing) : 120;
		GL11.glScaled(1.0f / itemScale, 1.0f / itemScale, 1.0f / itemScale);
		String category = info.getSubCategory();
		String data = info.getDisplayableData();
		if (category.isEmpty() || category.equals(" ")) {
			rend.drawString(data, -rend.getStringWidth(data) / 2, -4, -1);
		} else {
			rend.drawString(category, -rend.getStringWidth(category) / 2, -8, -1);
			rend.drawString(data, -rend.getStringWidth(data) / 2, 4, -1);
		}
	}

	public void renderCenteredString(String string, float minX, float minY, float maxX, float maxY, float scale) {
		FontRenderer rend = Minecraft.getMinecraft().fontRenderer;
		GL11.glTranslatef(minX + (maxX - minX) / 2, minY + (maxY - minY) / 2, 0.01f);
		int sizing = Math.round(Math.min((maxX - minX), (maxY - minY) * 3));
		GL11.glTranslatef(0.0f, (float) (scale >= 120 ? -0.08F : -0.2F + ((sizing - 1) * 0.001)), 0);
		double itemScale = sizing >= 2 ? getScale(sizing) : 120;
		GL11.glScaled(1.0f / itemScale, 1.0f / itemScale, 1.0f / itemScale);
		rend.drawString(string, -rend.getStringWidth(string) / 2, -4, -1);
	}
	

	public double getScale(int sizing) {
		switch (sizing) {
		case 0 & 1:
			return 120;
		case 2:
			return 70;
		case 3:
			return 40;
		case 4:
			return 30;
		case 5:
			return 22;
		default:
			return 18;
		}

	}
}