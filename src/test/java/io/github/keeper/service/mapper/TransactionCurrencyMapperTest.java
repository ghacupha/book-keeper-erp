package io.github.keeper.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TransactionCurrencyMapperTest {

    private TransactionCurrencyMapper transactionCurrencyMapper;

    @BeforeEach
    public void setUp() {
        transactionCurrencyMapper = new TransactionCurrencyMapperImpl();
    }
}
