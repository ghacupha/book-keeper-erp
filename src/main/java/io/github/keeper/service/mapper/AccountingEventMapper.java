package io.github.keeper.service.mapper;

import io.github.keeper.domain.AccountingEvent;
import io.github.keeper.domain.Dealer;
import io.github.keeper.domain.EventType;
import io.github.keeper.service.dto.AccountingEventDTO;
import io.github.keeper.service.dto.DealerDTO;
import io.github.keeper.service.dto.EventTypeDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link AccountingEvent} and its DTO {@link AccountingEventDTO}.
 */
@Mapper(componentModel = "spring")
public interface AccountingEventMapper extends EntityMapper<AccountingEventDTO, AccountingEvent> {
    @Mapping(target = "eventType", source = "eventType", qualifiedByName = "eventTypeName")
    @Mapping(target = "dealer", source = "dealer", qualifiedByName = "dealerName")
    AccountingEventDTO toDto(AccountingEvent s);

    @Named("eventTypeName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    EventTypeDTO toDtoEventTypeName(EventType eventType);

    @Named("dealerName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    DealerDTO toDtoDealerName(Dealer dealer);
}
