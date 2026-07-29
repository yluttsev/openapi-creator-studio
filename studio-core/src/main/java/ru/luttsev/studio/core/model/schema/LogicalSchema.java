package ru.luttsev.studio.core.model.schema;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public final class LogicalSchema implements Schema {

    private boolean value;
}
