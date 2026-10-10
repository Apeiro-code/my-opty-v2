package com.myopty.order.service;

import java.io.InputStream;

/**
 * Where prescription document bytes live. The database stores only the key, so
 * the bytes have to go somewhere else — this is that boundary.
 *
 * <p>An interface with one filesystem implementation today, because the
 * repository's `.env` promises MinIO ({@code MINIO_*}) and the shop will run on
 * object storage, but a network store the test suite cannot start would make every
 * test of the upload path a test of Docker. The filesystem store makes disk the
 * stand-in for S3 and lets the tests run without it; a MinIO implementation
 * replaces the bean without the service noticing.
 *
 * <p>The key is chosen by the caller, so it is the caller's job to make it safe
 * and unique. Implementations must still refuse a key that escapes their root.
 */
public interface ObjectStore {

    /**
     * Stores the bytes under {@code key}. The stream is read to the end; the
     * caller keeps ownership of it and the implementation must not close it.
     */
    void put(String key, InputStream content, long contentLength, String contentType);

    /**
     * Removes the object under {@code key}. Absent is success: a compensating
     * delete after a failed insert must not itself fail the request.
     */
    void delete(String key);
}
