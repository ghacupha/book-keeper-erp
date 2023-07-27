package io.github.keeper.service.mapper;

import io.github.keeper.domain.TransactionCurrency;
import io.github.keeper.service.dto.TransactionCurrencyDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link TransactionCurrency} and its DTO {@link TransactionCurrencyDTO}.
 */
@Mapper(componentModel = "spring")
public interface TransactionCurrencyMapper extends EntityMapper<TransactionCurrencyDTO, TransactionCurrency> {}
