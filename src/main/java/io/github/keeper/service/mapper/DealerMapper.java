package io.github.keeper.service.mapper;

import io.github.keeper.domain.Dealer;
import io.github.keeper.domain.DealerType;
import io.github.keeper.service.dto.DealerDTO;
import io.github.keeper.service.dto.DealerTypeDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Dealer} and its DTO {@link DealerDTO}.
 */
@Mapper(componentModel = "spring")
public interface DealerMapper extends EntityMapper<DealerDTO, Dealer> {
    @Mapping(target = "dealerType", source = "dealerType", qualifiedByName = "dealerTypeName")
    DealerDTO toDto(Dealer s);

    @Named("dealerTypeName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    DealerTypeDTO toDtoDealerTypeName(DealerType dealerType);
}
