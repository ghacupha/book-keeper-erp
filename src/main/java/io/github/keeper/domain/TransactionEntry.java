package io.github.keeper.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.keeper.domain.enumeration.TransactionEntryTypes;
import java.io.Serializable;
import java.math.BigDecimal;
import javax.validation.constraints.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * A TransactionEntry.
 */
@Table("transaction_entry")
@org.springframework.data.elasticsearch.annotations.Document(indexName = "transactionentry")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class TransactionEntry implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column("id")
    private Long id;

    @Column("entry_amount")
    private BigDecimal entryAmount;

    @NotNull(message = "must not be null")
    @Column("transaction_entry_type")
    private TransactionEntryTypes transactionEntryType;

    @Column("description")
    private String description;

    @Column("was_proposed")
    private Boolean wasProposed;

    @Column("was_posted")
    private Boolean wasPosted;

    @Column("was_deleted")
    private Boolean wasDeleted;

    @Column("was_approved")
    private Boolean wasApproved;

    @Transient
    @JsonIgnoreProperties(value = { "parentAccount", "transactionAccountType", "transactionCurrency" }, allowSetters = true)
    private TransactionAccount transactionAccount;

    @Transient
    @JsonIgnoreProperties(value = { "transactionEntries" }, allowSetters = true)
    private AccountTransaction accountTransaction;

    @Column("transaction_account_id")
    private Long transactionAccountId;

    @Column("account_transaction_id")
    private Long accountTransactionId;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public TransactionEntry id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getEntryAmount() {
        return this.entryAmount;
    }

    public TransactionEntry entryAmount(BigDecimal entryAmount) {
        this.setEntryAmount(entryAmount);
        return this;
    }

    public void setEntryAmount(BigDecimal entryAmount) {
        this.entryAmount = entryAmount != null ? entryAmount.stripTrailingZeros() : null;
    }

    public TransactionEntryTypes getTransactionEntryType() {
        return this.transactionEntryType;
    }

    public TransactionEntry transactionEntryType(TransactionEntryTypes transactionEntryType) {
        this.setTransactionEntryType(transactionEntryType);
        return this;
    }

    public void setTransactionEntryType(TransactionEntryTypes transactionEntryType) {
        this.transactionEntryType = transactionEntryType;
    }

    public String getDescription() {
        return this.description;
    }

    public TransactionEntry description(String description) {
        this.setDescription(description);
        return this;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getWasProposed() {
        return this.wasProposed;
    }

    public TransactionEntry wasProposed(Boolean wasProposed) {
        this.setWasProposed(wasProposed);
        return this;
    }

    public void setWasProposed(Boolean wasProposed) {
        this.wasProposed = wasProposed;
    }

    public Boolean getWasPosted() {
        return this.wasPosted;
    }

    public TransactionEntry wasPosted(Boolean wasPosted) {
        this.setWasPosted(wasPosted);
        return this;
    }

    public void setWasPosted(Boolean wasPosted) {
        this.wasPosted = wasPosted;
    }

    public Boolean getWasDeleted() {
        return this.wasDeleted;
    }

    public TransactionEntry wasDeleted(Boolean wasDeleted) {
        this.setWasDeleted(wasDeleted);
        return this;
    }

    public void setWasDeleted(Boolean wasDeleted) {
        this.wasDeleted = wasDeleted;
    }

    public Boolean getWasApproved() {
        return this.wasApproved;
    }

    public TransactionEntry wasApproved(Boolean wasApproved) {
        this.setWasApproved(wasApproved);
        return this;
    }

    public void setWasApproved(Boolean wasApproved) {
        this.wasApproved = wasApproved;
    }

    public TransactionAccount getTransactionAccount() {
        return this.transactionAccount;
    }

    public void setTransactionAccount(TransactionAccount transactionAccount) {
        this.transactionAccount = transactionAccount;
        this.transactionAccountId = transactionAccount != null ? transactionAccount.getId() : null;
    }

    public TransactionEntry transactionAccount(TransactionAccount transactionAccount) {
        this.setTransactionAccount(transactionAccount);
        return this;
    }

    public AccountTransaction getAccountTransaction() {
        return this.accountTransaction;
    }

    public void setAccountTransaction(AccountTransaction accountTransaction) {
        this.accountTransaction = accountTransaction;
        this.accountTransactionId = accountTransaction != null ? accountTransaction.getId() : null;
    }

    public TransactionEntry accountTransaction(AccountTransaction accountTransaction) {
        this.setAccountTransaction(accountTransaction);
        return this;
    }

    public Long getTransactionAccountId() {
        return this.transactionAccountId;
    }

    public void setTransactionAccountId(Long transactionAccount) {
        this.transactionAccountId = transactionAccount;
    }

    public Long getAccountTransactionId() {
        return this.accountTransactionId;
    }

    public void setAccountTransactionId(Long accountTransaction) {
        this.accountTransactionId = accountTransaction;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TransactionEntry)) {
            return false;
        }
        return id != null && id.equals(((TransactionEntry) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "TransactionEntry{" +
            "id=" + getId() +
            ", entryAmount=" + getEntryAmount() +
            ", transactionEntryType='" + getTransactionEntryType() + "'" +
            ", description='" + getDescription() + "'" +
            ", wasProposed='" + getWasProposed() + "'" +
            ", wasPosted='" + getWasPosted() + "'" +
            ", wasDeleted='" + getWasDeleted() + "'" +
            ", wasApproved='" + getWasApproved() + "'" +
            "}";
    }
}
