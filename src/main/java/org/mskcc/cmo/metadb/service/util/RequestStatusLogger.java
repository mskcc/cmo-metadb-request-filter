package org.mskcc.cmo.metadb.service.util;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.apache.commons.lang3.StringUtils;
import org.mskcc.cmo.common.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Class for logging request statuses to the provided
 * Request Handling filepath.
 * @author ochoaa
 */
@Component
public class RequestStatusLogger {
    @Value("${metadb_req_filter.request_logger_filepath}")
    private String requestLoggerFilepath;

    @Autowired
    private FileUtil fileUtil;

    private File requestStatusLoggerFile;

    @Autowired
    private static final String[] REQUEST_LOGGER_FILE_HEADER = new String[]{"DATE", "STATUS", "MESSAGE"};

    /**
     * Request StatusType descriptions:
     * - EMPTY_REQUEST: a request json that came in empty
     * - REQUEST_WITH_MISSING_SAMPLES: a request that came in with no sample metadata
     * - CMO_REQUEST_MISSING_REQ_FIELDS: a CMO request with sample metadata
     *        that is missing required fields (cmoPatientId, baitSet)
     * - REQUEST_PARSING_ERROR: json parsing exception thrown
     * - CMO_REQUEST_FILTER_SKIPPED_REQUEST: applies if metadb server is running
     *        with the cmoRequestFilter enabled and a non-cmo request is encountered
     * - CMO_REQUEST_FAILED_SANITY_CHECK: request failed cmo-specific sanity check on
     *        required fields for generating cmo labels
     */
    public enum StatusType {
        EMPTY_REQUEST,
        REQUEST_WITH_MISSING_SAMPLES,
        CMO_REQUEST_MISSING_REQ_FIELDS,
        REQUEST_PARSING_ERROR,
        CMO_REQUEST_FILTER_SKIPPED_REQUEST,
        CMO_REQUEST_FAILED_SANITY_CHECK
    }

    /**
     * Writes request contents and status to the request status logger file.
     * @param message
     * @param status
     * @throws IOException
     */
    public void logRequestStatus(String message, StatusType status) throws IOException {
        if (requestStatusLoggerFile ==  null) {
            this.requestStatusLoggerFile = fileUtil.getOrCreateFileWithHeader(requestLoggerFilepath,
                    StringUtils.join(REQUEST_LOGGER_FILE_HEADER, "\t") + "\n");
        }
        String currentDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        StringBuilder builder = new StringBuilder();
        builder.append(currentDate)
                .append("\t")
                .append(status.toString())
                .append("\t")
                .append(message)
                .append("\n");
        fileUtil.writeToFile(requestStatusLoggerFile, builder.toString());
    }
}
