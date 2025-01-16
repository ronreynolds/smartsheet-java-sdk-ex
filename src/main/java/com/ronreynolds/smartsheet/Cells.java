package com.ronreynolds.smartsheet;

import com.google.common.base.Preconditions;
import com.smartsheet.api.Smartsheet;
import com.smartsheet.api.SmartsheetException;
import com.smartsheet.api.models.Cell;
import com.smartsheet.api.models.Row;
import com.smartsheet.api.models.Sheet;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * util methods for dealing with Cells (the values of Columns within Sheets)
 */
public class Cells {
    /**
     * @param client   - client to use to access the Smartsheet API
     * @param sheetId  - ID of sheet into which we want to set the cell value
     * @param rowId    - ID of the row in the sheet into which we want to set the cell value
     * @param columnId - ID of the column in the row in the sheet in which we want to set the cell value
     * @param value    - the value we want to set into the cell in the column in the row in the sheet that Jack built. :)
     * @return the updated row
     * @throws SmartsheetException if anything goes wrong
     */
    @Nonnull
    public static List<Row> setCellValue(@Nonnull Smartsheet client, long sheetId, long rowId, long columnId, Object value) throws SmartsheetException {
        List<Cell> cell = Collections.singletonList(new Cell()
                .setColumnId(columnId).setValue(value).setStrict(true).setHyperlink(null).setLinkInFromCell(null));
        Row updatedRow = new Row(rowId);
        updatedRow.setCells(cell);
        List<Row> updatedRows = client.sheetResources().rowResources().updateRows(sheetId, Collections.singletonList(updatedRow));
        if (updatedRows == null || updatedRows.size() != 1) {
            throw new RuntimeException(String.format("update FAILED - row:%d column:%d value:'%s'", rowId, columnId, value));
        }
        return updatedRows;
    }

    /**
     * get the Cell for the specified column the provided row
     *
     * @param row      is the row from which we want a Cell
     * @param columnId is the column id for the Cell we want
     * @return the Cell in the Row for the columnId or Optional.empty if none is found
     */
    public static Optional<Cell> getCellForColumn(Row row, long columnId) {
        return row.getCells().stream().filter(cell -> cell.getColumnId() == columnId).findFirst();
    }

    /**
     * determine if a Cell in the provided row and column match the provided match criteria
     *
     * @param row       is the row for which we want to check a cell value
     * @param columnId  is the column of the cell we want to check
     * @param cellMatch is the match criteria by which we determine if the cell for the column in the row matches
     * @return true if the specified row's column's cell matches; false if not or no cell found for columnId in row
     */
    public static boolean rowHasMatchingCell(Row row, long columnId, Predicate<? super Cell> cellMatch) {
        return getCellForColumn(row, columnId).stream().anyMatch(cellMatch);
    }

    /**
     * set the values of many cells in a single row given a map of column ID and cell value
     *
     * @param client         is to access the Smartsheet API
     * @param sheetId        is the ID of the sheet with the row we want to update
     * @param rowId          is the ID of teh row we want to update in the sheet
     * @param columnValueMap is a collection of (rowId,cellValue) pairs to be applied to the specified row
     * @return the list of Rows updated
     * @throws SmartsheetException if anything goes wrong talking to the API
     */
    public static List<Row> setCellValues(@Nonnull Smartsheet client, long sheetId, long rowId,
                                          Map<Long, Object> columnValueMap)
            throws SmartsheetException {
        Row updatedRow = new Row(rowId)
                .setCells(columnValueMap.entrySet().stream()
                        .map(entry -> new Cell(entry.getKey()).setValue(entry.getValue()).setStrict(true)
                                .setHyperlink(null).setLinkInFromCell(null))
                        .collect(Collectors.toList()));
        List<Row> updatedRows = client.sheetResources().rowResources().updateRows(sheetId, List.of(updatedRow));
        Preconditions.checkState(updatedRows != null && updatedRows.size() == 1, "failed to update row");
        return updatedRows;
    }

    /**
     * @param sheet - sheet from which we get the Column info
     * @return a Stream of row cell values mapped by column title
     */
    public static Stream<Map<String, Object>> getCellValuesByNameStream(@Nonnull Sheet sheet) {
        List<Row> rowList = sheet.getRows();
        // for each row get the list of cells
        return rowList.stream()
                .map(Cells::cellsByColumnId)
                .map(cellMap -> {
                    Map<String, Object> cellValueMap = new HashMap<>();
                    for (var column : sheet.getColumns()) {
                        Cell cell = cellMap.get(column.getId());
                        cellValueMap.put(column.getTitle(), cell != null ? cell.getValue() : null);
                    }
                    return cellValueMap;
                });
    }

    /**
     * @param row from which to extract cells
     * @return Map of cells by their column-id
     */
    @Nonnull
    public static Map<Long, Cell> cellsByColumnId(@Nonnull Row row) {
        return row.getCells().stream().collect(Collectors.toMap(Cell::getColumnId, Function.identity()));
    }
}
