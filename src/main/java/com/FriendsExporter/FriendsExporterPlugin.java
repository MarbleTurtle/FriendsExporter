package com.FriendsExporter;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.Friend;
import net.runelite.api.FriendsChatMember;
import net.runelite.api.Ignore;
import net.runelite.api.Player;
import net.runelite.api.clan.ClanChannelMember;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.menus.MenuManager;
import net.runelite.client.menus.WidgetMenuOption;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.Text;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static net.runelite.client.RuneLite.RUNELITE_DIR;

@Slf4j
@PluginDescriptor(
        name = "Friends Exporter"
)
public class FriendsExporterPlugin extends Plugin {

    private static final String EXPORT = "Export";
    public static final File EXPORT_DIR = new File(RUNELITE_DIR, "player-exports");

    private static final WidgetMenuOption FIXED_FRIENDS_LIST
            = new WidgetMenuOption(EXPORT, "Friends List", WidgetInfo.FIXED_VIEWPORT_FRIENDS_TAB);

    private static final WidgetMenuOption RESIZABLE_FRIENDS_LIST
            = new WidgetMenuOption(EXPORT, "Friends List", WidgetInfo.RESIZABLE_VIEWPORT_FRIENDS_TAB);

    private static final WidgetMenuOption MODERN_FRIENDS_LIST
            = new WidgetMenuOption(EXPORT, "Friends List", 10747943);

    private static final WidgetMenuOption FIXED_IGNORE_LIST
            = new WidgetMenuOption(EXPORT, "Ignore List", WidgetInfo.FIXED_VIEWPORT_FRIENDS_TAB);

    private static final WidgetMenuOption RESIZABLE_IGNORE_LIST
            = new WidgetMenuOption(EXPORT, "Ignore List", WidgetInfo.RESIZABLE_VIEWPORT_FRIENDS_TAB);

    private static final WidgetMenuOption MODERN_IGNORE_LIST
            = new WidgetMenuOption(EXPORT, "Ignore List", 10747943);

    private static final WidgetMenuOption CHAT_CHANNEL_LIST
            = new WidgetMenuOption(EXPORT, "Current Members", 46333955);

    private static final WidgetMenuOption CHAT_CHANNEL_RANKS
            = new WidgetMenuOption(EXPORT, "Rank List", 46333955);

    private static final WidgetMenuOption CLAN_CHAT_MEMBERS
            = new WidgetMenuOption(EXPORT, "Online Clan Members", 46333956);

    private static final WidgetMenuOption CLAN_CHAT_JOINS
            = new WidgetMenuOption(EXPORT, "All Clan Members", 46333956);

    private static final WidgetMenuOption CLAN_CHAT_BANS
            = new WidgetMenuOption(EXPORT, "Clan Bans", 46333956);

    private static final WidgetMenuOption CLAN_CHAT_EVENTS
            = new WidgetMenuOption(EXPORT, "Clan Events", 46333956);

    private static final WidgetMenuOption GUEST_CLAN_CHAT_TITLES
            = new WidgetMenuOption(EXPORT, "Guest Clan Members", 46333957);

    private static final WidgetMenuOption FIXED_EMOTE
            = new WidgetMenuOption(EXPORT, "Local Players", WidgetInfo.FIXED_VIEWPORT_EMOTES_TAB);

    private static final WidgetMenuOption RESIZABLE_EMOTE
            = new WidgetMenuOption(EXPORT, "Local Players", WidgetInfo.RESIZABLE_VIEWPORT_EMOTES_TAB);

    private static final WidgetMenuOption MODERN_EMOTE
            = new WidgetMenuOption(EXPORT, "Local Players", 10747945);

    private static final DateFormat TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
    private boolean chatChannel = false;
    private boolean wid = false;
    @Inject
    private Client client;
    @Inject
    private MenuManager menuManager;
    @Inject
    private FriendsExporterConfig config;
    @Inject
    private ConfigManager configManager;

