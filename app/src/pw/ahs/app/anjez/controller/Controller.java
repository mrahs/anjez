/*******************************************************************************
 Copyright (c) 2014 - Anas H. Sulaiman (ahs.pw)

  This file is part of Anjez.
  Anjez is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 ******************************************************************************/

package pw.ahs.app.anjez.controller;

import javafx.scene.input.Clipboard;
import javafx.scene.input.DataFormat;
import pw.ahs.app.anjez.gui.App;
import pw.ahs.app.anjez.model.TDTask;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

import static pw.ahs.app.anjez.Globals.*;
import static pw.ahs.app.anjez.controller.PrefsController.pLang;

public class Controller {

    private static Controller instance = null;

    public static Controller getInstance() {
        if (instance == null)
            instance = new Controller();
        return instance;
    }

    private Controller() {
    }

    public TDTask parseTask(String txt) {
        // check feedback
        if (txt.startsWith(":)") || txt.startsWith(":(") || txt.startsWith(":|")) return null;

        // pre-process text for pw.ahs.app.anjez.i18n
        // 1. due:
        txt = txt.replace(i18n.getString("todo-txt.due") + ":", "due:");

        // 2. &today and &tomorrow
        txt = txt.replace(SHORTCUT_PREFIX + i18n.getString("filter.today"), LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        txt = txt.replace(SHORTCUT_PREFIX + i18n.getString("filter.next"), LocalDate.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE));

        // 3. x
        txt = txt.replaceFirst("^" + i18n.getString("todo-txt.x") + " ", "x ");

        // 4. priority
        Matcher matcher = PATTERN_HAS_PRIORITY.matcher(txt);
        if (matcher.matches()) {
            String p = matcher.group(2).substring(1, 2);
            txt = txt.replaceFirst(p, PRIORITY_CHARS_I18N.get(p));
        }

        return TDTask.parse(txt);
    }

    public boolean showHelpFile(App view) {
        try {
            String helpFileName = "/pw/ahs/app/anjez/res/help_" + prefsController.getPref(pLang) + ".html";
            InputStream is = getClass().getResourceAsStream(helpFileName);
            if (is == null) {
                return false;
            }
            Path tmpFile = Files.createTempFile(Paths.get(System.getProperty("java.io.tmpdir")), APP_TITLE + "_Help", ".html");
            OutputStream osHelp = Files.newOutputStream(tmpFile);
            byte[] buffer = new byte[600000];
            int len;
            while ((len = is.read(buffer)) != -1) {
                osHelp.write(buffer, 0, len);
            }

            view.getHostServices().showDocument(tmpFile.toString());

            is.close();
            osHelp.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public void copy(Collection<TDTask> tdTasks) {
        StringBuilder sb = new StringBuilder();
        for (TDTask tdTask : tdTasks) {
            sb.append(tdTask).append("\n");
        }
        Map<DataFormat, Object> content = new HashMap<>(1);
        content.put(DataFormat.PLAIN_TEXT, sb.toString());
        Clipboard.getSystemClipboard().setContent(content);
    }
}
