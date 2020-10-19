/*******************************************************************************
 Copyright (c) 2014 - Anas H. Sulaiman (ahs.pw)

  This file is part of Anjez.
  Anjez is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 ******************************************************************************/

package pw.ahs.app.anjez.gui.helper;

import com.dropbox.core.DbxException;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import pw.ahs.app.anjez.gui.Dialogs;
import pw.ahs.app.anjez.gui.UIHelper;
import pw.ahs.app.anjez.utils.SimpleAES;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;

import static pw.ahs.app.anjez.Globals.*;
import static pw.ahs.app.anjez.controller.PrefsController.pAT;
import static pw.ahs.app.anjez.controller.PrefsController.pFile_Path;

public class DropBoxHelper {

    private final SimpleAES simpleAES;

    private static DropBoxHelper instance = null;

    public static DropBoxHelper getInstance() {
        if (instance == null)
            instance = new DropBoxHelper();
        return instance;
    }

    private DropBoxHelper() {
        simpleAES = initAES();
    }

    private SimpleAES initAES() {
        try {
            return new SimpleAES(new String[]{"3BCrIaPzS5dqelYDAgqZhQ==", "59FVE3g+klTYvtgFUgAOhg=="});
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isAvailable() {
        return simpleAES != null;
    }

    public boolean connectDropbox() {
        if (dropBoxController.isConnected()) return true;

        String accessToken = prefsController.getPref(pAT);
        if (!accessToken.isEmpty()) {
            try {
                accessToken = simpleAES.decrypt(accessToken);
            } catch (BadPaddingException | IllegalBlockSizeException e) {
                accessToken = "";
            }
        }

        Stage stgWaiting = Dialogs.createWaitingDialog(view.getStage(), i18n.getString("m.working"), i18n.getString("dropbox.msg.connecting"), i18n.getString("button.cancel"));
        StringProperty errMsg = new SimpleStringProperty("");
        Thread thread;
        if (accessToken.isEmpty()) {
            // new access token
            String authUrl = dropBoxController.createAuthUrl();
            String code = Dialogs.showDropBoxNewConnectionDialog(
                    view.getStage(),
                    authUrl,
                    i18n.getString("dropbox.title"),
                    i18n.getString("dropbox.msg.get-code"),
                    i18n.getString("dropbox.auth-url"),
                    i18n.getString("dropbox.verify-code"),
                    i18n.getString("button.verify"),
                    i18n.getString("button.cancel"));
            if (code.isEmpty()) return false;

            thread = new Thread(() -> {
                try {
                    dropBoxController.verifyConnection(code);
                    dropBoxController.connect();
                } catch (DbxException e) {
                    errMsg.set(e.getMessage());
                }

                Platform.runLater(stgWaiting::hide);
            });

            thread.start();
            stgWaiting.showAndWait();

            if (!errMsg.get().isEmpty()) return showErrorMsg(errMsg.get());

            String accountName = dropBoxController.getAccountName();
            if (accountName.isEmpty()) return showErrorMsg("");

            if (!showConnectSuccess(accountName)) return false;

            try {
                prefsController.setPref(pAT, simpleAES.encrypt(dropBoxController.getAccessToken()));
            } catch (UnsupportedEncodingException | BadPaddingException | IllegalBlockSizeException e) {
                e.printStackTrace();
            }

            return true;
        }

        // old access token
        final String fAccessToken = accessToken.trim();
        thread = new Thread(() -> {
            try {
                dropBoxController.connect(fAccessToken);
            } catch (DbxException e) {
                errMsg.set(e.getMessage());
            }
            Platform.runLater(stgWaiting::hide);
        });
        thread.start();
        stgWaiting.showAndWait();

        if (!errMsg.get().isEmpty()) return showErrorAskForget(errMsg.get());

        String accountName = dropBoxController.getAccountName();
        if (accountName.isEmpty()) return showErrorAskForget("");

        return showConnectSuccess(accountName);
    }

    private boolean showErrorMsg(String errMsg) {
        Dialogs.showMessageDialog(
                view.getStage(),
                i18n.getString("dropbox.error.title"),
                errMsg.startsWith("dropbox.") ? i18n.getString(errMsg) : i18n.getString("dropbox.error.other"),
                i18n.getString("button.dismiss")
        );
        return false;
    }

    private boolean showErrorAskForget(String errMsg) {
        if (Dialogs.showYesNoDialog(
                view.getStage(),
                i18n.getString("dropbox.error.title"),
                errMsg.startsWith("dropbox.") ? errMsg : i18n.getString("dropbox.error.other"),
                i18n.getString("button.forget-account"),
                i18n.getString("button.dismiss"),
                "",
                2
        ) == Dialogs.Result.YES) {
            forget();
        }

        return false;
    }

    private boolean showConnectSuccess(String accountName) {
        Dialogs.Result result = Dialogs.showYesNoDialog(
                view.getStage(),
                i18n.getString("dropbox.title"),
                String.format(i18n.getString("dropbox.msg.connected-successfully"), accountName),
                i18n.getString("button.nice"),
                i18n.getString("button.forget-account"),
                i18n.getString("button.disconnect"),
                1);
        if (result == Dialogs.Result.CANCEL) {
            dropBoxController.disconnect();
            return false;
        }
        if (result == Dialogs.Result.NO) {
            forget();
            return false;
        }

        return true;
    }

    private void forget() {
        Stage stgWaiting = Dialogs.createWaitingDialog(view.getStage(), i18n.getString("m.working"), i18n.getString("dropbox.msg.connecting"), i18n.getString("button.cancel"));
        Thread thread = new Thread(() -> {
            dropBoxController.removeAccount();
            Platform.runLater(stgWaiting::hide);
        });
        thread.start();
        stgWaiting.showAndWait();
        prefsController.setPref(pAT, "");
    }

    public void dbxOpen() {
        if (view.waitForTracking()) return;

        // 1. if not already connected to dropbox, connect
        // 2. if a file is already open
        // 2.1 if the same name on dropbox does not exist, display message
        // 2.2 warn for overwrite
        // 2.3 open
        // 3. get list of files
        // 4. if no files, display message
        // 5. if only one file, choose a folder, open
        // 6. if more than one, display chooser, open

        if (!connectDropbox()) return;

        final String filePath = prefsController.getPref(pFile_Path);
        Thread thread;
        Stage stgWaiting = Dialogs.createWaitingDialog(view.getStage(), i18n.getString("m.working"), i18n.getString("dropbox.msg.connecting"), i18n.getString("button.cancel"));
        StringProperty errMsg = new SimpleStringProperty("");
        if (!filePath.isEmpty()) {
            // a file is already open

            BooleanProperty state = new SimpleBooleanProperty(false);
            thread = new Thread(() -> {
                try {
                    state.set(dropBoxController.fileExists(filePath));
                } catch (DbxException e) {
                    errMsg.set(e.getMessage());
                }
                Platform.runLater(stgWaiting::hide);
            });
            thread.start();
            stgWaiting.showAndWait();

            if (!errMsg.get().isEmpty()) {
                showErrorAskForget(errMsg.get());
                return;
            }

            if (!state.get()) {
                showErrorMsg("dropbox.error.file-does-not-exist");
                return;
            }

            Dialogs.Result result = Dialogs.showYesNoDialog(
                    view.getStage(),
                    i18n.getString("dropbox.warning.title"),
                    i18n.getString("dropbox.warning.overwrite-current-file"),
                    i18n.getString("button.overwrite"),
                    i18n.getString("button.cancel"),
                    "",
                    2);
            if (result == Dialogs.Result.YES) {
                thread = new Thread(() -> {
                    try {
                        dropBoxController.open(filePath);
                    } catch (DbxException | IOException e) {
                        errMsg.set(e.getMessage());
                    }
                    Platform.runLater(stgWaiting::hide);
                });
                thread.start();
                stgWaiting.showAndWait();

                if (!errMsg.get().isEmpty()) {
                    showErrorMsg(errMsg.get());
                    return;
                }

                UIHelper.io.openFile(filePath);
            }
            return;
        } // a file is already open

        // no file is open
        ArrayList<Object> tmp = new ArrayList<>();
        thread = new Thread(() -> {
            String[] files = null;
            try {
                files = dropBoxController.getFilesList();
            } catch (DbxException e) {
                errMsg.set(e.getMessage());
            }
            if (errMsg.get().isEmpty() && files != null)
                Collections.addAll(tmp, files);

            Platform.runLater(stgWaiting::hide);
        });
        thread.start();
        stgWaiting.showAndWait();

        if (!errMsg.get().isEmpty()) {
            showErrorAskForget(errMsg.get());
            return;
        }

        if (tmp.isEmpty()) {
            showErrorMsg("dropbox.error.no-files");
            return;
        }

        String filePath2;
        if (tmp.size() == 1) {
            // one file on dropbox
            DirectoryChooser dc = new DirectoryChooser();
            dc.setTitle(i18n.getString("m.save-dialog-folder-title"));
            dc.setInitialDirectory(new File(System.getProperty("user.home")));
            File f = dc.showDialog(view.getStage());
            if (f == null) return;

            String[] names = f.list((dir, name) -> name.equals(tmp.get(0)));
            if (names.length > 0) {
                Dialogs.Result result = Dialogs.showYesNoDialog(
                        view.getStage(),
                        i18n.getString("dropbox.warning.title"),
                        i18n.getString("dropbox.warning.overwrite-current-file"),
                        i18n.getString("button.overwrite"),
                        i18n.getString("button.cancel"),
                        "",
                        2);
                if (result != Dialogs.Result.YES) return;
            }

            filePath2 = f.getAbsolutePath() + File.separator + tmp.get(0);
        } // one file on dropbox
        else {
            // many files on dropbox
            String fileName = (String) Dialogs.showInputDialog(
                    view.getStage(),
                    i18n.getString("dropbox.title"),
                    i18n.getString("dropbox.choose-file"),
                    i18n.getString("button.choose"),
                    i18n.getString("button.cancel"),
                    tmp,
                    tmp.get(0));
            if (fileName == null) return;

            DirectoryChooser dc = new DirectoryChooser();
            dc.setTitle(i18n.getString("m.save-dialog-folder-title"));
            dc.setInitialDirectory(new File(System.getProperty("user.home")));
            File f = dc.showDialog(view.getStage());
            if (f == null) return;

            String[] names = f.list((dir, name) -> name.equals(fileName));
            if (names.length > 0) {
                Dialogs.Result result = Dialogs.showYesNoDialog(
                        view.getStage(),
                        i18n.getString("dropbox.warning.title"),
                        i18n.getString("dropbox.warning.overwrite-local-file"),
                        i18n.getString("button.overwrite"),
                        i18n.getString("button.cancel"),
                        "",
                        2);
                if (result != Dialogs.Result.YES) return;
            }

            filePath2 = f.getAbsolutePath() + File.separator + fileName;
        } // many files on dropbox

        errMsg.set("");
        final String filePath3 = filePath2;
        thread = new Thread(() -> {
            try {
                dropBoxController.open(filePath3);
            } catch (DbxException | IOException e) {
                errMsg.set(e.getMessage());
            }
            Platform.runLater(stgWaiting::hide);
        });
        thread.start();
        stgWaiting.showAndWait();

        if (!errMsg.get().isEmpty()) {
            showErrorMsg(errMsg.get());
            return;
        }

        UIHelper.io.openFile(filePath2);
    }

    public void dbxReload() {
        if (view.waitForTracking()) return;

        // 1. if no file is open, no op
        // 2. if not already connected to dropbox, connect
        // 3. if file doesn't exists, display message
        // 4. get the file, read the tasks

        String filePath = prefsController.getPref(pFile_Path);
        if (filePath.isEmpty() || !connectDropbox()) return;

        Thread thread;
        Stage stgWaiting = Dialogs.createWaitingDialog(view.getStage(), i18n.getString("m.working"), i18n.getString("dropbox.msg.connecting"), i18n.getString("button.cancel"));
        BooleanProperty state = new SimpleBooleanProperty(false);
        StringProperty errMsg = new SimpleStringProperty("");

        thread = new Thread(() -> {
            try {
                state.set(dropBoxController.fileExists(filePath));
            } catch (DbxException e) {
                errMsg.set(e.getMessage());
            }
            Platform.runLater(stgWaiting::hide);
        });
        thread.start();
        stgWaiting.showAndWait();

        if (!errMsg.get().isEmpty()) {
            showErrorAskForget(errMsg.get());
            return;
        }

        if (!state.get()) {
            showErrorMsg("dropbox.error.file-does-not-exist");
            return;
        }

        if (Dialogs.showYesNoDialog(
                view.getStage(),
                i18n.getString("dropbox.warning.title"),
                i18n.getString("dropbox.warning.overwrite-local-file"),
                i18n.getString("button.overwrite"),
                i18n.getString("button.cancel"),
                "",
                2) != Dialogs.Result.YES)
            return;

        thread = new Thread(() -> {
            try {
                dropBoxController.open(filePath);
            } catch (DbxException | IOException e) {
                errMsg.set(e.getMessage());
            }
            Platform.runLater(stgWaiting::hide);
        });

        thread.start();
        stgWaiting.showAndWait();

        if (!errMsg.get().isEmpty()) {
            showErrorAskForget(errMsg.get());
            return;
        }

        UIHelper.io.openFile(filePath);
    }

    public void dbxSave() {
        // 1. if no file is open, display message to save a local copy first
        // 2. if not already connected to dropbox, connect
        // 3. if file already exists in dropbox, warn
        // 4. save

        String filePath = prefsController.getPref(pFile_Path);
        if (filePath.isEmpty()) {
            showErrorMsg("dropbox.error.save-local-first");
            return;
        }

        if (!connectDropbox() || view.waitForSave()) return;

        Thread thread;
        Stage stgWaiting = Dialogs.createWaitingDialog(view.getStage(), i18n.getString("m.working"), i18n.getString("dropbox.msg.connecting"), i18n.getString("button.cancel"));
        BooleanProperty state = new SimpleBooleanProperty(false);
        StringProperty errMsg = new SimpleStringProperty("");
        thread = new Thread(() -> {
            try {
                state.set(dropBoxController.fileExists(filePath));
            } catch (DbxException e) {
                errMsg.set(e.getMessage());
            }
            Platform.runLater(stgWaiting::hide);
        });
        thread.start();
        stgWaiting.showAndWait();

        if (!errMsg.get().isEmpty()) {
            showErrorAskForget(errMsg.get());
            return;
        }

        if (state.get()) {
            Dialogs.Result result = Dialogs.showYesNoDialog(
                    view.getStage(),
                    i18n.getString("dropbox.warning.title"),
                    i18n.getString("dropbox.warning.overwrite-remote-file"),
                    i18n.getString("button.overwrite"),
                    i18n.getString("button.cancel"),
                    "",
                    2);
            if (result != Dialogs.Result.YES) return;
        }

        thread = new Thread(() -> {
            try {
                dropBoxController.save(filePath);
            } catch (DbxException | IOException e) {
                errMsg.set(e.getMessage());
            }
            Platform.runLater(stgWaiting::hide);
        });
        thread.start();
        stgWaiting.showAndWait();

        if (!errMsg.get().isEmpty()) {
            showErrorMsg(errMsg.get());
            return;
        }

        Dialogs.showMessageDialog(
                view.getStage(),
                i18n.getString("dropbox.title"),
                i18n.getString("dropbox.msg.saved-successfully"),
                i18n.getString("button.nice")
        );
    }
}
