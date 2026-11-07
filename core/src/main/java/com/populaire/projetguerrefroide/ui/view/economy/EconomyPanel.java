package com.populaire.projetguerrefroide.ui.view.economy;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;
import com.populaire.projetguerrefroide.protocol.request.SortType;
import com.populaire.projetguerrefroide.protocol.response.BuildingDetails;
import com.populaire.projetguerrefroide.protocol.response.RegionsBuildings;
import com.populaire.projetguerrefroide.ui.UiCatalog;
import com.populaire.projetguerrefroide.ui.format.TextFormat;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.FontsSkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.Skins;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiEconomySkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiSkin;
import com.populaire.projetguerrefroide.ui.widget.UiAnchor;
import com.populaire.projetguerrefroide.ui.widget.UiBuilder;
import com.populaire.projetguerrefroide.ui.widget.UiList;
import com.populaire.projetguerrefroide.ui.widget.UiWindow;

public class EconomyPanel extends UiWindow {
    private final Image bgMainshadow;
    private final Image bgEconomyTrade;
    private final Image economyPaper;
    private final Button automateCheckbox;
    private final Button closeButton;
    private final Label headline;
    private final Button sliderCheckbox;
    private final Label sliderCheckboxLabel;
    private final Button aiCheckbox;
    private final Label aiCheckboxLabel;
    private final Button resourceCheckbox;
    private final Label resourceCheckboxLabel;
    private final Button dIButton;
    private final Button populationButton;
    private final Button workersButton;
    private final Button emptyRegionCheckbox;
    private final UiList<RegionBlock> regionsListbox;
    private final EconomyResourceView resourceView;
    private final Label resourceName;
    private final Label resourcePrice;
    private final EconomySliderView sliderView;
    private final EconomyAiView aiView;
    private final FactoryInfo factoryInfo;
    private final FactoryBuild factoryBuild;
    private final EconomyDrawables drawables;
    private final boolean[] collapsedRegions;
    private UiWindow shownView;

