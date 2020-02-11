package com.FriendsExporter;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.WidgetMenuOptionClicked;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.RuneLite;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.menus.MenuManager;
import net.runelite.client.menus.WidgetMenuOption;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.Text;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;

@Slf4j
@PluginDescriptor(
	name = "Friends Exporter"
)
public class FriendsExporterPlugin extends Plugin
{
	private static final WidgetMenuOption FIXED_Friends_List;
	private static final WidgetMenuOption Resizable_Friends_List;
	private static final WidgetMenuOption Bottom_Friends_List;
	private static final WidgetMenuOption FIXED_Ignore_List ;
	private static final WidgetMenuOption Resizable_Ignore_List;
	private static final WidgetMenuOption Bottom_Ignore_List;
	@Inject
	private Client client;
	@Inject
	private MenuManager menuManager;
	@Inject
	private FriendsExporterConfig config;

	@Override
	protected void startUp() throws Exception
	{
		refreshShiftClickCustomizationMenus();
	}

	@Override
	protected void shutDown() throws Exception
	{
		removeShiftClickCustomizationMenus();
	}

	@Subscribe
	public void onWidgetMenuOptionClicked(WidgetMenuOptionClicked event) throws Exception {
		if (event.getWidget() == WidgetInfo.FIXED_VIEWPORT_FRIENDS_TAB||event.getWidget() == WidgetInfo.RESIZABLE_VIEWPORT_FRIENDS_TAB){
			if(event.getMenuOption().equals("Export") && Text.removeTags(event.getMenuTarget()).equals("Friends List")){
				exportFriendsList();
			}else if(event.getMenuOption().equals("Export") && Text.removeTags(event.getMenuTarget()).equals("Ignore List")){
				exportIgnoreList();
			}
			refreshShiftClickCustomizationMenus();
		}
	}

	private void refreshShiftClickCustomizationMenus() {
		this.removeShiftClickCustomizationMenus();
		this.menuManager.addManagedCustomMenu(FIXED_Friends_List);
		this.menuManager.addManagedCustomMenu(Resizable_Friends_List);
		this.menuManager.addManagedCustomMenu(Bottom_Friends_List);
		this.menuManager.addManagedCustomMenu(FIXED_Ignore_List);
		this.menuManager.addManagedCustomMenu(Resizable_Ignore_List);
		this.menuManager.addManagedCustomMenu(Bottom_Ignore_List);
	}
	private void removeShiftClickCustomizationMenus() {
		this.menuManager.removeManagedCustomMenu(FIXED_Friends_List);
		this.menuManager.removeManagedCustomMenu(Resizable_Friends_List);
		this.menuManager.removeManagedCustomMenu(Bottom_Friends_List);
		this.menuManager.removeManagedCustomMenu(FIXED_Ignore_List);
		this.menuManager.removeManagedCustomMenu(Resizable_Ignore_List);
		this.menuManager.removeManagedCustomMenu(Bottom_Ignore_List);
	}

	private void exportFriendsList() throws Exception{
		String fileName= RuneLite.RUNELITE_DIR+"\\"+this.client.getLocalPlayer().getName()+" Friends "+ LocalDate.now()+".txt";
		purgeList(fileName);
		Friend array[]=this.client.getFriendContainer().getMembers();
		FileWriter writer = new FileWriter(fileName, true);
		for(int x = 0; x!=this.client.getFriendContainer().getMembers().length; x++) {
			String friendName = Text.toJagexName(array[x].getName());
			String prevName="";
			if(!StringUtils.isEmpty(array[x].getPrevName())){
				prevName = Text.toJagexName(array[x].getPrevName());
			}
			String Writing=toWrite(x+1,friendName,prevName);
			try {
				writer.write(Writing+"\r\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		writer.close();
	}
	private void exportIgnoreList() throws Exception{
		String fileName= RuneLite.RUNELITE_DIR+"\\"+this.client.getLocalPlayer().getName()+" Ignore "+ LocalDate.now()+".txt";
		purgeList(fileName);
		Ignore array[]=this.client.getIgnoreContainer().getMembers();
		FileWriter writer = new FileWriter(fileName, true);
		for(int x = 0; x!=this.client.getIgnoreContainer().getMembers().length; x++) {
			String friendName = Text.toJagexName(array[x].getName());
			String prevName="";
			if(!StringUtils.isEmpty(array[x].getPrevName())){
				prevName = Text.toJagexName(array[x].getPrevName());
			}
			String Writing=toWrite(x+1,friendName,prevName);
			try{
				writer.write(Writing+"\r\n");
			}catch(IOException e){
				e.printStackTrace();
			}
		}
		writer.close();
	}

	private void purgeList(String fileName){
		File purge=new File(fileName);
		purge.delete();
	}

	private String toWrite(Integer Num, String firstName,String lastName){
		String export="";
		String Separator="";
		if(this.config.Separator()==""){
			Separator=" ";
		}else{
			Separator=this.config.Separator();
		}
		switch (this.config.Lineleads()) {
			case None:
				if(!StringUtils.isEmpty(lastName))
					export=firstName+Separator+lastName;
				else
					export=firstName;
				break;
			case Number:
				if(!StringUtils.isEmpty(lastName))
					export=Num.toString()+" "+firstName+Separator+lastName;
				else
					export=Num.toString()+" "+firstName;
				break;
			case Number1:
				if(!StringUtils.isEmpty(lastName))
					export=Num.toString()+". "+firstName+Separator+lastName;
				else
					export=Num.toString()+". "+firstName;
				break;
			case Number2:
				if(!StringUtils.isEmpty(lastName))
					export=Num.toString()+") "+firstName+Separator+lastName;
				else
					export=Num.toString()+") "+firstName;
				break;
			case Number3:
				if(!StringUtils.isEmpty(lastName))
					export=Num.toString()+".) "+firstName+Separator+lastName;
				else
					export=Num.toString()+".) "+firstName;
		}
		return(export);
	}
	static {
		FIXED_Friends_List = new WidgetMenuOption("Export","Friends List",WidgetInfo.FIXED_VIEWPORT_FRIENDS_TAB);
		Resizable_Friends_List = new WidgetMenuOption("Export","Friends List",WidgetInfo.RESIZABLE_VIEWPORT_FRIENDS_TAB);
		Bottom_Friends_List = new WidgetMenuOption("Export","Friends List",WidgetInfo.RESIZABLE_VIEWPORT_BOTTOM_LINE_FRIEND_ICON);
		FIXED_Ignore_List = new WidgetMenuOption("Export","Ignore List",WidgetInfo.FIXED_VIEWPORT_FRIENDS_TAB);
		Resizable_Ignore_List = new WidgetMenuOption("Export","Ignore List",WidgetInfo.RESIZABLE_VIEWPORT_FRIENDS_TAB);
		Bottom_Ignore_List = new WidgetMenuOption("Export","Ignore List",WidgetInfo.RESIZABLE_VIEWPORT_BOTTOM_LINE_FRIEND_ICON);
	}
	@Provides
	FriendsExporterConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(FriendsExporterConfig.class);
	}
}
