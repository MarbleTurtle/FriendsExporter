package com.FriendsExporter;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.clan.ClanChannel;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.Widget;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@PluginDescriptor(
	name = "Friends Exporter"
)
public class FriendsExporterPlugin extends Plugin {
	private static final WidgetMenuOption FIXED_Friends_List;
	private static final WidgetMenuOption Resizable_Friends_List;
	//private static final WidgetMenuOption Modern_Friends_List;
	private static final WidgetMenuOption FIXED_Ignore_List;
	private static final WidgetMenuOption Resizable_Ignore_List;
	//private static final WidgetMenuOption Modern_Ignore_List;
	private static final WidgetMenuOption Friend_Chat_List;
	private static final WidgetMenuOption Friend_Chat_Ranks;
	private static final WidgetMenuOption Clan_Chat_Members;
	private static final WidgetMenuOption Clan_Chat_Joins;
	private static final WidgetMenuOption Clan_Chat_Bans;
	private static final WidgetMenuOption Clan_Chat_Events;
	private static final WidgetMenuOption Guest_Clan_Chat_Titles;
	private static final WidgetMenuOption Fixed_Emote;
	private static final WidgetMenuOption Resizable_Emote;
	//private static final WidgetMenuOption Modern_Emote;
	private static final DateFormat TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
	private boolean clan=false;
	private boolean wid=false;
	private List<Player> localPlayers = new ArrayList<>();
	@Inject
	private Client client;
	@Inject
	private MenuManager menuManager;
	@Inject
	private FriendsExporterConfig config;

	@Override
	protected void startUp() throws Exception {
		refreshShiftClickCustomizationMenus();
	}

	@Override
	protected void shutDown() throws Exception {
		removeShiftClickCustomizationMenus();
	}

	static String format(Date date) {
		synchronized(TIME_FORMAT) {
			return TIME_FORMAT.format(date);
		}
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event) throws Exception {
		if (event.getMenuOption().equals("Export") && Text.removeTags(event.getMenuTarget()).equals("Friends List")) {
			exportFriendsList();
		} else if (event.getMenuOption().equals("Export") && Text.removeTags(event.getMenuTarget()).equals("Ignore List")) {
			exportIgnoreList();
		} else if (event.getMenuOption().equals("Export") && Text.removeTags(event.getMenuTarget()).equals("Rank List")) {
			if(event.getWidgetId()==Friend_Chat_List.getWidgetId()) {
				if (clan) {
					exportRankList();
				} else {
					this.client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Please open Clan Setup found in Friends Chat tab to export this list.", "");
				}
			}
		} else if (event.getMenuOption().equals("Export") && Text.removeTags(event.getMenuTarget()).equals("Current Members")) {
			if(event.getWidgetId()==Friend_Chat_List.getWidgetId()) {
				if (this.client.getFriendsChatManager() != null) {
					exportFriendChatMemberList();
				} else {
					this.client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Please join a Friends Chat to export this list.", "");
				}
			}
		} else if (event.getMenuOption().equals("Export") && Text.removeTags(event.getMenuTarget()).equals("Local Players")) {
			exportLocalPlayers();
		}
		refreshShiftClickCustomizationMenus();
	}

	private void refreshShiftClickCustomizationMenus() {
		//note to more motivated me=look into try-with-resources to fix this
		this.removeShiftClickCustomizationMenus();
		this.menuManager.addManagedCustomMenu(FIXED_Friends_List, null);
		this.menuManager.addManagedCustomMenu(Resizable_Friends_List, null);
		//this.menuManager.addManagedCustomMenu(Modern_Friends_List);
		this.menuManager.addManagedCustomMenu(FIXED_Ignore_List, null);
		this.menuManager.addManagedCustomMenu(Resizable_Ignore_List, null);
		//this.menuManager.addManagedCustomMenu(Modern_Ignore_List);
		this.menuManager.addManagedCustomMenu(Friend_Chat_List, null);
		this.menuManager.addManagedCustomMenu(Friend_Chat_Ranks, null);
		this.menuManager.addManagedCustomMenu(Fixed_Emote, null);
		this.menuManager.addManagedCustomMenu(Resizable_Emote, null);
		this.menuManager.addManagedCustomMenu(Clan_Chat_Members, null);
		this.menuManager.addManagedCustomMenu(Clan_Chat_Joins, null);
		this.menuManager.addManagedCustomMenu(Clan_Chat_Bans, null);
		this.menuManager.addManagedCustomMenu(Clan_Chat_Events, null);
	}

