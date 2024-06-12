package com.ronreynolds.smartsheet;

import com.smartsheet.api.Smartsheet;
import com.smartsheet.api.SmartsheetException;
import com.smartsheet.api.models.Folder;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * utility methods for dealing with Folders in the Smartsheet API
 */
public class Folders {
    public static Optional<Folder> findFirstFolderByName(@Nonnull Smartsheet api, @Nonnull String name)
            throws SmartsheetException {
        return api.homeResources().folderResources()
                .listFolders(Constants.ALL_PAGES)
                .getData()
                .stream()
                .filter(ws -> name.equals(ws.getName()))
                .findFirst();
    }

    public static List<Folder> findFoldersByName(@Nonnull Smartsheet api, @Nonnull String name)
            throws SmartsheetException {
        return api.homeResources().folderResources()
                .listFolders(Constants.ALL_PAGES)
                .getData()
                .stream()
                .filter(ws -> name.equals(ws.getName()))
                .collect(Collectors.toList());
    }

    public static <T extends Folder> T populateIfNeeded(@Nonnull Smartsheet api, @Nonnull T folder)
            throws SmartsheetException {
        // it seems that if the folder is populated then the folders list is non-null
        if (folder.getFolders() == null) {
            Folder folderData = api.folderResources().getFolder(folder.getId(), Constants.ALL_SOURCES);
            if (folderData != null) {
                // even tho the ref within an Optional is immutable our Folder type is not
                folder.setFavorite(folderData.getFavorite());
                folder.setFolders(folderData.getFolders());
                folder.setReports(folderData.getReports());
                folder.setSheets(folderData.getSheets());
                folder.setSights(folderData.getSights());
                folder.setTemplates(folderData.getTemplates());
            }
        }
        return folder;
    }
}