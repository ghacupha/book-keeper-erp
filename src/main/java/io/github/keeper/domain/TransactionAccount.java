package io.github.keeper.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.math.BigDecimal;
import javax.validation.constraints.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * A TransactionAccount.
 */
@Table("transaction_account")
@org.springframework.data.elasticsearch.annotations.Document(indexName = "transactionaccount")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class TransactionAccount implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column("id")
    private Long id;

    @NotNull(message = "must not be null")
    @Column("account_name")
    private String accountName;

    @Column("account_number")
    private String accountNumber;

    @Column("opening_balance")
    private BigDecimal openingBalance;

    @Transient
    @JsonIgnoreProperties(value = { "parentAccount", "transactionAccountType", "transactionCurrency" }, allowSetters = true)
    private TransactionAccount parentAccount;

    @Transient
    private TransactionAccountType transactionAccountType;

    @Transient
    private TransactionCurrency transactionCurrency;

    @Column("parent_account_id")
    private Long parentAccountId;

    @Column("transaction_account_type_id")
    private Long transactionAccountTypeId;

    @Column("transaction_currency_id")
    private Long transactionCurrencyId;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public TransactionAccount id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAccountName() {
        return this.accountName;
    }

    public TransactionAccount accountName(String accountName) {
        this.setAccountName(accountName);
        return this;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getAccountNumber() {
        return this.accountNumber;
    }

    public TransactionAccount accountNumber(String accountNumber) {
        this.setAccountNumber(accountNumber);
        return this;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public BigDecimal getOpeningBalance() {
        return this.openingBalance;
    }

    public TransactionAccount openingBalance(BigDecimal openingBalance) {
        this.setOpeningBalance(openingBalance);
        return this;
    }

    public void setOpeningBalance(BigDecimal openingBalance) {
        this.openingBalance = openingBalance != null ? openingBalance.stripTrailingZeros() : null;
    }

    public TransactionAccount getParentAccount() {
        return this.parentAccount;
    }

    public void setParentAccount(TransactionAccount transactionAccount) {
        this.parentAccount = transactionAccount;
        this.parentAccountId = transactionAccount != null ? transactionAccount.getId() : null;
    }

    public TransactionAccount parentAccount(TransactionAccount transactionAccount) {
        this.setParentAccount(transactionAccount);
        return this;
    }

    public TransactionAccountType getTransactionAccountType() {
        return this.transactionAccountType;
    }

    public void setTransactionAccountType(TransactionAccountType transactionAccountType) {
        this.transactionAccountType = transactionAccountType;
        this.transactionAccountTypeId = transactionAccountType != null ? transactionAccountType.getId() : null;
    }

    public TransactionAccount transactionAccountType(TransactionAccountType transactionAccountType) {
        this.setTransactionAccountType(transactionAccountType);
        return this;
    }

    public TransactionCurrency getTransactionCurrency() {
        return this.transactionCurrency;
    }

    public void setTransactionCurrency(TransactionCurrency transactionCurrency) {
        this.transactionCurrency = transactionCurrency;
        this.transactionCurrencyId = transactionCurrency != null ? transactionCurrency.getId() : null;
    }

    public TransactionAccount transactionCurrency(TransactionCurrency transactionCurrency) {
        this.setTransactionCurrency(transactionCurrency);
        return this;
    }

    public Long getParentAccountId() {
        return this.parentAccountId;
    }

    public void setParentAccountId(Long transactionAccount) {
        this.parentAccountId = transactionAccount;
    }

    public Long getTransactionAccountTypeId() {
        return this.transactionAccountTypeId;
    }

    public void setTransactionAccountTypeId(Long transactionAccountType) {
        this.transactionAccountTypeId = transactionAccountType;
    }

    public Long getTransactionCurrencyId() {
        return this.transactionCurrencyId;
    }

    public void setTransactionCurrencyId(Long transactionCurrency) {
        this.transactionCurrencyId = transactionCurrency;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TransactionAccount)) {
            return false;
        }
        return id != null && id.equals(((TransactionAccount) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "TransactionAccount{" +
            "id=" + getId() +
            ", accountName='" + getAccountName() + "'" +
            ", accountNumber='" + getAccountNumber() + "'" +
            ", openingBalance=" + getOpeningBalance() +
            "}";
    }
}