    public EconomyPanel(UiBuilder ui, UiCatalog uiCatalog, TextFormat textFormat, EconomyPanelListener listener) {
        super(ui, 1120f, 768f);
        this.drawables = new EconomyDrawables(ui, uiCatalog.catalog());
        this.collapsedRegions = new boolean[uiCatalog.catalog().regions().size()];
        Skin economySkin = ui.skin(Skins.UI_ECONOMY);
        Skin uiSkin = ui.skin(Skins.UI);
        this.bgMainshadow = ui.image(this, economySkin, UiEconomySkin.Drawables.ECONOMY_BG_SHADOW, -22f, -6f, UiAnchor.UPPER_LEFT);
        this.bgEconomyTrade = ui.image(this, economySkin, UiEconomySkin.Drawables.BG_ECONOMY_TRADE, -22f, -5f, UiAnchor.UPPER_LEFT);
        this.economyPaper = ui.image(this, economySkin, UiEconomySkin.Drawables.ECONOMY_PAPER, 38f, 554f, UiAnchor.UPPER_LEFT);
        this.automateCheckbox = ui.button(this, economySkin, UiEconomySkin.Styles.Button.ECO_AI_TGL, 61f, 75f, UiAnchor.UPPER_LEFT);
        this.closeButton = ui.button(this, uiSkin, UiSkin.Styles.Button.CLOSE_BTN, 1035f, 29f, UiAnchor.UPPER_LEFT);
        ui.shortcut(this, this.closeButton, Input.Keys.ESCAPE);
        this.headline = ui.label(this, "ECONOMY", FontsSkin.Styles.Label.JOCKEY_30_GLOW_GREEN, 45f, 30f, -1f, 64f, Align.topLeft, UiAnchor.UPPER_LEFT);
        this.sliderCheckbox = ui.button(this, economySkin, UiEconomySkin.Styles.Button.ECO_ECO_TRADE_BTN, 809f, 70f, UiAnchor.UPPER_LEFT);
        ui.buttonLabel(this.sliderCheckbox, "ECONOMY", FontsSkin.Styles.Label.ARIAL_11_BOLD_BLACK).setVisible(false);
        this.sliderCheckboxLabel = ui.label(this, "ECONOMY", FontsSkin.Styles.Label.JOCKEY_16_DARK, 803f, 75f, 90f, 28f, Align.top, UiAnchor.UPPER_LEFT, 0f, 4f);
        this.aiCheckbox = ui.button(this, economySkin, UiEconomySkin.Styles.Button.ECO_TAB_AI_BTN, 900f, 70f, UiAnchor.UPPER_LEFT);
        ui.buttonLabel(this.aiCheckbox, "FOCUS", FontsSkin.Styles.Label.ARIAL_11_BOLD_BLACK).setVisible(false);
        this.aiCheckboxLabel = ui.label(this, "FOCUS", FontsSkin.Styles.Label.JOCKEY_16_DARK, 893f, 75f, 80f, 28f, Align.top, UiAnchor.UPPER_LEFT, 0f, 4f);
        this.resourceCheckbox = ui.button(this, economySkin, UiEconomySkin.Styles.Button.ECO_TAB_AI_BTN, 978f, 70f, UiAnchor.UPPER_LEFT);
        ui.buttonLabel(this.resourceCheckbox, "RESOURCES", FontsSkin.Styles.Label.ARIAL_11_BOLD_BLACK).setVisible(false);
        this.resourceCheckboxLabel = ui.label(this, "TRADE", FontsSkin.Styles.Label.JOCKEY_16_DARK, 971f, 75f, 80f, 28f, Align.top, UiAnchor.UPPER_LEFT, 0f, 4f);
        // name_button : GFX_filter_all (sans asset PGF)
        this.dIButton = ui.button(this, economySkin, UiEconomySkin.Styles.Button.ICON_DI_BTN, 505f, 78f, UiAnchor.UPPER_LEFT);
        this.populationButton = ui.button(this, economySkin, UiEconomySkin.Styles.Button.ICON_POP_BTN, 575f, 78f, UiAnchor.UPPER_LEFT);
        this.workersButton = ui.button(this, economySkin, UiEconomySkin.Styles.Button.ICON_WORKFORCE_BTN, 662f, 78f, UiAnchor.UPPER_LEFT);
        this.emptyRegionCheckbox = ui.button(this, economySkin, UiEconomySkin.Styles.Button.ECO_REGIONAL_TGL, 751f, 75f, UiAnchor.UPPER_LEFT);
        this.regionsListbox = ui.list(this, -210f, 108f, 980f, 450f, UiAnchor.UPPER_LEFT, 0f, 0f, 0f, rowUi -> new RegionBlock(rowUi, uiCatalog, textFormat, this.drawables, listener, this.collapsedRegions));
        this.resourceView = ui.add(this, new EconomyResourceView(ui), 784f, 64f, UiAnchor.UPPER_LEFT);
        this.resourceView.setVisible(false);
        this.resourceName = ui.label(this, "", FontsSkin.Styles.Label.JOCKEY_16_GLOW_BLUE, 811f, 105f, 150f, 28f, Align.topLeft, UiAnchor.UPPER_LEFT, 0f, 4f);
        this.resourcePrice = ui.label(this, "", FontsSkin.Styles.Label.JOCKEY_16_GLOW_BLUE, 830f, 105f, 200f, 28f, Align.topRight, UiAnchor.UPPER_LEFT, 0f, 4f);
        this.sliderView = ui.add(this, new EconomySliderView(ui), 786f, 64f, UiAnchor.UPPER_LEFT);
        this.sliderView.setVisible(false);
        this.aiView = ui.add(this, new EconomyAiView(ui), 784f, 64f, UiAnchor.UPPER_LEFT);
        this.aiView.setVisible(false);
        this.factoryInfo = ui.add(this, new FactoryInfo(ui, uiCatalog, this.drawables, listener), 177f, 370f, UiAnchor.UPPER_LEFT);
        this.factoryInfo.setVisible(false);
        this.factoryBuild = ui.add(this, new FactoryBuild(ui), -10f, 0f, UiAnchor.UPPER_LEFT);
        this.factoryBuild.setVisible(false);
        this.economyPaper.setVisible(false);
        this.showView(this.sliderView);
        ui.onClick(this.sliderCheckbox, () -> this.showView(this.sliderView));
        ui.onClick(this.aiCheckbox, () -> this.showView(this.aiView));
        ui.onClick(this.resourceCheckbox, () -> this.showView(this.resourceView));
        ui.onClick(this.dIButton, () -> listener.onSortRegions(SortType.DEVELOPPEMENT_INDEX));
        ui.onClick(this.populationButton, () -> listener.onSortRegions(SortType.POPULATION));
        ui.onClick(this.workersButton, () -> listener.onSortRegions(SortType.WORKFORCE));
        ui.onClick(this.closeButton, listener::onCloseEconomyPanelClicked);
    }

    public void setData(RegionsBuildings regions) {
        int regionCount = regions.regionCount();
        this.regionsListbox.setRowCount(regionCount);
        for (int position = 0; position < regionCount; position++) {
            this.regionsListbox.row(position).bind(regions, position);
        }
    }

    public void updateSelectedBuildingInfoBlock(BuildingDetails building) {
        this.factoryInfo.bind(building);
        this.economyPaper.setVisible(true);
        this.factoryInfo.setVisible(true);
    }

    public long getSelectedBuildingId() {
        return this.factoryInfo.isVisible() ? this.factoryInfo.buildingId() : 0;
    }

    public void clearSelectedBuildingInfoBlock() {
        this.economyPaper.setVisible(false);
        this.factoryInfo.setVisible(false);
    }

    private void showView(UiWindow view) {
        if (this.shownView != null) {
            this.shownView.setVisible(false);
        }
        view.setVisible(true);
        this.shownView = view;
    }
}