    @Override
    protected void startUp() throws Exception {
        EXPORT_DIR.mkdirs();
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
        refreshShiftClickCustomizationMenus();
    }

    private void refreshShiftClickCustomizationMenus() {
        this.removeShiftClickCustomizationMenus();
        this.menuManager.addManagedCustomMenu(FIXED_FRIENDS_LIST, e -> exportFriendsList());
        this.menuManager.addManagedCustomMenu(RESIZABLE_FRIENDS_LIST, e -> exportFriendsList());
        this.menuManager.addManagedCustomMenu(MODERN_FRIENDS_LIST, e -> exportFriendsList());

        this.menuManager.addManagedCustomMenu(FIXED_IGNORE_LIST, e -> exportIgnoreList());
        this.menuManager.addManagedCustomMenu(RESIZABLE_IGNORE_LIST, e -> exportIgnoreList());
        this.menuManager.addManagedCustomMenu(MODERN_IGNORE_LIST, e -> exportIgnoreList());

        this.menuManager.addManagedCustomMenu(CHAT_CHANNEL_LIST, e -> exportChatChannelMembers());
        this.menuManager.addManagedCustomMenu(CHAT_CHANNEL_RANKS, e -> exportChatChannelRankList());

        this.menuManager.addManagedCustomMenu(FIXED_EMOTE, e -> exportLocalPlayers());
        this.menuManager.addManagedCustomMenu(RESIZABLE_EMOTE, e -> exportLocalPlayers());
        this.menuManager.addManagedCustomMenu(MODERN_EMOTE, e -> exportLocalPlayers());

        this.menuManager.addManagedCustomMenu(CLAN_CHAT_MEMBERS, e -> exportOnlineClanMembers());
        this.menuManager.addManagedCustomMenu(CLAN_CHAT_JOINS, e -> exportAllClanMembers());
        this.menuManager.addManagedCustomMenu(CLAN_CHAT_BANS, e -> exportClanBanList());
        this.menuManager.addManagedCustomMenu(CLAN_CHAT_EVENTS, e -> exportClanEventList());
    }

    private void removeShiftClickCustomizationMenus() {
        this.menuManager.removeManagedCustomMenu(FIXED_FRIENDS_LIST);
        this.menuManager.removeManagedCustomMenu(RESIZABLE_FRIENDS_LIST);
        this.menuManager.removeManagedCustomMenu(MODERN_FRIENDS_LIST);

        this.menuManager.removeManagedCustomMenu(FIXED_IGNORE_LIST);
        this.menuManager.removeManagedCustomMenu(RESIZABLE_IGNORE_LIST);
        this.menuManager.removeManagedCustomMenu(MODERN_IGNORE_LIST);

        this.menuManager.removeManagedCustomMenu(CHAT_CHANNEL_LIST);
        this.menuManager.removeManagedCustomMenu(CHAT_CHANNEL_RANKS);

        this.menuManager.removeManagedCustomMenu(FIXED_EMOTE);
        this.menuManager.removeManagedCustomMenu(RESIZABLE_EMOTE);
        this.menuManager.removeManagedCustomMenu(MODERN_EMOTE);

        this.menuManager.removeManagedCustomMenu(CLAN_CHAT_MEMBERS);
        this.menuManager.removeManagedCustomMenu(CLAN_CHAT_JOINS);
        this.menuManager.removeManagedCustomMenu(CLAN_CHAT_BANS);
        this.menuManager.removeManagedCustomMenu(CLAN_CHAT_EVENTS);
    }

    /**
     * Exports the player's Friends List.
     */
    private void exportFriendsList() {
        String filename = this.client.getLocalPlayer().getName() + " Friends " + format(new Date()) + ".txt";
        Friend array[] = this.client.getFriendContainer().getMembers();
        List<PlayerListItem> playerList = new ArrayList<>();
        for (int i = 0; i != this.client.getFriendContainer().getMembers().length; ++i) {
            PlayerListItem playerListItem = new PlayerListItem();
            playerListItem.setName(array[i].getName());
            if (!StringUtils.isEmpty(array[i].getPrevName()) && this.config.includePrevName()) {
                playerListItem.setPreviousName(array[i].getPrevName());
            }
            if (this.config.includeNote()) {
                playerListItem.setNote(configManager.getConfiguration("friendNotes", "note_" + playerListItem.getName()));
            }
            playerList.add(playerListItem);
        }
        exportList(filename, playerList);
    }


