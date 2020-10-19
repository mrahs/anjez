/*******************************************************************************
 Copyright (c) 2014 - Anas H. Sulaiman (ahs.pw)

  This file is part of Anjez.
  Anjez is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 ******************************************************************************/

package pw.ahs.app.anjez.controller;

import com.dropbox.core.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class DropBoxController {
    private final String appNameVer;

    private String accessToken;
    private boolean connected;
    private String accountName;

    private DbxRequestConfig dbxConfig;
    private final DbxAppInfo dbxAppInfo;
    private DbxWebAuthNoRedirect dbxWebAuth;
    private DbxClient dbxClient;

    private static DropBoxController instance = null;

    public static DropBoxController getInstance(String appName, String appVer) {
        if (instance == null)
            instance = new DropBoxController(appName, appVer);
        return instance;
    }

    private DropBoxController(String appName, String appVer) {
        this.appNameVer = appName + "/" + appVer;

        accessToken = "";
        accountName = "";
        connected = false;

        dbxAppInfo = new DbxAppInfo("832vubxhh8mprqk", "zkdvrtrcaahv1r0");
        dbxWebAuth = null;
        dbxClient = null;
    }

    public void setUserLocal(String userLocal) {
        dbxConfig = new DbxRequestConfig(appNameVer, userLocal);
    }

    public boolean isConnected() {
        return connected;
    }

    public String createAuthUrl() {
        disconnect();
        dbxWebAuth = new DbxWebAuthNoRedirect(dbxConfig, dbxAppInfo);
        return dbxWebAuth.start();
    }

    public void disconnect() {
        dbxWebAuth = null;
        dbxClient = null;
        accountName = "";
        connected = false;
    }

    public boolean verifyConnection(String verificationCode) throws DbxException {
        if (dbxWebAuth == null) throw new DbxException("dropbox.error.nothing-to-verify");

        DbxAuthFinish dbxAuthFinish;
        try {
            dbxAuthFinish = dbxWebAuth.finish(verificationCode);
        } catch (DbxException e) {
            throw new DbxException("dropbox.error.invalid-code", e);
        }

        this.accessToken = dbxAuthFinish.accessToken;

        return true;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public boolean connect() throws DbxException {
        return connect(accessToken);
    }

    public boolean connect(String accessToken) throws DbxException {
        if (accessToken.isEmpty()) throw new DbxException("dropbox.error.invalid-access-token");

        dbxClient = new DbxClient(dbxConfig, accessToken);
        accountName = dbxClient.getAccountInfo().displayName;

        return connected = true;
    }

    public String getAccountName() {
        return accountName;
    }

    public void removeAccount() {
        if (dbxClient != null)
            try {
                dbxClient.disableAccessToken();
            } catch (DbxException ignored) {
            }
        accessToken = "";

        disconnect();
    }

    public void open(String localPath) throws DbxException, IOException {
        checkConnection();

        Path path = Paths.get(localPath);
        String dbxPath = "/" + path.getFileName().toString();
        OutputStream outputStream = Files.newOutputStream(path);
        dbxClient.getFile(dbxPath, null, outputStream);
        outputStream.close();
    }

    public String[] getFilesList() throws DbxException {
        checkConnection();

        DbxEntry.WithChildren listing;
        listing = dbxClient.getMetadataWithChildren("/");

        if (listing.children.size() == 0) {
            return new String[0];
        }

        ArrayList<String> list = new ArrayList<>(listing.children.size());
        for (DbxEntry child : listing.children) {
            list.add(child.name);
        }

        return list.toArray(new String[list.size()]);
    }

    public void save(String localPath) throws DbxException, IOException {
        checkConnection();

        Path path = Paths.get(localPath);
        if (!Files.exists(path, LinkOption.NOFOLLOW_LINKS))
            throw new DbxException("dropbox.error.local-file-does-not-exist");

        String dbxPath = "/" + path.getFileName().toString();
        DbxEntry dbxEntry;
        dbxEntry = dbxClient.getMetadata(dbxPath);

        InputStream inputStream = Files.newInputStream(path);
        dbxClient.uploadFile(
                dbxPath,
                dbxEntry == null ? DbxWriteMode.add() : DbxWriteMode.update(dbxEntry.asFile().rev),
                path.toFile().length(),
                inputStream);
        inputStream.close();
    }

    public boolean fileExists(String filePath) throws DbxException {
        String[] files = getFilesList();

        Path path = Paths.get(filePath);
        String fileName = path.getFileName().toString();

        for (String name : files) {
            if (name.equals(fileName)) return true;
        }
        return false;
    }

    private void checkConnection() throws DbxException {
        if (!isConnected()) throw new DbxException("dropbox.error.not-connected");
    }
}
