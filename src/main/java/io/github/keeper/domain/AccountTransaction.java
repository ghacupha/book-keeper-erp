package io.github.keeper.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import javax.validation.constraints.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * A AccountTransaction.
 */
@Table("account_transaction")
@org.springframework.data.elasticsearch.annotations.Document(indexName = "accounttransaction")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class AccountTransaction implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column("id")
    private Long id;

    @NotNull(message = "must not be null")
    @Column("transaction_date")
    private LocalDate transactionDate;

    @Column("description")
    private String description;

    @Column("reference_number")
    private String referenceNumber;

    @Column("was_proposed")
    private Boolean wasProposed;

    @Column("was_posted")
    private Boolean wasPosted;

    @Column("was_deleted")
    private Boolean wasDeleted;

    @Column("was_approved")
    private Boolean wasApproved;

    @Transient
    @JsonIgnoreProperties(value = { "transactionAccount", "accountTransaction" }, allowSetters = true)
    private Set<TransactionEntry> transactionEntries = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public AccountTransaction id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getTransactionDate() {
        return this.transactionDate;
    }

    public AccountTransaction transactionDate(LocalDate transactionDate) {
        this.setTransactionDate(transactionDate);
        return this;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getDescription() {
        return this.description;
    }

    public AccountTransaction description(String description) {
        this.setDescription(description);
        return this;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReferenceNumber() {
        return this.referenceNumber;
    }

    public AccountTransaction referenceNumber(String referenceNumber) {
        this.setReferenceNumber(referenceNumber);
        return this;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public Boolean getWasProposed() {
        return this.wasProposed;
    }

    public AccountTransaction wasProposed(Boolean wasProposed) {
        this.setWasProposed(wasProposed);
        return this;
    }

    public void setWasProposed(Boolean wasProposed) {
        this.wasProposed = wasProposed;
    }

    public Boolean getWasPosted() {
        return this.wasPosted;
    }

    public AccountTransaction wasPosted(Boolean wasPosted) {
        this.setWasPosted(wasPosted);
        return this;
    }

    public void setWasPosted(Boolean wasPosted) {
        this.wasPosted = wasPosted;
    }

    public Boolean getWasDeleted() {
        return this.wasDeleted;
    }

    public AccountTransaction wasDeleted(Boolean wasDeleted) {
        this.setWasDeleted(wasDeleted);
        return this;
    }

    public void setWasDeleted(Boolean wasDeleted) {
        this.wasDeleted = wasDeleted;
    }

    public Boolean getWasApproved() {
        return this.wasApproved;
    }

    public AccountTransaction wasApproved(Boolean wasApproved) {
        this.setWasApproved(wasApproved);
        return this;
    }

    public void setWasApproved(Boolean wasApproved) {
        this.wasApproved = wasApproved;
    }

    public Set<TransactionEntry> getTransactionEntries() {
        return this.transactionEntries;
    }

    public void setTransactionEntries(Set<TransactionEntry> transactionEntries) {
        if (this.transactionEntries != null) {
            this.transactionEntries.forEach(i -> i.setAccountTransaction(null));
        }
        if (transactionEntries != null) {
            transactionEntries.forEach(i -> i.setAccountTransaction(this));
        }
        this.transactionEntries = transactionEntries;
    }

    public AccountTransaction transactionEntries(Set<TransactionEntry> transactionEntries) {
        this.setTransactionEntries(transactionEntries);
        return this;
    }

    public AccountTransaction addTransactionEntry(TransactionEntry transactionEntry) {
        this.transactionEntries.add(transactionEntry);
        transactionEntry.setAccountTransaction(this);
        return this;
    }

    public AccountTransaction removeTransactionEntry(TransactionEntry transactionEntry) {
        this.transactionEntries.remove(transactionEntry);
        transactionEntry.setAccountTransaction(null);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AccountTransaction)) {
            return false;
        }
        return id != null && id.equals(((AccountTransaction) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AccountTransaction{" +
            "id=" + getId() +
            ", transactionDate='" + getTransactionDate() + "'" +
            ", description='" + getDescription() + "'" +
            ", referenceNumber='" + getReferenceNumber() + "'" +
            ", wasProposed='" + getWasProposed() + "'" +
            ", wasPosted='" + getWasPosted() + "'" +
            ", wasDeleted='" + getWasDeleted() + "'" +
            ", wasApproved='" + getWasApproved() + "'" +
            "}";
    }
}
