package com.ronreynolds.smartsheet;

import com.smartsheet.api.SmartsheetBuilder;
import com.smartsheet.api.models.PaginationParameters;
import com.smartsheet.api.models.PaginationParameters.PaginationParametersBuilder;
import com.smartsheet.api.models.enums.AccessLevel;
import com.smartsheet.api.models.enums.ColumnInclusion;
import com.smartsheet.api.models.enums.ObjectExclusion;
import com.smartsheet.api.models.enums.ReportInclusion;
import com.smartsheet.api.models.enums.SheetCopyInclusion;
import com.smartsheet.api.models.enums.SheetInclusion;
import com.smartsheet.api.models.enums.SourceInclusion;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * a collection of constants useful when working with the Smartsheet-API Java-SDK library
 */
public class Constants {
    public static final String PROD_URL = SmartsheetBuilder.DEFAULT_BASE_URI;
    public static final String DEFAULT_AUTH = System.getenv("SMARTSHEET_ACCESS_TOKEN");

    public static final PaginationParameters ALL_PAGES = new PaginationParametersBuilder().setIncludeAll(true).build();
    public static final PaginationParameters NO_PAGINATION = null;

    public static final EnumSet<SheetInclusion> ALL_SHEET_INCLUSIONS
            = EnumSet.of(SheetInclusion.values()[0], SheetInclusion.values());
    public static final EnumSet<SheetInclusion> NORMAL_SHEET_INCLUSIONS = EnumSet.of(
            SheetInclusion.OWNER_INFO,
            SheetInclusion.COLUMN_TYPE,
            SheetInclusion.SOURCE);
    public static final EnumSet<SheetInclusion> NO_SHEET_INCLUSIONS = null;

    public static final EnumSet<SheetCopyInclusion> ALL_SHEET_COPY_INCLUSIONS = EnumSet.allOf(SheetCopyInclusion.class);

    public static final EnumSet<ReportInclusion> ALL_REPORT_INCLUSIONS
            = EnumSet.of(ReportInclusion.values()[0], ReportInclusion.values());
    public static final EnumSet<ReportInclusion> NORMAL_REPORT_INCLUSIONS = ALL_REPORT_INCLUSIONS;
    public static final EnumSet<ReportInclusion> NO_REPORT_INCLUSIONS = null;

    public static final EnumSet<ObjectExclusion> ALL_OBJECT_EXCLUSIONS
            = EnumSet.of(ObjectExclusion.values()[0], ObjectExclusion.values());
    public static final EnumSet<ObjectExclusion> NO_OBJECT_EXCLUSIONS = null;

    public static final EnumSet<SourceInclusion> ALL_SOURCES
            = EnumSet.of(SourceInclusion.values()[0], SourceInclusion.values());
    public static final EnumSet<SourceInclusion> NO_SOURCE_RESTRICTION = null;

    public static final EnumSet<ColumnInclusion> NO_COLUMN_INCLUSIONS = null;
    public static final EnumSet<ColumnInclusion> ALL_COLUMN_INCLUSIONS
            = EnumSet.of(ColumnInclusion.values()[0], ColumnInclusion.values());

    public static final Set<Integer> FIRST_ROW_NUM = Collections.singleton(0);
    public static final Set<Long> ALL_ROW_IDS = null;
    public static final Set<Integer> ALL_ROW_NUMBERS = null;
    public static final Set<Long> ALL_COLUMN_IDS = null;
    public static final Integer NO_PAGE_SIZE_LIMIT = null;
    public static final Integer ALL_PAGE_NUMBERS = null;

    // the API can't xfer ownership so we have a list without it
    public static final AccessLevel[] ACCESS_LEVELS_WITHOUT_OWNER =
            {AccessLevel.VIEWER, AccessLevel.EDITOR, AccessLevel.EDITOR_SHARE, AccessLevel.ADMIN};

    private Constants() {
        // can't extend; can't create
    }
}
