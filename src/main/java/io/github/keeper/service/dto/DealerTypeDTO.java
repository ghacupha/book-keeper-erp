package io.github.keeper.service.dto;

import java.io.Serializable;
import java.util.Objects;
import javax.validation.constraints.*;

/**
 * A DTO for the {@link io.github.keeper.domain.DealerType} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class DealerTypeDTO implements Serializable {

    private Long id;

    @NotNull(message = "must not be null")
    private String name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DealerTypeDTO)) {
            return false;
        }

        DealerTypeDTO dealerTypeDTO = (DealerTypeDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, dealerTypeDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "DealerTypeDTO{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            "}";
    }
}
