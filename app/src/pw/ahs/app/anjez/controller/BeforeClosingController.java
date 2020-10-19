/*******************************************************************************
 Copyright (c) 2014 - Anas H. Sulaiman (ahs.pw)

  This file is part of Anjez.
  Anjez is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 ******************************************************************************/

package pw.ahs.app.anjez.controller;

import java.util.ArrayList;
import java.util.Collection;

public class BeforeClosingController {
    private final Collection<Runnable> beforeClosingController = new ArrayList<>();
    private static BeforeClosingController instance = null;

    public static BeforeClosingController getInstance() {
        if (instance == null)
            instance = new BeforeClosingController();
        return instance;
    }

    private BeforeClosingController() {
    }

    public void addAction(Runnable action) {
        beforeClosingController.add(action);
    }

    public void doAll() {
        for (Runnable action : beforeClosingController) {
            action.run();
        }
    }
}
