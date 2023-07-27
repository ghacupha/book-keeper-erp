package io.github.keeper.service.mapper;

import io.github.keeper.domain.TransactionAccountType;
import io.github.keeper.service.dto.TransactionAccountTypeDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link TransactionAccountType} and its DTO {@link TransactionAccountTypeDTO}.
 */
@Mapper(componentModel = "spring")
public interface TransactionAccountTypeMapper extends EntityMapper<TransactionAccountTypeDTO, TransactionAccountType> {}
