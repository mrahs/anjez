/*******************************************************************************
 Copyright (c) 2014 - Anas H. Sulaiman (ahs.pw)

  This file is part of Anjez.
  Anjez is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 ******************************************************************************/

package pw.ahs.app.anjez.gui;

import pw.ahs.app.anjez.gui.helper.*;

public class UIHelper {
    public static final AboutHelper about = AboutHelper.getInstance();
    public static final DropBoxHelper dropBox = DropBoxHelper.getInstance();
    public static final GuiTourHelper tour = GuiTourHelper.getInstance();
    public static final IOHelper io = IOHelper.getInstance();
    public static final MenuHelper menu = MenuHelper.getInstance();
    public static final PrefsHelper prefs = PrefsHelper.getInstance();
    public static final ShortcutSheetHelper shortcutSheet = ShortcutSheetHelper.getInstance();
    public static final StatusBarHelper statusBar = StatusBarHelper.getInstance();
    public static final TableHelper table = TableHelper.getInstance();
    public static final TrackingDetailsHelper tracking = TrackingDetailsHelper.getInstance();
}
