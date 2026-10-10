package com.myopty.order.service;

import com.myopty.order.exception.ObjectStoreException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

/**
 * An {@link ObjectStore} that writes under a configured directory on the local
 * filesystem. The development stand-in for the object store; see
 * {@link ObjectStore} for why the boundary exists.
 *
 * <p>Keys are UUID-based (see the service) so a collision is not expected, but the
 * traversal guard is not optional: {@code put} and {@code delete} both resolve the
 * key against the root and refuse anything that lands outside it, so a future
 * caller passing user-influenced text cannot read or write above the store.
 */
public class FileSystemObjectStore implements ObjectStore {

    private final Path root;

    public FileSystemObjectStore(Path root) {
        this.root = root.toAbsolutePath().normalize();
    }

    @Override
    public void put(String key, InputStream content, long contentLength, String contentType) {
        Path target = resolve(key);
        try {
            Files.createDirectories(target.getParent());
            // No REPLACE_EXISTING: a key that already exists is a bug (a UUID
            // collided, or a caller reused a key), and overwriting would hide it.
            Files.copy(content, target, StandardCopyOption.COPY_ATTRIBUTES);
        } catch (IOException exception) {
            throw new ObjectStoreException("Could not store the uploaded document.", exception);
        }
    }

    @Override
    public void delete(String key) {
        try {
            Files.deleteIfExists(resolve(key));
        } catch (IOException exception) {
            throw new ObjectStoreException("Could not remove a stored document.", exception);
        }
    }

    private Path resolve(String key) {
        Objects.requireNonNull(key, "key");
        Path resolved = root.resolve(key).normalize();
        if (!resolved.startsWith(root) || resolved.equals(root)) {
            throw new ObjectStoreException("Refusing an object key outside the store.", null);
        }
        return resolved;
    }
}
