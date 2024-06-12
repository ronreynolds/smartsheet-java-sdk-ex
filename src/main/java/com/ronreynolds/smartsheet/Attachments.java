package com.ronreynolds.smartsheet;

import com.google.common.base.Preconditions;
import com.google.common.io.ByteStreams;
import com.ronreynolds.jackson.ObjectMappers;
import com.smartsheet.api.Smartsheet;
import com.smartsheet.api.SmartsheetException;
import com.smartsheet.api.models.Attachment;
import com.smartsheet.api.models.PagedResult;
import com.smartsheet.api.models.Sheet;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 */
@Slf4j
public class Attachments {
    /**
     * builds a map of all attachments on the specified sheet-id
     */
    public static <K> Map<K, Attachment> getAttachmentMap(Smartsheet client, long sheetId, Function<Attachment, K> keyMapper)
            throws SmartsheetException {
        PagedResult<Attachment> attachments = client.sheetResources().attachmentResources().listAttachments(sheetId, Constants.ALL_PAGES);

        return buildMap(attachments.getData(), keyMapper);
    }

    /**
     * builds a map of all attachments on the specified sheet (uses sheet-attachments if available; fetches them if not)
     */
    public static <K> Map<K, Attachment> getAttachmentMap(Smartsheet client, Sheet sheet, Function<Attachment, K> keyMapper)
            throws SmartsheetException {
        List<Attachment> attachments = sheet.getAttachments();
        if (attachments == null) {
            attachments = client.sheetResources().attachmentResources().listAttachments(sheet.getId(), Constants.ALL_PAGES).getData();
        }
        return buildMap(attachments, keyMapper);
    }

    /**
     * converts a list of Attachment objects into a map keyed by whatever the keyMapper returns as the key
     */
    public static <K> Map<K, Attachment> buildMap(Collection<Attachment> attachments, Function<Attachment, K> keyMapper) {
        return attachments.stream()
                .collect(Collectors.toMap(
                        keyMapper,          // the key is the name of the attachment
                        (a) -> a,             // the value is the attachment
                        (a1, a2) -> {          // if there are duplicates we want to know
                            log.warn("duplicate attachments found - {} & {}", a1.getName(), a2.getName());
                            return a1;
                        }));

    }

    public static Attachment addSheetAttachment(Smartsheet client, long sheetId, String name, File source)
            throws FileNotFoundException, SmartsheetException {
        return client.sheetResources().attachmentResources().attachFile(sheetId, source, name);
    }

    public static Attachment addSheetRowAttachment(Smartsheet client, long sheetId, long rowId, String name, File source)
            throws FileNotFoundException, SmartsheetException {
        return client.sheetResources().rowResources().attachmentResources().attachFile(sheetId, rowId, source, name);
    }

    /**
     * downloads an attachment to a local file
     *
     * @param dir        the directory into which to place the file (name based on name of attachment)
     * @param attachment the attachment to download
     * @param fileCb     callback to receive File created (even if download fails)
     * @return the number of bytes downloaded
     * @throws IOException if anything goes wrong
     */
    public static long downloadToDir(File dir, Attachment attachment, Consumer<File> fileCb) throws IOException {
        File file = new File(dir, attachment.getName());
        fileCb.accept(file);
        try (FileOutputStream fos = new FileOutputStream(file);
             InputStream is = new URL(Preconditions.checkNotNull(attachment.getUrl())).openStream()) {
            return ByteStreams.copy(is, fos);
        }
    }

    public static <K> String toString(Map<K, Attachment> attachmentMap) {
        StringBuilder buf = new StringBuilder(attachmentMap.size() * 100);
        for (Map.Entry<K, Attachment> entry : attachmentMap.entrySet()) {
            buf.append(entry.getKey()).append(':').append(toString(entry.getValue())).append('\n');
        }
        return buf.toString();
    }

    public static String toString(Attachment attachment) {
        return ObjectMappers.toCompactJson(attachment); // for now
    }
}

