package io.github.keeper.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import javax.validation.constraints.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * A BalanceSheetItemType.
 */
@Table("balance_sheet_item_type")
@org.springframework.data.elasticsearch.annotations.Document(indexName = "balancesheetitemtype")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class BalanceSheetItemType implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column("id")
    private Long id;

    @NotNull(message = "must not be null")
    @Column("item_sequence")
    private Integer itemSequence;

    @NotNull(message = "must not be null")
    @Column("item_number")
    private String itemNumber;

    @Column("short_description")
    private String shortDescription;

    @Transient
    private TransactionAccount transactionAccount;

    @Transient
    @JsonIgnoreProperties(value = { "transactionAccount", "parentItem" }, allowSetters = true)
    private BalanceSheetItemType parentItem;

    @Column("transaction_account_id")
    private Long transactionAccountId;

    @Column("parent_item_id")
    private Long parentItemId;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public BalanceSheetItemType id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getItemSequence() {
        return this.itemSequence;
    }

    public BalanceSheetItemType itemSequence(Integer itemSequence) {
        this.setItemSequence(itemSequence);
        return this;
    }

    public void setItemSequence(Integer itemSequence) {
        this.itemSequence = itemSequence;
    }

    public String getItemNumber() {
        return this.itemNumber;
    }

    public BalanceSheetItemType itemNumber(String itemNumber) {
        this.setItemNumber(itemNumber);
        return this;
    }

    public void setItemNumber(String itemNumber) {
        this.itemNumber = itemNumber;
    }

    public String getShortDescription() {
        return this.shortDescription;
    }

    public BalanceSheetItemType shortDescription(String shortDescription) {
        this.setShortDescription(shortDescription);
        return this;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public TransactionAccount getTransactionAccount() {
        return this.transactionAccount;
    }

    public void setTransactionAccount(TransactionAccount transactionAccount) {
        this.transactionAccount = transactionAccount;
        this.transactionAccountId = transactionAccount != null ? transactionAccount.getId() : null;
    }

    public BalanceSheetItemType transactionAccount(TransactionAccount transactionAccount) {
        this.setTransactionAccount(transactionAccount);
        return this;
    }

    public BalanceSheetItemType getParentItem() {
        return this.parentItem;
    }

    public void setParentItem(BalanceSheetItemType balanceSheetItemType) {
        this.parentItem = balanceSheetItemType;
        this.parentItemId = balanceSheetItemType != null ? balanceSheetItemType.getId() : null;
    }

    public BalanceSheetItemType parentItem(BalanceSheetItemType balanceSheetItemType) {
        this.setParentItem(balanceSheetItemType);
        return this;
    }

    public Long getTransactionAccountId() {
        return this.transactionAccountId;
    }

    public void setTransactionAccountId(Long transactionAccount) {
        this.transactionAccountId = transactionAccount;
    }

    public Long getParentItemId() {
        return this.parentItemId;
    }

    public void setParentItemId(Long balanceSheetItemType) {
        this.parentItemId = balanceSheetItemType;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BalanceSheetItemType)) {
            return false;
        }
        return id != null && id.equals(((BalanceSheetItemType) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "BalanceSheetItemType{" +
            "id=" + getId() +
            ", itemSequence=" + getItemSequence() +
            ", itemNumber='" + getItemNumber() + "'" +
            ", shortDescription='" + getShortDescription() + "'" +
            "}";
    }
}
