package io.github.keeper.service.mapper;

import io.github.keeper.domain.AccountTransaction;
import io.github.keeper.domain.TransactionAccount;
import io.github.keeper.domain.TransactionEntry;
import io.github.keeper.service.dto.AccountTransactionDTO;
import io.github.keeper.service.dto.TransactionAccountDTO;
import io.github.keeper.service.dto.TransactionEntryDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link TransactionEntry} and its DTO {@link TransactionEntryDTO}.
 */
@Mapper(componentModel = "spring")
public interface TransactionEntryMapper extends EntityMapper<TransactionEntryDTO, TransactionEntry> {
    @Mapping(target = "transactionAccount", source = "transactionAccount", qualifiedByName = "transactionAccountAccountName")
    @Mapping(target = "accountTransaction", source = "accountTransaction", qualifiedByName = "accountTransactionReferenceNumber")
    TransactionEntryDTO toDto(TransactionEntry s);

    @Named("transactionAccountAccountName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "accountName", source = "accountName")
    TransactionAccountDTO toDtoTransactionAccountAccountName(TransactionAccount transactionAccount);

    @Named("accountTransactionReferenceNumber")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "referenceNumber", source = "referenceNumber")
    AccountTransactionDTO toDtoAccountTransactionReferenceNumber(AccountTransaction accountTransaction);
}
