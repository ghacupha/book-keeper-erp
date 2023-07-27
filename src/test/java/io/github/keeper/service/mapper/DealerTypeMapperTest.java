package io.github.keeper.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DealerTypeMapperTest {

    private DealerTypeMapper dealerTypeMapper;

    @BeforeEach
    public void setUp() {
        dealerTypeMapper = new DealerTypeMapperImpl();
    }
}
