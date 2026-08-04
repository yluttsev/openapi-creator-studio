package ru.luttsev.studio.web.exporting;

import org.mapstruct.Mapper;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.MappingConstants;
import ru.luttsev.studio.application.exporting.DocumentExport;
import ru.luttsev.studio.core.model.OpenApiVersion;
import ru.luttsev.studio.generated.model.ExportResponse;
import ru.luttsev.studio.web.document.DiagnosticMapper;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        uses = DiagnosticMapper.class)
public interface ExportMapper {

    ExportResponse map(DocumentExport documentExport);

    default ru.luttsev.studio.openapi.format.OpenApiFormat map(
            ru.luttsev.studio.generated.model.OpenApiFormat format) {
        return ru.luttsev.studio.openapi.format.OpenApiFormat.valueOf(
                format.name());
    }

    default ru.luttsev.studio.generated.model.OpenApiFormat map(
            ru.luttsev.studio.openapi.format.OpenApiFormat format) {
        return ru.luttsev.studio.generated.model.OpenApiFormat.valueOf(
                format.name());
    }

    default OpenApiVersion map(String version) {
        return version == null ? null : new OpenApiVersion(version);
    }

    default String map(OpenApiVersion version) {
        return version.value();
    }
}