    /**
     * Exports the player's Ignore List.
     */
    private void exportIgnoreList() {
        String filename = this.client.getLocalPlayer().getName() + " Ignore " + format(new Date()) + ".txt";
        Ignore array[] = this.client.getIgnoreContainer().getMembers();
        List<PlayerListItem> playerList = new ArrayList<>();
        for (int i = 0; i != array.length; ++i) {
            PlayerListItem playerListItem = new PlayerListItem();
            playerListItem.setName(array[i].getName());
            if (!StringUtils.isEmpty(array[i].getPrevName()) && this.config.includePrevName()) {
                playerListItem.setPreviousName(array[i].getPrevName());
            }
            if (this.config.includeNote()) {
                playerListItem.setNote(configManager.getConfiguration("friendNotes", "note_" + playerListItem.getName()));
            }
            playerList.add(playerListItem);
        }
        exportList(filename, playerList);
    }


    /**
     * Exports all members of the player's own chat-channel. Requires the Chat-channel Setup widget to be open.
     */
    private void exportChatChannelRankList() {
        String filename = this.client.getLocalPlayer().getName() + " Ranks " + format(new Date()) + ".txt";
        Friend memberArray[] = this.client.getFriendContainer().getMembers();
        Widget temp;
        Widget[] temp2;
        temp = this.client.getWidget(94, 28);
        temp2 = temp.getChildren();

        if (!chatChannel) {
            this.client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Please open Chat-channel Setup found in the Chat-channel tab to export this list.", "");
            return;
        }

        List<PlayerListItem> playerList = new ArrayList<>();

        for (int i = 0; i < temp2.length/4; ++i) {
            String rank = temp2[(i * 4) + 1].getText();
            if(!rank.equals("Not ranked") || this.config.showUnranked()) {
                String prevName = "";
                for (int j = 0; j < memberArray.length; ++j) {
                    String friendName = memberArray[j].getName();
                    if (friendName.equals(temp2[(i * 4) + 2].getText())) {
                        if (!StringUtils.isEmpty(memberArray[j].getPrevName()) && this.config.includePrevName()) {
                            prevName = memberArray[j].getPrevName();
                        }
                        break;
                    }
                }

                PlayerListItem playerListItem = new PlayerListItem();
                playerListItem.setName(temp2[(i * 4) + 2].getText());
                playerListItem.setPreviousName(prevName);
                playerListItem.setRank(!rank.equals("Not ranked") ? rank : "No Rank");

                playerList.add(playerListItem);
            }
        }
        exportList(filename, playerList);
    }

    /**
     * Exports players in the current Chat Channel. Player must be in a Chat Channel to have this available to them.
     * Drops a message in the Game chat if the player is not currently in a Chat Channel.
     *
     * This was previously known as Friends Chat.
     */
    private void exportChatChannelMembers() {
        String filename = this.client.getLocalPlayer().getName() + " Members " + format(new Date()) + ".txt";

        if (this.client.getFriendsChatManager() == null) {
            this.client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Please join a Chat-channel to export this list.", "");
            return;
        }

        FriendsChatMember array[] = this.client.getFriendsChatManager().getMembers();

        List<PlayerListItem> playerList = new ArrayList<>();
        for (int i = 0; i != this.client.getFriendsChatManager().getMembers().length; ++i) {
            String friendName = array[i].getName();
            PlayerListItem playerListItem = new PlayerListItem();
            playerListItem.setName(friendName);
            playerList.add(playerListItem);
        }
        exportList(filename, playerList);
    }

