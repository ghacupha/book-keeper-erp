package io.github.keeper.service.mapper;

import io.github.keeper.domain.EventType;
import io.github.keeper.service.dto.EventTypeDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link EventType} and its DTO {@link EventTypeDTO}.
 */
@Mapper(componentModel = "spring")
public interface EventTypeMapper extends EntityMapper<EventTypeDTO, EventType> {}
