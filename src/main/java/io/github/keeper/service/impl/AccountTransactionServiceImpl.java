package io.github.keeper.service.impl;

import static org.elasticsearch.index.query.QueryBuilders.*;

import io.github.keeper.domain.AccountTransaction;
import io.github.keeper.repository.AccountTransactionRepository;
import io.github.keeper.repository.search.AccountTransactionSearchRepository;
import io.github.keeper.service.AccountTransactionService;
import io.github.keeper.service.dto.AccountTransactionDTO;
import io.github.keeper.service.mapper.AccountTransactionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Implementation for managing {@link AccountTransaction}.
 */
@Service
@Transactional
public class AccountTransactionServiceImpl implements AccountTransactionService {

    private final Logger log = LoggerFactory.getLogger(AccountTransactionServiceImpl.class);

    private final AccountTransactionRepository accountTransactionRepository;

    private final AccountTransactionMapper accountTransactionMapper;

    private final AccountTransactionSearchRepository accountTransactionSearchRepository;

    public AccountTransactionServiceImpl(
        AccountTransactionRepository accountTransactionRepository,
        AccountTransactionMapper accountTransactionMapper,
        AccountTransactionSearchRepository accountTransactionSearchRepository
    ) {
        this.accountTransactionRepository = accountTransactionRepository;
        this.accountTransactionMapper = accountTransactionMapper;
        this.accountTransactionSearchRepository = accountTransactionSearchRepository;
    }

    @Override
    public Mono<AccountTransactionDTO> save(AccountTransactionDTO accountTransactionDTO) {
        log.debug("Request to save AccountTransaction : {}", accountTransactionDTO);
        return accountTransactionRepository
            .save(accountTransactionMapper.toEntity(accountTransactionDTO))
            .flatMap(accountTransactionSearchRepository::save)
            .map(accountTransactionMapper::toDto);
    }

    @Override
    public Mono<AccountTransactionDTO> update(AccountTransactionDTO accountTransactionDTO) {
        log.debug("Request to update AccountTransaction : {}", accountTransactionDTO);
        return accountTransactionRepository
            .save(accountTransactionMapper.toEntity(accountTransactionDTO))
            .flatMap(accountTransactionSearchRepository::save)
            .map(accountTransactionMapper::toDto);
    }

    @Override
    public Mono<AccountTransactionDTO> partialUpdate(AccountTransactionDTO accountTransactionDTO) {
        log.debug("Request to partially update AccountTransaction : {}", accountTransactionDTO);

        return accountTransactionRepository
            .findById(accountTransactionDTO.getId())
            .map(existingAccountTransaction -> {
                accountTransactionMapper.partialUpdate(existingAccountTransaction, accountTransactionDTO);

                return existingAccountTransaction;
            })
            .flatMap(accountTransactionRepository::save)
            .flatMap(savedAccountTransaction -> {
                accountTransactionSearchRepository.save(savedAccountTransaction);

                return Mono.just(savedAccountTransaction);
            })
            .map(accountTransactionMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<AccountTransactionDTO> findAll(Pageable pageable) {
        log.debug("Request to get all AccountTransactions");
        return accountTransactionRepository.findAllBy(pageable).map(accountTransactionMapper::toDto);
    }

    public Mono<Long> countAll() {
        return accountTransactionRepository.count();
    }

    public Mono<Long> searchCount() {
        return accountTransactionSearchRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<AccountTransactionDTO> findOne(Long id) {
        log.debug("Request to get AccountTransaction : {}", id);
        return accountTransactionRepository.findById(id).map(accountTransactionMapper::toDto);
    }

    @Override
    public Mono<Void> delete(Long id) {
        log.debug("Request to delete AccountTransaction : {}", id);
        return accountTransactionRepository.deleteById(id).then(accountTransactionSearchRepository.deleteById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<AccountTransactionDTO> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of AccountTransactions for query {}", query);
        return accountTransactionSearchRepository.search(query, pageable).map(accountTransactionMapper::toDto);
    }
}