	private void removeShiftClickCustomizationMenus() {
		this.menuManager.removeManagedCustomMenu(FIXED_Friends_List);
		this.menuManager.removeManagedCustomMenu(Resizable_Friends_List);
		//this.menuManager.removeManagedCustomMenu(Modern_Friends_List);
		this.menuManager.removeManagedCustomMenu(FIXED_Ignore_List);
		this.menuManager.removeManagedCustomMenu(Resizable_Ignore_List);
		//this.menuManager.removeManagedCustomMenu(Modern_Ignore_List);
		this.menuManager.removeManagedCustomMenu(Friend_Chat_List);
		this.menuManager.removeManagedCustomMenu(Friend_Chat_Ranks);
		this.menuManager.removeManagedCustomMenu(Fixed_Emote);
		this.menuManager.removeManagedCustomMenu(Resizable_Emote);
		this.menuManager.removeManagedCustomMenu(Clan_Chat_Members);
		this.menuManager.removeManagedCustomMenu(Clan_Chat_Joins);
		this.menuManager.removeManagedCustomMenu(Clan_Chat_Bans);
		this.menuManager.removeManagedCustomMenu(Clan_Chat_Events);
	}

	private void exportFriendsList() throws Exception {
		String fileName = RuneLite.RUNELITE_DIR + "\\" + this.client.getLocalPlayer().getName() + " Friends " + format(new Date()) + ".txt";
		purgeList(fileName);
		Friend array[] = this.client.getFriendContainer().getMembers();
		FileWriter writer = new FileWriter(fileName, true);
		for (int x = 0; x != this.client.getFriendContainer().getMembers().length; x++) {
			String friendName = array[x].getName();
			String prevName = "";
			if (!StringUtils.isEmpty(array[x].getPrevName())&&this.config.prevName()) {
				prevName = array[x].getPrevName();
			}
			String Writing = toWrite(x + 1, friendName, prevName,"");
			try {
				writer.write(Writing + "\r\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		writer.close();
	}

	private void exportRankList() throws Exception {
		String fileName = RuneLite.RUNELITE_DIR + "\\" + this.client.getLocalPlayer().getName() + " Ranks " + format(new Date()) + ".txt";
		purgeList(fileName);
		Friend array[] = this.client.getFriendContainer().getMembers();
		Widget temp=null;
		Widget[] temp2=null;
		temp = this.client.getWidget(94, 28);
		temp2=temp.getChildren();
		FileWriter writer = new FileWriter(fileName, true);
		for(int x=0;x<temp2.length/4;x++) {
			String rank=temp2[(x * 4) + 1].getText();
			if(!rank.equals("Not in clan")||this.config.showUnranked()) {
				String prevName = "";
				for (int y = 0; y != this.client.getFriendContainer().getMembers().length; y++) {
					String friendName = array[y].getName();
					if (friendName.equals(temp2[(x * 4) + 2].getText())) {
						if (!StringUtils.isEmpty(array[y].getPrevName())&&this.config.prevName()) {
							prevName = array[y].getPrevName();
						}
						break;
					}
				}
				String Writing="";
				if(!rank.equals("Not in clan")) {
					Writing = toWrite(x, temp2[(x * 4) + 2].getText(), prevName, rank);
				}else{
					Writing = toWrite(x, temp2[(x * 4) + 2].getText(), prevName, "No Rank");
				}
				try {
					writer.write(Writing + "\r\n");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		writer.close();
	}

	private void exportIgnoreList() throws Exception {
		String fileName = RuneLite.RUNELITE_DIR + "\\" + this.client.getLocalPlayer().getName() + " Ignore " + format(new Date()) + ".txt";
		purgeList(fileName);
		Ignore array[] = this.client.getIgnoreContainer().getMembers();
		FileWriter writer = new FileWriter(fileName, true);
		for (int x = 0; x != this.client.getIgnoreContainer().getMembers().length; x++) {
			String friendName = array[x].getName();
			String prevName = "";
			if (!StringUtils.isEmpty(array[x].getPrevName())&&this.config.prevName()) {
				prevName = array[x].getPrevName();
			}
			String Writing = toWrite(x + 1, friendName, prevName,"");
			try {
				writer.write(Writing + "\r\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		writer.close();
	}

	private void exportFriendChatMemberList() throws Exception {
		String fileName = RuneLite.RUNELITE_DIR + "\\" + this.client.getLocalPlayer().getName() + " Members " + format(new Date()) + ".txt";
		purgeList(fileName);
		FriendsChatMember array[] = this.client.getFriendsChatManager().getMembers();
		FileWriter writer = new FileWriter(fileName, true);
		for (int x = 0; x != this.client.getFriendsChatManager().getMembers().length; x++) {
			String friendName = array[x].getName();
			String Writing = toWrite(x + 1, friendName, "","");
			try {
				writer.write(Writing + "\r\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		writer.close();
	}

	private void exportLocalPlayers() throws Exception {
		String fileName = RuneLite.RUNELITE_DIR + "\\" + this.client.getLocalPlayer().getName() + " Local " + format(new Date()) + ".txt";
		purgeList(fileName);
		List<Player> array = this.client.getPlayers();
		FileWriter writer = new FileWriter(fileName, true);
		for (int x = 0; x != array.size(); x++) {
			String localName = array.get(x).getName();
			if(!localName.matches(client.getLocalPlayer().getName())) {
				String Writing = toWrite(x + 1, localName, "", "");
				try {
					writer.write(Writing + "\r\n");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		writer.close();
	}

	private void purgeList(String fileName) {
		File purge = new File(fileName);
		purge.delete();
	}

	private String toWrite(Integer Num, String firstName, String lastName, String rank) {
	    firstName=firstName.replace('\u00A0', ' ');
	    lastName=lastName.replace('\u00A0', ' ');
		String export = "";
		String Role="";
		String Separator = this.config.Separator();
		if(this.config.newLine()&&this.config.prevName()){
			Separator="\n"+Separator;
		}
		if(!rank.isEmpty()){
			Role=rank+Separator;
		}
		switch (this.config.Lineleads()) {
			case None:
				if (!StringUtils.isEmpty(lastName))
					export = Role+firstName + Separator + lastName;
				else
					export = Role+firstName;
				break;
			case Number:
				if (!StringUtils.isEmpty(lastName))
					export = Num.toString() + " " + Role+firstName + Separator + lastName;
				else
					export = Num.toString() + " " + Role+firstName;
				break;
			case Number1:
				if (!StringUtils.isEmpty(lastName))
					export = Num.toString() + ". " + Role+firstName + Separator + lastName;
				else
					export = Num.toString() + ". " + Role+firstName;
				break;
			case Number2:
				if (!StringUtils.isEmpty(lastName))
					export = Num.toString() + ") " + Role+firstName + Separator + lastName;
				else
					export = Num.toString() + ") " + Role+firstName;
				break;
			case Number3:
				if (!StringUtils.isEmpty(lastName))
					export = Num.toString() + ".) " + Role+firstName + Separator + lastName;
				else
					export = Num.toString() + ".) " + Role+firstName;
		}
		return (export);
	}

	static {
		FIXED_Friends_List = new WidgetMenuOption("Export", "Friends List", WidgetInfo.FIXED_VIEWPORT_FRIENDS_TAB);
		Resizable_Friends_List = new WidgetMenuOption("Export", "Friends List", WidgetInfo.RESIZABLE_VIEWPORT_FRIENDS_TAB);
		//Modern_Friends_List = new WidgetMenuOption("Export", "Friends List", WidgetInfo.RESIZABLE_VIEWPORT_BOTTOM_LINE_FRIEND_ICON);
		FIXED_Ignore_List = new WidgetMenuOption("Export", "Ignore List", WidgetInfo.FIXED_VIEWPORT_FRIENDS_TAB);
		Resizable_Ignore_List = new WidgetMenuOption("Export", "Ignore List", WidgetInfo.RESIZABLE_VIEWPORT_FRIENDS_TAB);
		//Modern_Ignore_List = new WidgetMenuOption("Export", "Ignore List", WidgetInfo.RESIZABLE_VIEWPORT_BOTTOM_LINE_FRIEND_ICON);
		Friend_Chat_List = new WidgetMenuOption("Export", "Current Members", 46333955);
		Friend_Chat_Ranks = new WidgetMenuOption("Export", "Rank List", 46333955);
		Fixed_Emote = new WidgetMenuOption("Export", "Local Players", WidgetInfo.FIXED_VIEWPORT_EMOTES_TAB);
		Resizable_Emote = new WidgetMenuOption("Export", "Local Players", WidgetInfo.RESIZABLE_VIEWPORT_EMOTES_TAB);
		//Modern_Emote = new WidgetMenuOption("Export", "Local Players", WidgetInfo.RESIZABLE_VIEWPORT_EMOTES_TAB);
		Clan_Chat_Members = new WidgetMenuOption("Export", "Clan Members", 46333956);
		Clan_Chat_Joins = new WidgetMenuOption("Export", "Member Join Order", 46333956);
		Clan_Chat_Bans = new WidgetMenuOption("Export", "Clan Bans", 46333956);
		Clan_Chat_Events = new WidgetMenuOption("Export", "Clan Events", 46333956);
		Guest_Clan_Chat_Titles = new WidgetMenuOption("Export", "Guest Clan Members", 46333957);
	}

	@Provides
	FriendsExporterConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(FriendsExporterConfig.class);
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded widget) {
		if(widget.getGroupId()==94){
			wid=true;
			clan=true;
		}
		Widget temp=this.client.getWidget(widget.getGroupId(),0);
	}
	@Subscribe
	public void onGameTick(GameTick event) {
		if(this.client.getWidget(94,28)==null){
			clan=false;
			refreshShiftClickCustomizationMenus();
		}

		if(wid){
			refreshShiftClickCustomizationMenus();
			wid=false;
		}
	}
}