    /**
     * Exports loaded players around the user in-game.
     */
    private void exportLocalPlayers() {
        String filename = this.client.getLocalPlayer().getName() + " Local " + format(new Date()) + ".txt";
        List<Player> array = this.client.getPlayers();
        List<PlayerListItem> playerList = new ArrayList<>();
        for (int i = 0; i != array.size(); ++i) {
            String localName = array.get(i).getName();
            if(!localName.matches(client.getLocalPlayer().getName())) {
                PlayerListItem playerListItem = new PlayerListItem();
                playerListItem.setName(localName);
                playerList.add(playerListItem);
            }
        }
        exportList(filename, playerList);
    }

    /**
     * Exports currently online clan members.
     */
    private void exportOnlineClanMembers() {
        String filename = this.client.getClanChannel().getName() + " Members " + format(new Date()) + ".txt";
        List<PlayerListItem> playerList = client.getClanChannel().getMembers().stream()
                .sorted(Comparator.comparing(ClanChannelMember::getRank).reversed()
                        .thenComparing(ClanChannelMember::getName, String::compareToIgnoreCase))
                .map((ClanChannelMember clanmate) -> {
                    PlayerListItem playerListItem = new PlayerListItem();
                    playerListItem.setName(clanmate.getName());
                    playerListItem.setRank(client.getClanSettings().titleForRank(clanmate.getRank()).getName());
                    return playerListItem;
                })
                .collect(Collectors.toList());
        exportList(filename, playerList);
    }

    /**
     * Exports the full list of current clan members, not just online. Requires the clan "Members" menu to be open.
     * Drops a message into game chat if required Widget is not open.
     */
    private void exportAllClanMembers() {
        String filename = this.client.getClanChannel().getName() + " Full Member List " + format(new Date()) + ".txt";
        Widget clanListWidget = client.getWidget(693,10);
        if (clanListWidget == null) {
            this.client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Please open clan settings and navigate to the Members tab.", "");
            return;
        }

        Widget[] clanList = clanListWidget.getChildren();

        List<PlayerListItem> playerList = new ArrayList<>();
        for (int i = 1; i < clanList.length; i+=3) {
            PlayerListItem playerListItem = new PlayerListItem();
            playerListItem.setName(clanList[i].getText());
            playerList.add(playerListItem);
        }
        exportList(filename, playerList);
    }

    /**
     * Exports the ban list for your clan. Drops a message in game chat if the Clan Banlist isn't open.
     */
    private void exportClanBanList() {
        String filename = this.client.getClanChannel().getName() + " Ban List " + format(new Date()) + ".txt";

        Widget clanBanListWidget = client.getWidget(689,6);
        if (clanBanListWidget == null) {
            this.client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Please open clan settings and navigate to the Bans tab.", "");
            return;
        }

        int banSize = clanBanListWidget.getDynamicChildren().length/2;
        List<PlayerListItem> playerList = new ArrayList<>();
        for (int i = 0; i != banSize; ++i) {
            String player = client.getWidget(689, 6).getDynamicChildren()[500+i].getText();
            if (player.isEmpty()) {
                break;
            }
            PlayerListItem playerListItem = new PlayerListItem();
            playerListItem.setName(player);
            playerList.add(playerListItem);
        }
        exportList(filename, playerList);
    }

