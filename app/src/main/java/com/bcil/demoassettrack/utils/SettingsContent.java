package com.bcil.demoassettrack.utils;

import com.bcil.demoassettrack.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SettingsContent {
    public static List<SettingItem> ITEMS = new ArrayList<>();

    /**
     * A map of sample (Settings) items, by ID.
     */
    public static Map<String, SettingItem> ITEM_MAP = new HashMap<>();

    static {
        // Add items.
        addItem(new SettingItem("1", "Readers List"/*,"Available Readers"*/, R.drawable.title_rdl));
//        addItem(new SettingItem("2", "Application"/*,"Settings"*/, R.drawable.title_sett));
        addItem(new SettingItem("2", "Antenna",/*"Set Antenna parameters",*/R.drawable.title_antn));
//        addItem(new SettingItem("4", "Start\\Stop Triggers",/*"Region and channels",*/R.drawable.title_strstp));
//        addItem(new SettingItem("5", "Power Management",/*"Version information",*/R.drawable.title_dpo_disabled));
        addItem(new SettingItem("3", "Battery",/*"Configurations",*/R.drawable.title_batt));
//        addItem(new SettingItem("8", "Battery",/*"Configurations",*/R.drawable.title_batt));
//        addItem(new SettingItem("9", "Power Management",/*"Version information",*/R.drawable.title_dpo_disabled));
//        addItem(new SettingItem("10", "Beeper",/*"Status",*/R.drawable.title_beep));
//        addItem(new SettingItem("11", "Save Configuration",/*"Tag Settings",*/R.drawable.title_save));
    }

    private static void addItem(SettingItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    /**
     * A Settings item representing a piece of content.
     */
    public static class SettingItem {
        public String id;
        public String content;
        //public String subcontent;
        public int icon;

        public SettingItem(String id, String content/*,String subcontent*/, int icon_id) {
            this.id = id;
            this.content = content;
            //this.subcontent = subcontent;
            this.icon = icon_id;
        }

        @Override
        public String toString() {
            return content;
        }
    }
}
