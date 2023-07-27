package io.github.keeper.service.impl;

import static org.elasticsearch.index.query.QueryBuilders.*;

import io.github.keeper.domain.TransactionAccount;
import io.github.keeper.repository.TransactionAccountRepository;
import io.github.keeper.repository.search.TransactionAccountSearchRepository;
import io.github.keeper.service.TransactionAccountService;
import io.github.keeper.service.dto.TransactionAccountDTO;
import io.github.keeper.service.mapper.TransactionAccountMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Implementation for managing {@link TransactionAccount}.
 */
@Service
@Transactional
public class TransactionAccountServiceImpl implements TransactionAccountService {

    private final Logger log = LoggerFactory.getLogger(TransactionAccountServiceImpl.class);

    private final TransactionAccountRepository transactionAccountRepository;

    private final TransactionAccountMapper transactionAccountMapper;

    private final TransactionAccountSearchRepository transactionAccountSearchRepository;

    public TransactionAccountServiceImpl(
        TransactionAccountRepository transactionAccountRepository,
        TransactionAccountMapper transactionAccountMapper,
        TransactionAccountSearchRepository transactionAccountSearchRepository
    ) {
        this.transactionAccountRepository = transactionAccountRepository;
        this.transactionAccountMapper = transactionAccountMapper;
        this.transactionAccountSearchRepository = transactionAccountSearchRepository;
    }

    @Override
    public Mono<TransactionAccountDTO> save(TransactionAccountDTO transactionAccountDTO) {
        log.debug("Request to save TransactionAccount : {}", transactionAccountDTO);
        return transactionAccountRepository
            .save(transactionAccountMapper.toEntity(transactionAccountDTO))
            .flatMap(transactionAccountSearchRepository::save)
            .map(transactionAccountMapper::toDto);
    }

    @Override
    public Mono<TransactionAccountDTO> update(TransactionAccountDTO transactionAccountDTO) {
        log.debug("Request to update TransactionAccount : {}", transactionAccountDTO);
        return transactionAccountRepository
            .save(transactionAccountMapper.toEntity(transactionAccountDTO))
            .flatMap(transactionAccountSearchRepository::save)
            .map(transactionAccountMapper::toDto);
    }

    @Override
    public Mono<TransactionAccountDTO> partialUpdate(TransactionAccountDTO transactionAccountDTO) {
        log.debug("Request to partially update TransactionAccount : {}", transactionAccountDTO);

        return transactionAccountRepository
            .findById(transactionAccountDTO.getId())
            .map(existingTransactionAccount -> {
                transactionAccountMapper.partialUpdate(existingTransactionAccount, transactionAccountDTO);

                return existingTransactionAccount;
            })
            .flatMap(transactionAccountRepository::save)
            .flatMap(savedTransactionAccount -> {
                transactionAccountSearchRepository.save(savedTransactionAccount);

                return Mono.just(savedTransactionAccount);
            })
            .map(transactionAccountMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<TransactionAccountDTO> findAll(Pageable pageable) {
        log.debug("Request to get all TransactionAccounts");
        return transactionAccountRepository.findAllBy(pageable).map(transactionAccountMapper::toDto);
    }

    public Flux<TransactionAccountDTO> findAllWithEagerRelationships(Pageable pageable) {
        return transactionAccountRepository.findAllWithEagerRelationships(pageable).map(transactionAccountMapper::toDto);
    }

    public Mono<Long> countAll() {
        return transactionAccountRepository.count();
    }

    public Mono<Long> searchCount() {
        return transactionAccountSearchRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<TransactionAccountDTO> findOne(Long id) {
        log.debug("Request to get TransactionAccount : {}", id);
        return transactionAccountRepository.findOneWithEagerRelationships(id).map(transactionAccountMapper::toDto);
    }

    @Override
    public Mono<Void> delete(Long id) {
        log.debug("Request to delete TransactionAccount : {}", id);
        return transactionAccountRepository.deleteById(id).then(transactionAccountSearchRepository.deleteById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<TransactionAccountDTO> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of TransactionAccounts for query {}", query);
        return transactionAccountSearchRepository.search(query, pageable).map(transactionAccountMapper::toDto);
    }
}
