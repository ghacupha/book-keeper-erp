package io.github.keeper.service.dto;

import io.github.keeper.domain.enumeration.TransactionEntryTypes;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;
import javax.validation.constraints.*;

/**
 * A DTO for the {@link io.github.keeper.domain.TransactionEntry} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class TransactionEntryDTO implements Serializable {

    private Long id;

    private BigDecimal entryAmount;

    @NotNull(message = "must not be null")
    private TransactionEntryTypes transactionEntryType;

    private String description;

    private Boolean wasProposed;

    private Boolean wasPosted;

    private Boolean wasDeleted;

    private Boolean wasApproved;

    private TransactionAccountDTO transactionAccount;

    private AccountTransactionDTO accountTransaction;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getEntryAmount() {
        return entryAmount;
    }

    public void setEntryAmount(BigDecimal entryAmount) {
        this.entryAmount = entryAmount;
    }

    public TransactionEntryTypes getTransactionEntryType() {
        return transactionEntryType;
    }

    public void setTransactionEntryType(TransactionEntryTypes transactionEntryType) {
        this.transactionEntryType = transactionEntryType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getWasProposed() {
        return wasProposed;
    }

    public void setWasProposed(Boolean wasProposed) {
        this.wasProposed = wasProposed;
    }

    public Boolean getWasPosted() {
        return wasPosted;
    }

    public void setWasPosted(Boolean wasPosted) {
        this.wasPosted = wasPosted;
    }

    public Boolean getWasDeleted() {
        return wasDeleted;
    }

    public void setWasDeleted(Boolean wasDeleted) {
        this.wasDeleted = wasDeleted;
    }

    public Boolean getWasApproved() {
        return wasApproved;
    }

    public void setWasApproved(Boolean wasApproved) {
        this.wasApproved = wasApproved;
    }

    public TransactionAccountDTO getTransactionAccount() {
        return transactionAccount;
    }

    public void setTransactionAccount(TransactionAccountDTO transactionAccount) {
        this.transactionAccount = transactionAccount;
    }

    public AccountTransactionDTO getAccountTransaction() {
        return accountTransaction;
    }

    public void setAccountTransaction(AccountTransactionDTO accountTransaction) {
        this.accountTransaction = accountTransaction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TransactionEntryDTO)) {
            return false;
        }

        TransactionEntryDTO transactionEntryDTO = (TransactionEntryDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, transactionEntryDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "TransactionEntryDTO{" +
            "id=" + getId() +
            ", entryAmount=" + getEntryAmount() +
            ", transactionEntryType='" + getTransactionEntryType() + "'" +
            ", description='" + getDescription() + "'" +
            ", wasProposed='" + getWasProposed() + "'" +
            ", wasPosted='" + getWasPosted() + "'" +
            ", wasDeleted='" + getWasDeleted() + "'" +
            ", wasApproved='" + getWasApproved() + "'" +
            ", transactionAccount=" + getTransactionAccount() +
            ", accountTransaction=" + getAccountTransaction() +
            "}";
    }
}
