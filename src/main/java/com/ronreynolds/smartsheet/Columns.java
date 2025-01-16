package com.ronreynolds.smartsheet;

import com.smartsheet.api.models.Column;
import com.smartsheet.api.models.Sheet;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class Columns {
    /**
     * @param sheet - sheet from which we want a map of all the columns by name ("title")
     * @return a Map of all columns in the specified sheet by name ("title")
     */
    @Nonnull
    public static Map<String, Column> buildColumnByNameMap(@Nonnull Sheet sheet) {
        return sheet.getColumns().stream().collect(Collectors.toMap(Column::getTitle, Function.identity()));
    }
}
