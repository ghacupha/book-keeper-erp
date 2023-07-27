package io.github.keeper.service.mapper;

import io.github.keeper.domain.BalanceSheetItemType;
import io.github.keeper.domain.TransactionAccount;
import io.github.keeper.service.dto.BalanceSheetItemTypeDTO;
import io.github.keeper.service.dto.TransactionAccountDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link BalanceSheetItemType} and its DTO {@link BalanceSheetItemTypeDTO}.
 */
@Mapper(componentModel = "spring")
public interface BalanceSheetItemTypeMapper extends EntityMapper<BalanceSheetItemTypeDTO, BalanceSheetItemType> {
    @Mapping(target = "transactionAccount", source = "transactionAccount", qualifiedByName = "transactionAccountAccountName")
    @Mapping(target = "parentItem", source = "parentItem", qualifiedByName = "balanceSheetItemTypeItemNumber")
    BalanceSheetItemTypeDTO toDto(BalanceSheetItemType s);

    @Named("transactionAccountAccountName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "accountName", source = "accountName")
    TransactionAccountDTO toDtoTransactionAccountAccountName(TransactionAccount transactionAccount);

    @Named("balanceSheetItemTypeItemNumber")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "itemNumber", source = "itemNumber")
    BalanceSheetItemTypeDTO toDtoBalanceSheetItemTypeItemNumber(BalanceSheetItemType balanceSheetItemType);
}
