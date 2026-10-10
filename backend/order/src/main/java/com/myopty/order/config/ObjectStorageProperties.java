package com.myopty.order.config;

import java.nio.file.Path;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

/**
 * Where uploaded prescription documents are stored and what may be uploaded.
 *
 * <p>Defaults live here rather than requiring new {@code .env} keys: the
 * object-store contract the repository settled on is MinIO's ({@code MINIO_*}),
 * and until a MinIO implementation exists there is nothing for those keys to
 * configure. The filesystem root therefore defaults under {@code target/}, is
 * git-ignored, and needs no setup to boot; the allowlist and cap are policy, not
 * environment.
 */
@ConfigurationProperties(prefix = "myopty.object-storage")
public class ObjectStorageProperties {

    /** Directory the filesystem store writes under. */
    private Path root = Path.of("target/prescription-documents");

    /** Largest document accepted. Mirrors {@code spring.servlet.multipart} in application.yml. */
    private DataSize maxFileSize = DataSize.ofMegabytes(10);

    /** Content types a scan or photo may arrive as. */
    private List<String> allowedContentTypes = List.of("application/pdf", "image/jpeg", "image/png");

    public Path getRoot() {
        return root;
    }

    public void setRoot(Path root) {
        this.root = root;
    }

    public DataSize getMaxFileSize() {
        return maxFileSize;
    }

    public void setMaxFileSize(DataSize maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    public List<String> getAllowedContentTypes() {
        return allowedContentTypes;
    }

    public void setAllowedContentTypes(List<String> allowedContentTypes) {
        this.allowedContentTypes = allowedContentTypes;
    }
}
