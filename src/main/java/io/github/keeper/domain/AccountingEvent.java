package io.github.keeper.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.time.LocalDate;
import javax.validation.constraints.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * A AccountingEvent.
 */
@Table("accounting_event")
@org.springframework.data.elasticsearch.annotations.Document(indexName = "accountingevent")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class AccountingEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column("id")
    private Long id;

    @NotNull(message = "must not be null")
    @Column("event_date")
    private LocalDate eventDate;

    @Transient
    private EventType eventType;

    @Transient
    @JsonIgnoreProperties(value = { "dealerType" }, allowSetters = true)
    private Dealer dealer;

    @Column("event_type_id")
    private Long eventTypeId;

    @Column("dealer_id")
    private Long dealerId;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public AccountingEvent id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getEventDate() {
        return this.eventDate;
    }

    public AccountingEvent eventDate(LocalDate eventDate) {
        this.setEventDate(eventDate);
        return this;
    }

    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
    }

    public EventType getEventType() {
        return this.eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
        this.eventTypeId = eventType != null ? eventType.getId() : null;
    }

    public AccountingEvent eventType(EventType eventType) {
        this.setEventType(eventType);
        return this;
    }

    public Dealer getDealer() {
        return this.dealer;
    }

    public void setDealer(Dealer dealer) {
        this.dealer = dealer;
        this.dealerId = dealer != null ? dealer.getId() : null;
    }

    public AccountingEvent dealer(Dealer dealer) {
        this.setDealer(dealer);
        return this;
    }

    public Long getEventTypeId() {
        return this.eventTypeId;
    }

    public void setEventTypeId(Long eventType) {
        this.eventTypeId = eventType;
    }

    public Long getDealerId() {
        return this.dealerId;
    }

    public void setDealerId(Long dealer) {
        this.dealerId = dealer;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AccountingEvent)) {
            return false;
        }
        return id != null && id.equals(((AccountingEvent) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AccountingEvent{" +
            "id=" + getId() +
            ", eventDate='" + getEventDate() + "'" +
            "}";
    }
}
