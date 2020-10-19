/*******************************************************************************
 Copyright (c) 2014 - Anas H. Sulaiman (ahs.pw)

  This file is part of Anjez.
  Anjez is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 ******************************************************************************/

package pw.ahs.app.anjez.controller;

import pw.ahs.app.anjez.model.TDTask;

import java.time.Instant;

public class TrackingController {

    private TDTask trackedTask;
    private boolean tracking;
    private Instant startInstant;
    private Instant endInstant;

    private static TrackingController instance = null;

    public static TrackingController getInstance() {
        if (instance == null)
            instance = new TrackingController();
        return instance;
    }

    private TrackingController() {
        clear();
    }

    public void setTrackedTask(TDTask trackedTask) {
        clear();
        this.trackedTask = trackedTask;
    }

    public void clear() {
        trackedTask = null;
        tracking = false;
        startInstant = endInstant = null;
    }

    public TDTask getTrackedTask() {
        return trackedTask;
    }

    public boolean isTracking() {
        return tracking;
    }

    public void startTracking(TDTask tdTask) {
        setTrackedTask(tdTask);
        startTracking();
    }

    public void startTracking() {
        if (trackedTask != null && !tracking) {
            startInstant = Instant.now();
            endInstant = null;
            tracking = true;
        } else {
            throw new IllegalStateException("Already tracking or tracked task not set!");
        }
    }

    public void stopTracking() {
        if (trackedTask != null && tracking) {
            endInstant = Instant.now();
            tracking = false;
            trackedTask.getInstants().addAll(startInstant, endInstant);
        } else {
            throw new IllegalStateException("Not tracking or tracked task not set");
        }
    }

    public long getDurationSeconds() {
        if (startInstant != null) {
            if (endInstant != null)
                return endInstant.minusSeconds(startInstant.getEpochSecond()).getEpochSecond();
            else
                return Instant.now().minusSeconds(startInstant.getEpochSecond()).getEpochSecond();
        }
        return 0;
    }
}
