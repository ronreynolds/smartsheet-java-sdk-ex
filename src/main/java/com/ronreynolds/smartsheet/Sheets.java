package com.ronreynolds.smartsheet;

import com.google.common.base.Preconditions;
import com.smartsheet.api.Smartsheet;
import com.smartsheet.api.SmartsheetException;
import com.smartsheet.api.models.Cell;
import com.smartsheet.api.models.Column;
import com.smartsheet.api.models.ContainerDestination;
import com.smartsheet.api.models.Folder;
import com.smartsheet.api.models.PagedResult;
import com.smartsheet.api.models.Row;
import com.smartsheet.api.models.Sheet;
import com.smartsheet.api.models.Workspace;
import com.smartsheet.api.models.enums.DestinationType;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;


/**
 * a collection of utility methods for working with Sheets (and Rows and Cells)
 */
@SuppressWarnings("unused")
public class Sheets {
    static String LINE_DELIMITER = "";    // no line-breaks by default

    @NonNull
    public static CharSequence toString(@NonNull Sheet sheet) {
        try {
            return toString(sheet, null);
        } catch (SmartsheetException e) {
            throw new IllegalStateException("should be impossible", e);
        }
    }

    @NonNull
    public static CharSequence toString(@NonNull Sheet sheet, Smartsheet client, ToStringOptions... options)
            throws SmartsheetException {
        StringBuilder buf = new StringBuilder();
        buf.append(String.format("{{id:%d%s name:'%s'%s rowCount:%d%s version:%d%s owner:%s(%d)%s source:%s%s accessLevel:%s%s " +
                        "readOnly:%s%s link:%s%s ganttEnabled:%s%s dependEnabled:%s%s resMgmntEnabled:%s%s favorite:%s%s}%n",
                sheet.getId(), LINE_DELIMITER, sheet.getName(), LINE_DELIMITER, sheet.getTotalRowCount(), LINE_DELIMITER,
                sheet.getVersion(), LINE_DELIMITER, sheet.getOwner(), sheet.getOwnerId(), LINE_DELIMITER,
                sheet.getSource(), LINE_DELIMITER, sheet.getAccessLevel(), LINE_DELIMITER, sheet.getReadOnly(),
                LINE_DELIMITER, sheet.getPermalink(), LINE_DELIMITER, sheet.getGanttEnabled(), LINE_DELIMITER,
                sheet.getDependenciesEnabled(), LINE_DELIMITER, sheet.getResourceManagementEnabled(), LINE_DELIMITER,
                sheet.isFavorite(), LINE_DELIMITER));
        if (options != null && options.length > 0) {
            for (ToStringOptions option : options) {
                if (option != null) {
                    option.appendData(buf, sheet, client);
                }
            }
        }

        buf.append("}");
        return buf;
    }

    public static Row addRow(@NonNull Smartsheet client, long sheetId, @NonNull Supplier<Row> rowProvider) throws SmartsheetException {
        List<Row> rows = addRows(client, sheetId, Collections.singletonList(rowProvider.get()), null);
        return rows.isEmpty() ? null : rows.get(0);
    }

    @NonNull
    public static List<Row> addRows(@NonNull Smartsheet client, long sheetId, @NonNull List<Row> rowData,
                                    Consumer<List<Row>> cb) throws SmartsheetException {
        List<Row> newRows = client.sheetResources().rowResources().addRows(sheetId, rowData);
        Preconditions.checkState(rowData.size() == newRows.size(), "%s rows sent, only %s returned", rowData.size(),
                newRows.size());
        if (cb != null) {
            cb.accept(newRows);
        }
        return newRows;
    }

    @NonNull
    public static List<Row> updateRows(@NonNull Smartsheet client, long sheetId, @NonNull List<Row> rowData,
                                       Consumer<List<Row>> cb) throws SmartsheetException {
        // be sure all forbidden fields are cleared
        // InvalidRequestException: The attribute(s) row.rowNumber, row.createdAt, row.modifiedAt, row.sheetId are not allowed for this operation.
        rowData.forEach((row) -> {
            row.setRowNumber(null);
            row.setCreatedAt(null);
            row.setModifiedAt(null);
            row.setSheetId(null);
        });
        List<Row> newRows = client.sheetResources().rowResources().updateRows(sheetId, rowData);
        Preconditions.checkState(rowData.size() == newRows.size(), "%s rows sent, only %s returned", rowData.size(),
                newRows.size());
        if (cb != null) {
            cb.accept(newRows);
        }
        return newRows;
    }

    @NonNull
    public static String columnInfo(@NonNull Sheet sheet) {
        StringBuilder buf = new StringBuilder();
        for (Column column : sheet.getColumns()) {
            buf.append(String.format("col[%d]={title:%s index:%d primary:%s}", column.getId(), column.getTitle(),
                    column.getIndex(), column.getPrimary()));

//            column.getAutoNumberFormat()
//            column.getFilter()
//            column.getOptions()
//            column.getSymbol()
        }
        return buf.toString();
    }

