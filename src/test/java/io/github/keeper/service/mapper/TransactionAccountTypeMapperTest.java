package io.github.keeper.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TransactionAccountTypeMapperTest {

    private TransactionAccountTypeMapper transactionAccountTypeMapper;

    @BeforeEach
    public void setUp() {
        transactionAccountTypeMapper = new TransactionAccountTypeMapperImpl();
    }
}
