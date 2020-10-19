/*******************************************************************************
 Copyright (c) 2014 - Anas H. Sulaiman (ahs.pw)

  This file is part of Anjez.
  Anjez is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 ******************************************************************************/

package pw.ahs.app.anjez.controller;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Properties;

import static pw.ahs.app.anjez.Globals.*;

public class PrefsController {
     /*
    ************************************************************
    *
    * Prefs Literals
    *
    ************************************************************/

    public static final String pLang = "Lang";
    public static final String pFile_Path = "File_Path";
    public static final String pRemember_File_Path = "Remember_File_Path";
    public static final String pRemember_Sort_State = "Remember_Sort_State";
    public static final String pRemember_Column_Order = "Remember_Column_Order";
    public static final String pRemember_Column_Visibility = "Remember_Column_Visibility";
    public static final String pColumn_Done_Sort_Order = "Column_Done_Sort_Order";
    public static final String pColumn_Done_Date_Sort_Order = "Column_Done_Date_Sort_Order";
    public static final String pColumn_Start_Date_Sort_Order = "Column_Start_Date_Sort_Order";
    public static final String pColumn_Due_Date_Sort_Order = "Column_Due_Date_Sort_Order";
    public static final String pColumn_Info_Sort_Order = "Column_Info_Sort_Order";
    public static final String pColumn_Duration_Sort_Order = "Column_Duration_Sort_Order";
    public static final String pColumn_Priority_Sort_Order = "Column_Priority_Sort_Order";
    public static final String pColumn_Done_Sort_Type = "Column_Done_Sort_Type";
    public static final String pColumn_Done_Date_Sort_Type = "Column_Done_Date_Sort_Type";
    public static final String pColumn_Start_Date_Sort_Type = "Column_Start_Date_Sort_Type";
    public static final String pColumn_Due_Date_Sort_Type = "Column_Due_Date_Sort_Type";
    public static final String pColumn_Info_Sort_Type = "Column_Info_Sort_Type";
    public static final String pColumn_Duration_Sort_Type = "Column_Duration_Sort_Type";
    public static final String pColumn_Priority_Sort_Type = "Column_Priority_Sort_Type";
    public static final String pColumn_Done_Order = "Column_Done_Order";
    public static final String pColumn_Done_Date_Order = "Column_Done_Date_Order";
    public static final String pColumn_Start_Date_Order = "Column_Start_Date_Order";
    public static final String pColumn_Due_Date_Order = "Column_Due_Date_Order";
    public static final String pColumn_Info_Order = "Column_Info_Order";
    public static final String pColumn_Duration_Order = "Column_Duration_Order";
    public static final String pColumn_Priority_Order = "Column_Priority_Order";
    public static final String pColumn_Done_Visibility = "Column_Done_Visibility";
    public static final String pColumn_Done_Date_Visibility = "Column_Done_Date_Visibility";
    public static final String pColumn_Start_Date_Visibility = "Column_Start_Date_Visibility";
    public static final String pColumn_Due_Date_Visibility = "Column_Due_Date_Visibility";
    public static final String pColumn_Info_Visibility = "Column_Info_Visibility";
    public static final String pColumn_Duration_Visibility = "Column_Duration_Visibility";
    public static final String pColumn_Priority_Visibility = "Column_Priority_Visibility";
    public static final String pFilter_Query = "Filter_Query";
    public static final String pShow_Done_Task = "Show_Done_Task";
    public static final String pShow_Status_Bar = "Show_Status_Bar";
    public static final String pAT = "AT";
    public static final String pAuto_Save_Interval = "Auto_Save_Interval";
    public static final String pUse_Custom_Style = "Use_Custom_Style";
    public static final String pUse_Custom_Font = "Use_Custom_Font";
    public static final String pConfirm_Delete = "Confirm_Delete";
    public static final String pCheck_For_Update_On_Startup = "Check_For_Update_On_Startup";

    private final Collection<String> keys = new LinkedHashSet<>(43);

     /*
    ************************************************************
    *
    * Fields
    *
    ************************************************************/


    private final Properties prefsDefault;
    private final Properties prefs;
    private static PrefsController instance = null;

    public static PrefsController getInstance() {
        if (instance == null)
            instance = new PrefsController();
        return instance;
    }

    private PrefsController() {
        prefsDefault = new Properties();
        prefs = new Properties(prefsDefault) {
            @Override
            public synchronized Object setProperty(String key, String value) {
                if (defaults.getProperty(key).equals(value)) {
                    remove(key);
                    return value;
                }
                return super.setProperty(key, value);
            }
        };
    }

