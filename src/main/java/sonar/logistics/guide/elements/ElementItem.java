package sonar.logistics.guide.elements;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import sonar.core.helpers.RenderHelper;
import sonar.logistics.client.LogisticsColours;
import sonar.logistics.client.gui.GuiGuide;
import sonar.logistics.guide.IGuidePage;
import sonar.logistics.guide.IGuidePageElement;

public class ElementItem implements IGuidePageElement {

	public int page;
	public ItemStack stack;
	public int x, y;
	public static final int SIZE = 80;

	public ElementItem(int page, ItemStack stack, int x, int y) {
		this.page = page;
		this.stack = stack.copy();
		this.x = x;
		this.y = y;
	}

	@Override
	public int[] getSizing() {
		return new int[] { x, y, SIZE, SIZE};
	}

	@Override
	public void drawElement(GuiGuide gui, int x, int y, int page, int mouseX, int mouseY) {
	}

	@Override
	public void drawForegroundElement(GuiGuide gui, int x, int y, int page, int mouseX, int mouseY) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		net.minecraft.client.renderer.RenderHelper.enableGUIStandardItemLighting();
		GlStateManager.pushMatrix();
		GlStateManager.pushAttrib();
		GlStateManager.enableDepth();
		double scale = (double) SIZE / 16;
		GlStateManager.translate(x, y, 640);
		GlStateManager.scale(scale, scale, scale);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		RenderHelper.renderItemIntoGUI(stack, 0, 0);
		RenderHelper.renderStoredItemStackOverlay(stack, 0, 0, 0, null, false);
		GlStateManager.popAttrib();
		GlStateManager.popMatrix();

	}

	@Override
	public void drawBackgroundElement(GuiGuide gui, int x, int y, int page, int mouseX, int mouseY) {
		RenderHelper.saveBlendState();
		gui.drawTransparentRect(x, y, x + SIZE, y + SIZE, LogisticsColours.blue_overlay.getRGB());
		RenderHelper.restoreBlendState();

	}

	@Override
	public boolean mouseClicked(GuiGuide gui, IGuidePage page, int x, int y, int button) {
		return false;
	}

	@Override
	public int getDisplayPage() {
		return page;
	}

}
