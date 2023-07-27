package io.github.keeper.service.dto;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;
import javax.validation.constraints.*;

/**
 * A DTO for the {@link io.github.keeper.domain.AccountingEvent} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class AccountingEventDTO implements Serializable {

    private Long id;

    @NotNull(message = "must not be null")
    private LocalDate eventDate;

    private EventTypeDTO eventType;

    private DealerDTO dealer;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
    }

    public EventTypeDTO getEventType() {
        return eventType;
    }

    public void setEventType(EventTypeDTO eventType) {
        this.eventType = eventType;
    }

    public DealerDTO getDealer() {
        return dealer;
    }

    public void setDealer(DealerDTO dealer) {
        this.dealer = dealer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AccountingEventDTO)) {
            return false;
        }

        AccountingEventDTO accountingEventDTO = (AccountingEventDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, accountingEventDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AccountingEventDTO{" +
            "id=" + getId() +
            ", eventDate='" + getEventDate() + "'" +
            ", eventType=" + getEventType() +
            ", dealer=" + getDealer() +
            "}";
    }
}
