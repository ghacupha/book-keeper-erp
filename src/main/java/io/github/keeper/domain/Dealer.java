package io.github.keeper.domain;

import java.io.Serializable;
import javax.validation.constraints.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * A Dealer.
 */
@Table("dealer")
@org.springframework.data.elasticsearch.annotations.Document(indexName = "dealer")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Dealer implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column("id")
    private Long id;

    @NotNull(message = "must not be null")
    @Column("name")
    private String name;

    @Transient
    private DealerType dealerType;

    @Column("dealer_type_id")
    private Long dealerTypeId;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Dealer id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public Dealer name(String name) {
        this.setName(name);
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DealerType getDealerType() {
        return this.dealerType;
    }

    public void setDealerType(DealerType dealerType) {
        this.dealerType = dealerType;
        this.dealerTypeId = dealerType != null ? dealerType.getId() : null;
    }

    public Dealer dealerType(DealerType dealerType) {
        this.setDealerType(dealerType);
        return this;
    }

    public Long getDealerTypeId() {
        return this.dealerTypeId;
    }

    public void setDealerTypeId(Long dealerType) {
        this.dealerTypeId = dealerType;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Dealer)) {
            return false;
        }
        return id != null && id.equals(((Dealer) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Dealer{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            "}";
    }
}
