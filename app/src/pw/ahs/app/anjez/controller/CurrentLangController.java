/*******************************************************************************
 Copyright (c) 2014 - Anas H. Sulaiman (ahs.pw)

  This file is part of Anjez.
  Anjez is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 ******************************************************************************/

package pw.ahs.app.anjez.controller;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.function.Function;

public class CurrentLangController {
    private String lang = "en";
    private final Collection<Function<String, Void>> listeners = new LinkedHashSet<>();
    private final Collection<Function<String, Void>> beforeListeners = new LinkedHashSet<>();

    private static CurrentLangController instance = null;

    public static CurrentLangController getInstance() {
        if (instance == null)
            instance = new CurrentLangController();
        return instance;
    }

    private CurrentLangController() {
    }

    public void setLang(String lang) {
        for (Function<String, Void> f : beforeListeners) {
            f.apply(lang);
        }
        this.lang = lang;
        for (Function<String, Void> f : listeners) {
            f.apply(lang);
        }
    }

    public String getLang() {
        return lang;
    }

    public void addListener(Function<String, Void> listener) {
        listeners.add(listener);
    }

    public void addBeforeChangeListener(Function<String, Void> listener) {
        beforeListeners.add(listener);
    }
}
