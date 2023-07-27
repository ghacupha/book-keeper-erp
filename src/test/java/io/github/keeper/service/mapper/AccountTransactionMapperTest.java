package io.github.keeper.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AccountTransactionMapperTest {

    private AccountTransactionMapper accountTransactionMapper;

    @BeforeEach
    public void setUp() {
        accountTransactionMapper = new AccountTransactionMapperImpl();
    }
}
