package com.myopty.catalog.config;

import com.myopty.catalog.model.ExportFormat;
import java.util.Locale;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Accepts {@code ?format=pdf} as readily as {@code ?format=PDF}.
 *
 * <p>Spring's default enum conversion is {@code Enum.valueOf}, which is
 * case-sensitive, so a lowercase query value — the shape the API documents and
 * the frontend sends — would be refused. Boot registers a {@link Converter} bean
 * into the MVC conversion service, so this is the whole fix. A value that is
 * neither format still fails conversion and answers 400 in the module's
 * envelope, through {@code CatalogExceptionHandler}.
 */
@Component
public class ExportFormatConverter implements Converter<String, ExportFormat> {

    @Override
    public ExportFormat convert(String source) {
        return ExportFormat.valueOf(source.trim().toUpperCase(Locale.ROOT));
    }
}
