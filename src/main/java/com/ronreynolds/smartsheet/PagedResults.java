package com.ronreynolds.smartsheet;

import com.smartsheet.api.models.PagedResult;

/**
 *
 */
public class PagedResults {
    public static <T> String toString(PagedResult<T> result) {
        String buf = "{pageNum:" + result.getPageNumber() + ", pageSize:" + result.getPageSize() +
                ", totalCount:" + result.getTotalCount() + ", totalPages:" + result.getTotalPages() +
                ", data:" + result.getData() + "}";
        return buf;
    }
}
