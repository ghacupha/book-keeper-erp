package io.github.keeper.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BalanceSheetItemTypeMapperTest {

    private BalanceSheetItemTypeMapper balanceSheetItemTypeMapper;

    @BeforeEach
    public void setUp() {
        balanceSheetItemTypeMapper = new BalanceSheetItemTypeMapperImpl();
    }
}
