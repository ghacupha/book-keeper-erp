package io.github.keeper.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BalanceSheetItemValueMapperTest {

    private BalanceSheetItemValueMapper balanceSheetItemValueMapper;

    @BeforeEach
    public void setUp() {
        balanceSheetItemValueMapper = new BalanceSheetItemValueMapperImpl();
    }
}
