package io.github.keeper.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import javax.validation.constraints.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * A BalanceSheetItemValue.
 */
@Table("balance_sheet_item_value")
@org.springframework.data.elasticsearch.annotations.Document(indexName = "balancesheetitemvalue")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class BalanceSheetItemValue implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column("id")
    private Long id;

    @Column("short_description")
    private String shortDescription;

    @NotNull(message = "must not be null")
    @Column("effective_date")
    private LocalDate effectiveDate;

    @NotNull(message = "must not be null")
    @Column("item_amount")
    private BigDecimal itemAmount;

    @Transient
    @JsonIgnoreProperties(value = { "transactionAccount", "parentItem" }, allowSetters = true)
    private BalanceSheetItemType itemType;

    @Column("item_type_id")
    private Long itemTypeId;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public BalanceSheetItemValue id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getShortDescription() {
        return this.shortDescription;
    }

    public BalanceSheetItemValue shortDescription(String shortDescription) {
        this.setShortDescription(shortDescription);
        return this;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public LocalDate getEffectiveDate() {
        return this.effectiveDate;
    }

    public BalanceSheetItemValue effectiveDate(LocalDate effectiveDate) {
        this.setEffectiveDate(effectiveDate);
        return this;
    }

    public void setEffectiveDate(LocalDate effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public BigDecimal getItemAmount() {
        return this.itemAmount;
    }

    public BalanceSheetItemValue itemAmount(BigDecimal itemAmount) {
        this.setItemAmount(itemAmount);
        return this;
    }

    public void setItemAmount(BigDecimal itemAmount) {
        this.itemAmount = itemAmount != null ? itemAmount.stripTrailingZeros() : null;
    }

    public BalanceSheetItemType getItemType() {
        return this.itemType;
    }

    public void setItemType(BalanceSheetItemType balanceSheetItemType) {
        this.itemType = balanceSheetItemType;
        this.itemTypeId = balanceSheetItemType != null ? balanceSheetItemType.getId() : null;
    }

    public BalanceSheetItemValue itemType(BalanceSheetItemType balanceSheetItemType) {
        this.setItemType(balanceSheetItemType);
        return this;
    }

    public Long getItemTypeId() {
        return this.itemTypeId;
    }

    public void setItemTypeId(Long balanceSheetItemType) {
        this.itemTypeId = balanceSheetItemType;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BalanceSheetItemValue)) {
            return false;
        }
        return id != null && id.equals(((BalanceSheetItemValue) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "BalanceSheetItemValue{" +
            "id=" + getId() +
            ", shortDescription='" + getShortDescription() + "'" +
            ", effectiveDate='" + getEffectiveDate() + "'" +
            ", itemAmount=" + getItemAmount() +
            "}";
    }
}
