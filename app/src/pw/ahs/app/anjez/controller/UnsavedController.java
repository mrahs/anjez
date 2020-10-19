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

public class UnsavedController {
    private boolean saved = true;
    private final Collection<Function<Boolean, Void>> listeners = new LinkedHashSet<>();

    private static UnsavedController instance = null;

    public static UnsavedController getInstance() {
        if (instance == null)
            instance = new UnsavedController();
        return instance;
    }

    private UnsavedController() {
    }

    public void setSaved(boolean state) {
        if (state == this.saved) return;
        this.saved = state;
        for (Function<Boolean, Void> f : listeners) {
            f.apply(state);
        }
    }

    public boolean isSaved() {
        return saved;
    }

    public void addListener(Function<Boolean, Void> listener) {
        listeners.add(listener);
    }
}
