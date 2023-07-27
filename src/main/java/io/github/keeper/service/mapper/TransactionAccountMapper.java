package io.github.keeper.service.mapper;

import io.github.keeper.domain.TransactionAccount;
import io.github.keeper.domain.TransactionAccountType;
import io.github.keeper.domain.TransactionCurrency;
import io.github.keeper.service.dto.TransactionAccountDTO;
import io.github.keeper.service.dto.TransactionAccountTypeDTO;
import io.github.keeper.service.dto.TransactionCurrencyDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link TransactionAccount} and its DTO {@link TransactionAccountDTO}.
 */
@Mapper(componentModel = "spring")
public interface TransactionAccountMapper extends EntityMapper<TransactionAccountDTO, TransactionAccount> {
    @Mapping(target = "parentAccount", source = "parentAccount", qualifiedByName = "transactionAccountAccountName")
    @Mapping(target = "transactionAccountType", source = "transactionAccountType", qualifiedByName = "transactionAccountTypeName")
    @Mapping(target = "transactionCurrency", source = "transactionCurrency", qualifiedByName = "transactionCurrencyCode")
    TransactionAccountDTO toDto(TransactionAccount s);

    @Named("transactionAccountAccountName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "accountName", source = "accountName")
    TransactionAccountDTO toDtoTransactionAccountAccountName(TransactionAccount transactionAccount);

    @Named("transactionAccountTypeName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    TransactionAccountTypeDTO toDtoTransactionAccountTypeName(TransactionAccountType transactionAccountType);

    @Named("transactionCurrencyCode")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "code", source = "code")
    TransactionCurrencyDTO toDtoTransactionCurrencyCode(TransactionCurrency transactionCurrency);
}
