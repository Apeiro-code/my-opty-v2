package com.myopty.catalog.model;

/**
 * The file formats the availability report can be exported as.
 *
 * <p>The enum carries the two facts the controller needs to send a file rather
 * than JSON — the MIME type the browser keys off and the extension a filename
 * ends in — so a new format is one constant, not a second conditional in the
 * controller. Values are the {@code ?format=} names and are matched
 * case-insensitively by Spring's conversion.
 */
public enum ExportFormat {
    PDF("application/pdf", "pdf"),
    XLSX("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "xlsx");

    private final String contentType;
    private final String extension;

    ExportFormat(String contentType, String extension) {
        this.contentType = contentType;
        this.extension = extension;
    }

    public String contentType() {
        return contentType;
    }

    public String extension() {
        return extension;
    }
}
