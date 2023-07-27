package io.github.keeper.service.mapper;

import io.github.keeper.domain.AccountTransaction;
import io.github.keeper.service.dto.AccountTransactionDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link AccountTransaction} and its DTO {@link AccountTransactionDTO}.
 */
@Mapper(componentModel = "spring")
public interface AccountTransactionMapper extends EntityMapper<AccountTransactionDTO, AccountTransaction> {}
