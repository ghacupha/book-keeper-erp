package io.github.keeper.service.mapper;

import io.github.keeper.domain.DealerType;
import io.github.keeper.service.dto.DealerTypeDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link DealerType} and its DTO {@link DealerTypeDTO}.
 */
@Mapper(componentModel = "spring")
public interface DealerTypeMapper extends EntityMapper<DealerTypeDTO, DealerType> {}