    public void init() {
        for (Field f : PrefsController.class.getDeclaredFields()) {
            if (Modifier.isStatic(f.getModifiers()) && Modifier.isFinal(f.getModifiers()))
                try {
                    keys.add(f.get(null).toString());
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
        }

        // Language & File
        if (System.getProperty("user.language").startsWith("ar"))
            prefsDefault.setProperty(pLang, "ar");
        else
            prefsDefault.setProperty(pLang, "en");
        prefsDefault.setProperty(pFile_Path, "");

        // Remember
        prefsDefault.setProperty(pRemember_File_Path, "true");
        prefsDefault.setProperty(pRemember_Sort_State, "true");
        prefsDefault.setProperty(pRemember_Column_Order, "true");
        prefsDefault.setProperty(pRemember_Column_Visibility, "true");

        // Columns Sort State
        prefsDefault.setProperty(pColumn_Done_Sort_Order, "0");
        prefsDefault.setProperty(pColumn_Priority_Sort_Order, "1");
        prefsDefault.setProperty(pColumn_Due_Date_Sort_Order, "2");
        prefsDefault.setProperty(pColumn_Done_Date_Sort_Order, "-1");
        prefsDefault.setProperty(pColumn_Start_Date_Sort_Order, "-1");
        prefsDefault.setProperty(pColumn_Info_Sort_Order, "-1");
        prefsDefault.setProperty(pColumn_Duration_Sort_Order, "-1");

        prefsDefault.setProperty(pColumn_Done_Sort_Type, "a");
        prefsDefault.setProperty(pColumn_Priority_Sort_Type, "a");
        prefsDefault.setProperty(pColumn_Due_Date_Sort_Type, "a");
        prefsDefault.setProperty(pColumn_Done_Date_Sort_Type, "a");
        prefsDefault.setProperty(pColumn_Start_Date_Sort_Type, "a");
        prefsDefault.setProperty(pColumn_Info_Sort_Type, "a");
        prefsDefault.setProperty(pColumn_Duration_Sort_Type, "a");

        // Columns Order
        prefsDefault.setProperty(pColumn_Info_Order, "0");
        prefsDefault.setProperty(pColumn_Priority_Order, "1");
        prefsDefault.setProperty(pColumn_Start_Date_Order, "2");
        prefsDefault.setProperty(pColumn_Due_Date_Order, "3");
        prefsDefault.setProperty(pColumn_Done_Order, "4");
        prefsDefault.setProperty(pColumn_Done_Date_Order, "5");
        prefsDefault.setProperty(pColumn_Duration_Order, "6");

        // Columns Visibility
        prefsDefault.setProperty(pColumn_Done_Visibility, "true");
        prefsDefault.setProperty(pColumn_Done_Date_Visibility, "false");
        prefsDefault.setProperty(pColumn_Priority_Visibility, "true");
        prefsDefault.setProperty(pColumn_Start_Date_Visibility, "false");
        prefsDefault.setProperty(pColumn_Due_Date_Visibility, "true");
        prefsDefault.setProperty(pColumn_Info_Visibility, "true");
        prefsDefault.setProperty(pColumn_Duration_Visibility, "false");

        // Filter Query
        prefsDefault.setProperty(pFilter_Query, FILTER_PREFIX + i18n.getString("filter.all"));

        // Show Done Tasks
        prefsDefault.setProperty(pShow_Done_Task, "false");

        // Show Status Bar
        prefsDefault.setProperty(pShow_Status_Bar, "true");

        // DropBox Access Token
        prefsDefault.setProperty(pAT, "");

        // Auto Save Timer
        prefsDefault.setProperty(pAuto_Save_Interval, "0");

        // Use Custom Style
        prefsDefault.setProperty(pUse_Custom_Style, "false");

        // Use Custom Font
        prefsDefault.setProperty(pUse_Custom_Font, "false");

        // Confirm Delete
        prefsDefault.setProperty(pConfirm_Delete, "true");

        // Update
        prefsDefault.setProperty(pCheck_For_Update_On_Startup, "true");
    }

    public void setPref(String key, String val) {
        if (keys.contains(key)) prefs.setProperty(key, val);
    }

    public void updateDefault(String key, String val) {
        if (keys.contains(key)) prefsDefault.setProperty(key, val);
    }

    public String getPref(String key) {
        return prefs.getProperty(key);
    }

    public boolean getPrefBoolean(String key) {
        String val = prefs.getProperty(key);
        return val != null && Boolean.parseBoolean(val);
    }

    public int getPrefInt(String key) {
        String val = prefs.getProperty(key);
        return val == null ? Integer.MIN_VALUE : Integer.parseInt(val);
    }

    public void restoreToDefault(String key) {
        prefs.remove(key);
    }

    public void restoreToDefault() {
        prefs.clear();
    }

    public boolean loadPrefs(String filePath) {
        File f = new File(filePath);
        if (!f.exists()) return true;
        try {
            Reader r = new FileReader(f);
            prefs.load(r);
            r.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public boolean savePrefs(String filePath) {
        boolean doNotSave = prefs.isEmpty();

        if (doNotSave) {
            try {
                Files.deleteIfExists(Paths.get(filePath));
            } catch (IOException ignored) {
                doNotSave = false;
            }
        }

        if (doNotSave) return true;

        try {
            Writer w = new FileWriter(filePath);
            prefs.store(w, APP_TITLE + " v" + APP_VERSION + " Preferences");
            w.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
