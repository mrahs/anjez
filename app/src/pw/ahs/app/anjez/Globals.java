/*******************************************************************************
 Copyright (c) 2014 - Anas H. Sulaiman (ahs.pw)

  This file is part of Anjez.
  Anjez is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 ******************************************************************************/

package pw.ahs.app.anjez;

import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import pw.ahs.app.anjez.controller.*;
import pw.ahs.app.anjez.gui.App;
import pw.ahs.app.anjez.model.TDTask;

import java.text.Collator;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class Globals {

    public static App view = null;
    public static final String APP_TITLE = "Anjez";
    public static final String APP_VERSION = "1.0.0";
    public static final String APP_HOME = "http://ahs.pw/app/anjez/index.php";
    public static final String DEV_HOME = "http://ahs.pw/";
    public static final String PREFS_FILE_NAME = APP_TITLE + ".conf";
    public static final String DEFAULT_FILE_NAME = APP_TITLE + ".txt";
    public static final Pattern PATTERN_HAS_PRIORITY = Pattern.compile("(. \\d{4}-\\d{2}-\\d{2} )?(\\(.\\)).*");
    public static final Map<String, String> PRIORITY_CHARS_I18N = new HashMap<>(26);
    public static final String LINE_SEP = System.getProperty("line.separator");
    public static final Collator COLLATOR_EN = Collator.getInstance(new Locale("en", "US"));
    public static final Collator COLLATOR_AR = Collator.getInstance(new Locale("ar", "SA"));
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("uuuu-MM-dd kk:mm:ss", Locale.US);
    public static final FileChooser.ExtensionFilter TEXT_EXT_FILTER = new FileChooser.ExtensionFilter("Text Files (.txt)", "*.txt");

    public static ResourceBundle i18n = ResourceBundle.getBundle("pw.ahs.app.anjez.i18n.word", Locale.US);
    public static Collator theCollator = COLLATOR_EN;
    public static Locale theLocale = new Locale("en", "US");

    public static final StringConverter<String> THE_LANG_NAME_CODE_CONVERTER = new StringConverter<String>() {
        @Override
        public String toString(String s) {
            switch (s) {
                case "en":
                    return "English";
                case "ar":
                    return "العربية";
                case "English":
                    return "en";
                case "العربية":
                    return "ar";
                default:
                    return "";
            }
        }

        @Override
        public String fromString(String s) {
            return toString(s);
        }
    };
    public static final Comparator<LocalDate> THE_DATE_COMPARATOR = (o1, o2) -> {
        if (o1 == null && o2 == null) return 0;
        if (o1 == null) return 1;
        if (o2 == null) return -1;

        return o1.compareTo(o2);
    };
    public static final Comparator<String> THE_TEXT_COMPARATOR = (o1, o2) -> {
        if (o1 == null && o2 == null) return 0;
        if (o1 == null) return 1;
        if (o2 == null) return -1;

        if (o1.isEmpty() && o2.isEmpty()) return 0;
        if (o1.isEmpty()) return 1;
        if (o2.isEmpty()) return -1;

        return theCollator.compare(o1, o2);
    };

    /*
    ************************************************************
    *
    * Filters Fields
    *
    ************************************************************/

    public static final String FILTER_PREFIX = ":";
    public static final String SHORTCUT_PREFIX = "&";
    public static final Predicate<TDTask> FILTER_NOT_DONE = p -> !p.isDone();
    public static final Predicate<TDTask> FILTER_ALL = p -> true;
    public static final Predicate<TDTask> FILTER_TODAY = p -> {
        LocalDate taskDue = p.getDueDate();
        if (taskDue == null) return false;
        LocalDate today = LocalDate.now();
        return taskDue.isEqual(today);
    };
    public static final Predicate<TDTask> FILTER_TOMORROW = p -> {
        LocalDate taskDue = p.getDueDate();
        if (taskDue == null) return false;
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        return taskDue.isEqual(tomorrow);
    };
    public static final Predicate<TDTask> FILTER_WEEK = p -> {
        LocalDate taskDue = p.getDueDate();
        if (taskDue == null) return false;
        LocalDate today = LocalDate.now();
        return taskDue.isEqual(today) || (taskDue.isAfter(today) && taskDue.isBefore(today.plusWeeks(1)));
    };
    public static final Predicate<TDTask> FILTER_SOME_DAY = p -> p.getDueDate() == null;
    public static final Predicate<TDTask> FILTER_OVER_DUE = p -> !p.isDone() && p.getDueDate() != null && p.getDueDate().isBefore(LocalDate.now());

    public static boolean filterKeywords(String info, String... keywords) {
        boolean containsAll = true;
        for (String kw : keywords) {
            if (kw.isEmpty()) continue;
            if (!info.toUpperCase(theLocale).contains(kw.toUpperCase(theLocale))) {
                containsAll = false;
                break;
            }
        }
        return containsAll;
    }

    /*
    ************************************************************
    *
    * Controllers
    *
    ************************************************************/

    public static final Controller controller = Controller.getInstance();
    public static final BeforeClosingController beforeClosingController = BeforeClosingController.getInstance();
    public static final AutoSaveController autoSaveController = AutoSaveController.getInstance(); // requires BeforeClosingController
    public static final CurrentLangController currentLangController = CurrentLangController.getInstance();
    public static final DropBoxController dropBoxController = DropBoxController.getInstance(APP_TITLE, APP_VERSION);
    public static final InternetController internetController = InternetController.getInstance();
    public static final PrefsController prefsController = PrefsController.getInstance();
    public static final TrackingController trackingController = TrackingController.getInstance();
    public static final UndoRedoController undoRedoController = UndoRedoController.getInstance();
    public static final UnsavedController unsavedController = UnsavedController.getInstance();

    // user agent
    public static String getUserAgentString() {
        return APP_TITLE
                + "/"
                + APP_VERSION
                + "; "
                + "Java/"
                + System.getProperty("java.version")
                + " ("
                + System.getProperty("os.name")
                + " "
                + System.getProperty("os.version")
                + "; "
                + System.getProperty("os.arch")
                + ")";
    }

    // Initializer
    static {
        COLLATOR_EN.setStrength(Collator.TERTIARY);
        COLLATOR_EN.setDecomposition(Collator.CANONICAL_DECOMPOSITION);
        COLLATOR_AR.setStrength(Collator.TERTIARY);
        COLLATOR_AR.setStrength(Collator.CANONICAL_DECOMPOSITION);
        System.setProperty("http.agent", "");
    }
}
