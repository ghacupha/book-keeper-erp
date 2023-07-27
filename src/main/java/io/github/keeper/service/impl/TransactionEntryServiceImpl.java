package io.github.keeper.service.impl;

import static org.elasticsearch.index.query.QueryBuilders.*;

import io.github.keeper.domain.TransactionEntry;
import io.github.keeper.repository.TransactionEntryRepository;
import io.github.keeper.repository.search.TransactionEntrySearchRepository;
import io.github.keeper.service.TransactionEntryService;
import io.github.keeper.service.dto.TransactionEntryDTO;
import io.github.keeper.service.mapper.TransactionEntryMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Implementation for managing {@link TransactionEntry}.
 */
@Service
@Transactional
public class TransactionEntryServiceImpl implements TransactionEntryService {

    private final Logger log = LoggerFactory.getLogger(TransactionEntryServiceImpl.class);

    private final TransactionEntryRepository transactionEntryRepository;

    private final TransactionEntryMapper transactionEntryMapper;

    private final TransactionEntrySearchRepository transactionEntrySearchRepository;

    public TransactionEntryServiceImpl(
        TransactionEntryRepository transactionEntryRepository,
        TransactionEntryMapper transactionEntryMapper,
        TransactionEntrySearchRepository transactionEntrySearchRepository
    ) {
        this.transactionEntryRepository = transactionEntryRepository;
        this.transactionEntryMapper = transactionEntryMapper;
        this.transactionEntrySearchRepository = transactionEntrySearchRepository;
    }

    @Override
    public Mono<TransactionEntryDTO> save(TransactionEntryDTO transactionEntryDTO) {
        log.debug("Request to save TransactionEntry : {}", transactionEntryDTO);
        return transactionEntryRepository
            .save(transactionEntryMapper.toEntity(transactionEntryDTO))
            .flatMap(transactionEntrySearchRepository::save)
            .map(transactionEntryMapper::toDto);
    }

    @Override
    public Mono<TransactionEntryDTO> update(TransactionEntryDTO transactionEntryDTO) {
        log.debug("Request to update TransactionEntry : {}", transactionEntryDTO);
        return transactionEntryRepository
            .save(transactionEntryMapper.toEntity(transactionEntryDTO))
            .flatMap(transactionEntrySearchRepository::save)
            .map(transactionEntryMapper::toDto);
    }

    @Override
    public Mono<TransactionEntryDTO> partialUpdate(TransactionEntryDTO transactionEntryDTO) {
        log.debug("Request to partially update TransactionEntry : {}", transactionEntryDTO);

        return transactionEntryRepository
            .findById(transactionEntryDTO.getId())
            .map(existingTransactionEntry -> {
                transactionEntryMapper.partialUpdate(existingTransactionEntry, transactionEntryDTO);

                return existingTransactionEntry;
            })
            .flatMap(transactionEntryRepository::save)
            .flatMap(savedTransactionEntry -> {
                transactionEntrySearchRepository.save(savedTransactionEntry);

                return Mono.just(savedTransactionEntry);
            })
            .map(transactionEntryMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<TransactionEntryDTO> findAll(Pageable pageable) {
        log.debug("Request to get all TransactionEntries");
        return transactionEntryRepository.findAllBy(pageable).map(transactionEntryMapper::toDto);
    }

    public Flux<TransactionEntryDTO> findAllWithEagerRelationships(Pageable pageable) {
        return transactionEntryRepository.findAllWithEagerRelationships(pageable).map(transactionEntryMapper::toDto);
    }

    public Mono<Long> countAll() {
        return transactionEntryRepository.count();
    }

    public Mono<Long> searchCount() {
        return transactionEntrySearchRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<TransactionEntryDTO> findOne(Long id) {
        log.debug("Request to get TransactionEntry : {}", id);
        return transactionEntryRepository.findOneWithEagerRelationships(id).map(transactionEntryMapper::toDto);
    }

    @Override
    public Mono<Void> delete(Long id) {
        log.debug("Request to delete TransactionEntry : {}", id);
        return transactionEntryRepository.deleteById(id).then(transactionEntrySearchRepository.deleteById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<TransactionEntryDTO> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of TransactionEntries for query {}", query);
        return transactionEntrySearchRepository.search(query, pageable).map(transactionEntryMapper::toDto);
    }
}
