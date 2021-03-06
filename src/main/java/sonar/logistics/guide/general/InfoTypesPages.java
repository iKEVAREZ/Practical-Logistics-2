package sonar.logistics.guide.general;

import java.util.List;

import sonar.core.helpers.FontHelper;
import sonar.logistics.client.gui.GuiGuide;
import sonar.logistics.guide.BaseInfoPage;
import sonar.logistics.guide.elements.ElementInfo;

//W.I.P
public class InfoTypesPages extends BaseInfoPage {
	public String displayKey;

	public InfoTypesPages(int pageID, String displayKey) {
		super(pageID);
		this.displayKey = displayKey;
	}

	@Override
	public String getDisplayName() {
		return FontHelper.translate(displayKey);
	}

	@Override
	public List<ElementInfo> getPageInfo(GuiGuide gui, List<ElementInfo> pageInfo) {
		
		return pageInfo;
	}

}
