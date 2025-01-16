package com.ronreynolds.smartsheet;

import com.smartsheet.api.Smartsheet;
import com.smartsheet.api.SmartsheetBuilder;
import com.smartsheet.api.Trace;
import org.apache.commons.lang3.StringUtils;

/**
 *
 */
public class SmartsheetBuilders {
    public static Smartsheet getDefaultClient() {
        return getClientWithAuthAndUri(null, null);
    }

    public static Smartsheet getClientWithAuth(String authToken) {
        return getClientWithAuthAndUri(authToken, null);
    }

    public static Smartsheet getClientWithAuthAndUri(String authToken, String baseUri) {
        return getBuilderWithAuthAndUri(authToken, baseUri).build();
    }

    public static SmartsheetBuilder getBuilderWithAuthAndUri(String authToken, String baseUri) {
        SmartsheetBuilder builder = new SmartsheetBuilder();
        builder.setAccessToken(StringUtils.isNotBlank(authToken) ? authToken : Constants.DEFAULT_AUTH);
        if (StringUtils.isNotBlank(baseUri)) {
            builder.setBaseURI(baseUri);
        }
        return builder;
    }

    public static void enableDebug(Smartsheet api, boolean prettyOutput) {
        api.setTraces(Trace.Request, Trace.RequestBody, Trace.Response, Trace.ResponseBody);
        api.setTracePrettyPrint(prettyOutput);
    }
}
