package io.github.keeper.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.keeper.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class DealerDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(DealerDTO.class);
        DealerDTO dealerDTO1 = new DealerDTO();
        dealerDTO1.setId(1L);
        DealerDTO dealerDTO2 = new DealerDTO();
        assertThat(dealerDTO1).isNotEqualTo(dealerDTO2);
        dealerDTO2.setId(dealerDTO1.getId());
        assertThat(dealerDTO1).isEqualTo(dealerDTO2);
        dealerDTO2.setId(2L);
        assertThat(dealerDTO1).isNotEqualTo(dealerDTO2);
        dealerDTO1.setId(null);
        assertThat(dealerDTO1).isNotEqualTo(dealerDTO2);
    }
}