    public static Sheet getWholeSheet(@NonNull Smartsheet client, long sheetId) throws SmartsheetException {
        return client.sheetResources().getSheet(sheetId, Constants.ALL_SHEET_INCLUSIONS, Constants.NO_OBJECT_EXCLUSIONS,
                Constants.ALL_ROW_IDS, Constants.ALL_ROW_NUMBERS, Constants.ALL_COLUMN_IDS, Constants.NO_PAGE_SIZE_LIMIT,
                Constants.ALL_PAGE_NUMBERS);
    }

    public static Sheet getSheetNoRows(@NonNull Smartsheet client, long sheetId) throws SmartsheetException {
        return client.sheetResources().getSheet(sheetId, Constants.NORMAL_SHEET_INCLUSIONS, Constants.ALL_OBJECT_EXCLUSIONS,
                Collections.emptySet(), Collections.emptySet(), Constants.ALL_COLUMN_IDS, Constants.NO_PAGE_SIZE_LIMIT,
                Constants.ALL_PAGE_NUMBERS);
    }

    @NonNull
    public static List<Sheet> findByName(@NonNull Smartsheet client, @NonNull String sheetName) throws SmartsheetException {
        List<Sheet> results = new ArrayList<>();
        client.sheetResources().listSheets(Constants.ALL_SOURCES, Constants.NO_PAGINATION, null).getData()
                .forEach((sheet) -> {
                    if (sheet.getName().equals(sheetName)) results.add(sheet);
                });
        return results;
    }

    /**
     * remove all the rows from the specified sheet
     */
    public static void clearRows(@NonNull Smartsheet client, @NonNull Sheet sheet) throws SmartsheetException {
        // check if sheet already has no rows;
        // otherwise this call fails with "InvalidRequestException: A required parameter is missing from your request: ids."
        Set<Long> rowIds = sheet.getRows().stream().map(Row::getId).collect(Collectors.toSet());
        if (!rowIds.isEmpty()) {
            client.sheetResources().rowResources().deleteRows(sheet.getId(), rowIds, true);
        }
    }

    @NonNull
    public static Sheet copyAndRefresh(@NonNull Smartsheet client, @NonNull Sheet original,
                                       Folder folder, @NonNull String newSheetName) throws SmartsheetException {
        ContainerDestination destination = new ContainerDestination();
        if (folder != null) {
            destination.setDestinationId(folder.getId());
            destination.setDestinationType(folder instanceof Workspace ? DestinationType.WORKSPACE : DestinationType.FOLDER);
        }
        destination.setNewName(newSheetName);
        Sheet newSheet = client.sheetResources().copySheet(original.getId(), destination, Constants.ALL_SHEET_COPY_INCLUSIONS);
        long newSheetId = newSheet.getId();
        return Objects.requireNonNull(Sheets.getSheetNoRows(client, newSheetId), "failed to find cloned sheet id:" + newSheetId);
    }

    @Deprecated // use Rows.clearLocations(Row) instead
    public static void clearLocations(@NonNull Row row) {
        Rows.clearLocations(row);
    }

    public enum ToStringOptions {
        WITH_COLUMNS {
            @Override
            void appendData(@NonNull StringBuilder buf, @NonNull Sheet sheet, @NonNull Smartsheet client) throws SmartsheetException {
                PagedResult<Column> columnPage = client.sheetResources().columnResources().listColumns(sheet.getId(), null, Constants.ALL_PAGES);
                List<Column> columns = columnPage.getData();
                buf.append(String.format("%ncolumns:{num:%d data:{%n", columns.size()));
                for (Column col : columns) {
                    buf.append(col.getId()).append(":'").append(col.getTitle()).append("',").append(LINE_DELIMITER);
                }
                buf.append("}}");
            }
        },
        WITH_ROW_CONTENT {
            @Override
            void appendData(@NonNull StringBuilder buf, @NonNull Sheet sheet, @NonNull Smartsheet client) throws SmartsheetException {
                buf.append("{rows:{");
                for (Row row : sheet.getRows()) {
                    buf.append(row.getId()).append(":[");
                    for (Cell cell : row.getCells()) {
                        buf.append(cell.getValue()).append(',').append(LINE_DELIMITER);
                    }
                    buf.append("],\n");
                }
                buf.append("}}\n");
            }
        }, ALL {
            @Override
            void appendData(@NonNull StringBuilder buf, @NonNull Sheet sheet, @NonNull Smartsheet client) throws SmartsheetException {
                WITH_COLUMNS.appendData(buf, sheet, client);
                WITH_ROW_CONTENT.appendData(buf, sheet, client);
            }
        };

        abstract void appendData(@NonNull StringBuilder buf, @NonNull Sheet sheet, @NonNull Smartsheet client) throws SmartsheetException;
    }
}
