package io.github.keeper.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AccountingEventMapperTest {

    private AccountingEventMapper accountingEventMapper;

    @BeforeEach
    public void setUp() {
        accountingEventMapper = new AccountingEventMapperImpl();
    }
}
