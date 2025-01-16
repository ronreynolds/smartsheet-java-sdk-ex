package com.ronreynolds.smartsheet;

import com.smartsheet.api.Smartsheet;
import com.smartsheet.api.SmartsheetException;
import com.smartsheet.api.models.Workspace;
import lombok.NonNull;

import java.util.Optional;

public class Workspaces {
    public static Optional<Workspace> findWorkspaceByName(@NonNull Smartsheet api, @NonNull String name)
            throws SmartsheetException {
        return api.workspaceResources()
                .listWorkspaces(Constants.ALL_PAGES)
                .getData()
                .stream()
                .filter(ws -> name.equals(ws.getName()))
                .findFirst();
    }
}
