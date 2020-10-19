/*******************************************************************************
 Copyright (c) 2014 - Anas H. Sulaiman (ahs.pw)

  This file is part of Anjez.
  Anjez is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 ******************************************************************************/

package pw.ahs.app.anjez.model;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TDTask {
    private static final DateTimeFormatter DATE_ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    public static final Set<String> PRIORITY_CHARS;

    private static final Pattern PATTERN_TDTask = Pattern.compile("(x \\d{4}-\\d{2}-\\d{2} )?(\\([A-Z]\\) )?(\\d{4}-\\d{2}-\\d{2} )?(.+)");
    private static final Pattern PATTERN_DUE_DATE = Pattern.compile("\\b(due:)(\\d{4}-\\d{2}-\\d{2})\\b");
    private static final Pattern PATTERN_DURATION = Pattern.compile("\\b(duration:)(.+(;.+)*)\\b");
    private static final Pattern PATTERN_PROJECT = Pattern.compile("(\\+[^ ]+)");
    private static final Pattern PATTERN_CONTEXT = Pattern.compile("(@[^ ]+)");
    private static final ObservableSet<String> _PROJECTS = FXCollections.observableSet();
    private static final ObservableSet<String> _CONTEXTS = FXCollections.observableSet();
    public static final ObservableSet<String> CONTEXTS = FXCollections.unmodifiableObservableSet(_CONTEXTS);
    public static final ObservableSet<String> PROJECTS = FXCollections.unmodifiableObservableSet(_PROJECTS);


    static {
        Set<String> tmp = new LinkedHashSet<>(26);
        tmp.add("");
        for (int i = 'A'; i <= 'Z'; i++) {
            tmp.add(String.valueOf((char) i));
        }
        PRIORITY_CHARS = Collections.unmodifiableSet(tmp);
    }

    public static void clearProjects() {
        _PROJECTS.clear();
    }

    public static void clearContexts() {
        _CONTEXTS.clear();
    }

    public static TDTask parse(String taskString) {
        if (taskString.isEmpty()) return null;

        Matcher matchTDTask = PATTERN_TDTask.matcher(taskString);
        if (!matchTDTask.matches()) return null;

        TDTask t = new TDTask();
        String doneString = matchTDTask.group(1);
        String priorityString = matchTDTask.group(2);
        String startString = matchTDTask.group(3);
        String restString = matchTDTask.group(4);

        if (doneString != null) {
            LocalDate comDate = LocalDate.parse(doneString.substring(2, doneString.length() - 1), DATE_ISO_FORMATTER);
            if (t.getStartDate() != null && comDate.isBefore(t.getStartDate())) {
                t.startDate.set(null);
            }
            t.setDone(comDate);
        }

        if (priorityString != null) {
            t.setPriority(priorityString.substring(1, 2));
        }

        if (startString != null) {
            t.setStartDate(LocalDate.parse(startString.substring(0, startString.length() - 1), DATE_ISO_FORMATTER));
        }

        Matcher matchDueDate = PATTERN_DUE_DATE.matcher(restString);
        if (matchDueDate.find()) {
            String s = matchDueDate.group(2);
            t.setDueDate(LocalDate.parse(s, DATE_ISO_FORMATTER));
            restString = restString.substring(0, matchDueDate.start()) + restString.substring(matchDueDate.end());
        }

        Matcher matchDuration = PATTERN_DURATION.matcher(restString);
        if (matchDuration.find()) {
            String s = matchDuration.group(2);
            String[] instantStrings = s.split(";");
            for (String instantString : instantStrings) {
                t.instants.add(DateTimeFormatter.ISO_INSTANT.parse(instantString, Instant::from));
            }
            restString = restString.substring(0, matchDuration.start()) + restString.substring(matchDuration.end());
        }

        t.setInfo(restString);
        if (t.getInfo().isEmpty()) {
            t.setInfo(t.toString());
            t.setDone(null);
            t.setStartDate(null);
            t.setPriority("");
            t.setDueDate(null);
            t.getInstants().clear();
        }
        return t;
    }

    private final BooleanProperty done;
    private final ObjectProperty<LocalDate> doneDate;
    private final StringProperty priority;
    private final ObjectProperty<LocalDate> startDate;
    private final ObjectProperty<LocalDate> dueDate;
    private final StringProperty info;
    private final ObservableList<Instant> instants;
    private final ObjectProperty<TDDuration> duration;

    private TDTask() {
        // A mutex is required to avoid binding loop when updating
        // done state and completion date
        final AtomicBoolean mutex = new AtomicBoolean(false);

        done = new SimpleBooleanProperty(false);

        doneDate = new ReadOnlyObjectWrapper<>();

        done.addListener((observableValue, oldVal, newVal) -> {
            if (mutex.get()) return;
            mutex.set(true);
            if (newVal) doneDate.set(LocalDate.now());
            else doneDate.set(null);
            mutex.set(false);
        });

        doneDate.addListener((observableValue, oldVal, newVal) -> {
            if (mutex.get()) return;
            mutex.set(true);
            done.set(newVal != null);
            mutex.set(false);
        });

        priority = new SimpleStringProperty("") {
            @Override
            public void set(String s) {
                if (s.isEmpty() || PRIORITY_CHARS.contains(s)) super.set(s);
            }
        };

        startDate = new ReadOnlyObjectWrapper<>();

        dueDate = new ReadOnlyObjectWrapper<>();

        info = new ReadOnlyStringWrapper("") {
            @Override
            public void set(String s) {
                s = s.trim().replaceAll("\\s{2,}", " ");
                super.set(s);
                if (s.isEmpty()) return;

                Matcher matcher = PATTERN_PROJECT.matcher(s);
                while (matcher.find()) {
                    _PROJECTS.add(matcher.group(1));
                }

                matcher = PATTERN_CONTEXT.matcher(s);
                while (matcher.find()) {
                    _CONTEXTS.add(matcher.group(1));
                }
            }
        };

        instants = FXCollections.observableArrayList();
        duration = new ReadOnlyObjectWrapper<>();

        instants.addListener((ListChangeListener<Instant>) change -> duration.set(getDuration()));
    }

    public BooleanProperty doneProperty() {
        return done;
    }

    public ObjectProperty doneDateProperty() {
        return doneDate;
    }

    public StringProperty priorityProperty() {
        return priority;
    }

    public ObjectProperty startDateProperty() {
        return startDate;
    }

    public ObjectProperty dueDateProperty() {
        return dueDate;
    }

    public StringProperty infoProperty() {
        return info;
    }

    public ObjectProperty durationProperty() {
        return duration;
    }

    public boolean isDone() {
        return done.get();
    }

    public void setDone(LocalDate comDate) {
        this.doneDate.set(comDate);
    }

    public LocalDate getDoneDate() {
        return doneDate.get();
    }

    public String getPriority() {
        return priority.get();
    }

    public void setPriority(String priority) {
        this.priority.set(priority);
    }

    public LocalDate getStartDate() {
        return startDate.get();
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate.set(startDate);
    }

    public LocalDate getDueDate() {
        return dueDate.get();
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate.set(dueDate);
    }

    public String getInfo() {
        return info.get();
    }

    public void setInfo(String info) {
        this.info.set(info);
    }

    public ObservableList<Instant> getInstants() {
        return instants;
    }

    public TDDuration getDuration() {
        if (instants.isEmpty()) return new TDDuration(0);

        long durationSeconds = 0;
        Instant previousInstant = instants.get(0);
        boolean add = false;
        for (Instant instant : instants) {
            if (add)
                durationSeconds += instant.getEpochSecond() - previousInstant.getEpochSecond();
            add = !add;
            previousInstant = instant;
        }

        return new TDDuration(durationSeconds);
    }

    private String getInstantsString() {
        if (instants.isEmpty()) return "";
        StringBuilder stringBuilder = new StringBuilder("duration:");
        for (Instant instant : instants) {
            /*stringBuilder.append(instant.getEpochSecond()).append('-');*/
            stringBuilder.append(DateTimeFormatter.ISO_INSTANT.format(instant)).append(';');

        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        return stringBuilder.toString();
    }

    @Override
    public String toString() {
        return ((isDone() ? "x " + (doneDate.get() == null ? "" : doneDate.get().format(DATE_ISO_FORMATTER)) + " " : "") +
                (!this.priority.get().isEmpty() ? "(" + priority.get() + ") " : "") +
                (this.startDate.get() != null ? (startDate.get() == null ? "" : startDate.get().format(DATE_ISO_FORMATTER)) + " " : "") +
                (this.dueDate.get() != null ? "due:" + (dueDate.get() == null ? "" : dueDate.get().format(DATE_ISO_FORMATTER)) + " " : "") +
                getInfo()).trim() +
                (instants.isEmpty() ? "" : " " + getInstantsString());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TDTask tdTask = (TDTask) o;

        return info.equals(tdTask.info);

    }

    @Override
    public int hashCode() {
        return info.hashCode();
    }

    public static enum TDTaskField {
        Done, CompletionDate, CreationDate, DueDate, Info, Priority
    }

//    private static Set<String> extractValues(String info, String key) {
//        Set<String> items = new LinkedHashSet<>();
//        if (key.isEmpty() || info.isEmpty()) return items;
//
//        int start = 0;
//        while (start >= 0) {
//            String item;
//            start = info.indexOf(key, start);
//            if (start < 0) break;
//            if (start > 0 && info.charAt(start - 1) != ' ') {
//                // the key was found as part of another word
//                start += key.length();
//                continue;
//            }
//            int end = info.indexOf(' ', start);
//            if (end < 0) {
//                // item is the last word
//                item = info.substring(start + key.length());
//                items.add(item);
//                break;
//            } else {
//                // item found
//                item = info.substring(start + key.length(), end);
//                items.add(item);
//            }
//            start = end;
//        }
//
//        return items;
//    }

//    private static String removeKeyValuePair(String info, String key) {
//        String quotedKey = Pattern.quote(key);
//
//        return info.replaceAll("(?U)( )?" + quotedKey + "[^ ]+", "");
//    }

//    public static TDTask parse2(String taskString) {
//        taskString = taskString.trim();
//        TDTask t = new TDTask();
//        if (taskString.isEmpty()) return t;
//
//        int i = 0;
//        // Prototype: x 2013-09-16 (A) 2013-09-16 due:2013-09-16 bla bla bla @con1 @con2 +pro1 +pro2 duration:1111-222-333-444
//
//        // check done
//        if (taskString.length() >= (i + 12) && taskString.charAt(i) == 'x') {
//            String dateString = taskString.substring(2, 12);
//            if (DATE_ISO_PATTERN.matcher(dateString).matches()) {
//                LocalDate comDate = LocalDate.parse(dateString, DATE_ISO_FORMATTER);
//                if (t.getStartDate() != null && comDate.isBefore(t.getStartDate())) {
//                    t.startDate.set(null);
//                }
//                t.setDone(comDate);
//
//                i = 13;
//                // i might have exceeded the string length, in case the string has no more information
//                if (i >= taskString.length()) return t;
//            }
//        }
//
//        // check priority
//        if (taskString.length() > (i + 3) && taskString.charAt(i) == '(' && taskString.charAt(i + 2) == ')' && taskString.charAt(i + 3) == ' ') {
//            String p = taskString.substring(i + 1, i + 2);
//            if (PRIORITY_CHARS.contains(p)) {
//                t.setPriority(p);
//                i += 4;
//                // i might have exceeded the string length, in case the string has no more information
//                if (i >= taskString.length()) return t;
//            }
//        }
//
//        // check start date
//        int j = i + 10;
//        if (taskString.length() >= j) {
//            String dateString = taskString.substring(i, j);
//            if (DATE_ISO_PATTERN.matcher(dateString).matches()) {
//                t.setStartDate(LocalDate.parse(dateString, DATE_ISO_FORMATTER));
//                i += 11;
//                // i might have exceeded the string length, in case the string has no more information
//                if (i >= taskString.length()) return t;
//            }
//        }
//
//        // check due date
//        Set<String> tmpValues = extractValues(taskString, "due:");
//        if (!tmpValues.isEmpty()) {
//            String dateString = tmpValues.iterator().next();
//            if (DATE_ISO_PATTERN.matcher(dateString).matches()) {
//                LocalDate dueDate = LocalDate.parse(dateString, DATE_ISO_FORMATTER);
//                t.setDueDate(dueDate);
//
//                taskString = removeKeyValuePair(taskString, "due:");
//            }
//        }
//
//        // check duration
//        tmpValues = extractValues(taskString, "duration:");
//        if (!tmpValues.isEmpty()) {
//            String[] instantStrings = tmpValues.iterator().next().split("-");
//            for (String instantString : instantStrings) {
//                if (INTEGER_PATTERN.matcher(instantString).matches())
//                    t.instants.add(Instant.ofEpochSecond(Long.parseLong(instantString)));
//            }
//            taskString = removeKeyValuePair(taskString, "duration:");
//        }
//
//        // finally, check info
//        t.setInfo(taskString.substring(i));
//
//        return t;
//    }

    //    private static final Pattern DATE_ISO_PATTERN = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");
    //    private static final Pattern INTEGER_PATTERN = Pattern.compile("\\d+");
    //    private static final Pattern WORD_PATTERN = Pattern.compile("(?U)\\w+");
}