    /**
     * Exports the clan event list. Drops a message in game chat if the clan events chat isn't open.
     * This is the only function that does not currently use exportList and instead does everything in it.
     */
    private void exportClanEventList() {
        String filePath = EXPORT_DIR + "\\" + this.client.getClanChannel().getName() + " Events " + format(new Date()) + ".txt";
        try {
            purgeList(filePath);
            Widget clanEventWidget = client.getWidget(703,11);
            if (clanEventWidget == null) {
                this.client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Please open clan settings and navigate to the Events tab.", "");
                return;
            }

            int eventSize = clanEventWidget.getDynamicChildren().length;
            FileWriter writer = new FileWriter(filePath, true);
            for (int i = 0; i != eventSize; ++i) {
                String world = Text.removeTags(client.getWidget(703, 11).getDynamicChildren()[i].getText());
                String startDate = Text.removeTags(client.getWidget(703, 12).getDynamicChildren()[i].getText());
                String startTime = Text.removeTags(client.getWidget(703, 13).getDynamicChildren()[i].getText());
                String duration = Text.removeTags(client.getWidget(703, 14).getDynamicChildren()[i].getText());
                String type = Text.removeTags(client.getWidget(703, 15).getDynamicChildren()[i].getText());
                String focus = Text.removeTags(client.getWidget(703, 16).getDynamicChildren()[i].getText());
                String subType = Text.removeTags(client.getWidget(703, 17).getDynamicChildren()[i].getText());
                String ranks = Text.removeTags(client.getWidget(703, 19).getDynamicChildren()[i].getText());

                StringBuilder exportString = new StringBuilder();
                if (!this.config.lineLeads().equals(LineLeads.None)) {
                    exportString.append(i + this.config.lineLeads().getPunctuation());
                }
                exportString.append(
                        focus + config.getSeparator() + type
                              + config.getSeparator() + subType
                              + config.getSeparator() + startDate + " " + startTime
                              + config.getSeparator() + duration
                              + config.getSeparator() + world
                              + config.getSeparator() + ranks
                );

                writer.write(exportString + "\r\n");
            }
            writer.close();
        } catch (IOException e) {
            System.err.println("Failed to create file: " + filePath + ". Stack trace: " + e);
        }
    }

    /**
     * Handles the exporting of a file for a list of {@link PlayerListItem}.
     * @param filename The base filename
     * @param playerList List of PlayerListItems containing relevant information for exporting
     */
    private void exportList(String filename, List<PlayerListItem> playerList) {
        String filePath = EXPORT_DIR + "\\" + filename;
        purgeList(filePath);
        try {
            FileWriter writer = new FileWriter(filePath, true);
            for (int i = 0; i < playerList.size(); ++i) {
                writer.write(getExportLineForPlayer(i + 1, playerList.get(i)) + "\r\n");
            }
            writer.close();
        } catch (IOException e) {
            System.err.println("Failed to create file: " + filePath + ". Stack trace: " + e);
        }
    }

    private void purgeList(String filename) {
        File purge = new File(filename);
        purge.delete();
    }

    /**
     * Puts together the text for one {@link PlayerListItem}.
     *
     * @param num the index to place as the front
     * @param playerListItem the player wrapper with info
     * @return the complete string to be exported for the player.
     */
    private String getExportLineForPlayer(int num, PlayerListItem playerListItem) {

        String separator = this.config.getSeparator();

        StringBuilder exportString = new StringBuilder();
        if (!this.config.lineLeads().equals(LineLeads.None)) {
            exportString.append(num + this.config.lineLeads().getPunctuation());
        }

        if (!playerListItem.getRank().isEmpty()) {
            exportString.append(playerListItem.getRank() + separator);
        }

        exportString.append(playerListItem.getName());

        if (!StringUtils.isEmpty(playerListItem.getPreviousName())) {
            exportString.append(separator + playerListItem.getPreviousName());
        }

        if (!StringUtils.isEmpty(playerListItem.getNote())) {
            exportString.append(separator + playerListItem.getNote());
        }

        return exportString.toString();
    }

    @Provides
    FriendsExporterConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(FriendsExporterConfig.class);
    }

    @Subscribe
    public void onWidgetLoaded(WidgetLoaded widget) {
        if (widget.getGroupId() == 94) {
            wid = true;
            chatChannel = true;
        }
        Widget temp = this.client.getWidget(widget.getGroupId(),0);
    }
    @Subscribe
    public void onGameTick(GameTick event) {
        if (this.client.getWidget(94,28) == null) {
            chatChannel = false;
            refreshShiftClickCustomizationMenus();
        }

        if (wid) {
            refreshShiftClickCustomizationMenus();
            wid = false;
        }
    }
}