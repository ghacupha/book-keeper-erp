package io.github.keeper.service.impl;

import static org.elasticsearch.index.query.QueryBuilders.*;

import io.github.keeper.domain.TransactionCurrency;
import io.github.keeper.repository.TransactionCurrencyRepository;
import io.github.keeper.repository.search.TransactionCurrencySearchRepository;
import io.github.keeper.service.TransactionCurrencyService;
import io.github.keeper.service.dto.TransactionCurrencyDTO;
import io.github.keeper.service.mapper.TransactionCurrencyMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Implementation for managing {@link TransactionCurrency}.
 */
@Service
@Transactional
public class TransactionCurrencyServiceImpl implements TransactionCurrencyService {

    private final Logger log = LoggerFactory.getLogger(TransactionCurrencyServiceImpl.class);

    private final TransactionCurrencyRepository transactionCurrencyRepository;

    private final TransactionCurrencyMapper transactionCurrencyMapper;

    private final TransactionCurrencySearchRepository transactionCurrencySearchRepository;

    public TransactionCurrencyServiceImpl(
        TransactionCurrencyRepository transactionCurrencyRepository,
        TransactionCurrencyMapper transactionCurrencyMapper,
        TransactionCurrencySearchRepository transactionCurrencySearchRepository
    ) {
        this.transactionCurrencyRepository = transactionCurrencyRepository;
        this.transactionCurrencyMapper = transactionCurrencyMapper;
        this.transactionCurrencySearchRepository = transactionCurrencySearchRepository;
    }

    @Override
    public Mono<TransactionCurrencyDTO> save(TransactionCurrencyDTO transactionCurrencyDTO) {
        log.debug("Request to save TransactionCurrency : {}", transactionCurrencyDTO);
        return transactionCurrencyRepository
            .save(transactionCurrencyMapper.toEntity(transactionCurrencyDTO))
            .flatMap(transactionCurrencySearchRepository::save)
            .map(transactionCurrencyMapper::toDto);
    }

    @Override
    public Mono<TransactionCurrencyDTO> update(TransactionCurrencyDTO transactionCurrencyDTO) {
        log.debug("Request to update TransactionCurrency : {}", transactionCurrencyDTO);
        return transactionCurrencyRepository
            .save(transactionCurrencyMapper.toEntity(transactionCurrencyDTO))
            .flatMap(transactionCurrencySearchRepository::save)
            .map(transactionCurrencyMapper::toDto);
    }

    @Override
    public Mono<TransactionCurrencyDTO> partialUpdate(TransactionCurrencyDTO transactionCurrencyDTO) {
        log.debug("Request to partially update TransactionCurrency : {}", transactionCurrencyDTO);

        return transactionCurrencyRepository
            .findById(transactionCurrencyDTO.getId())
            .map(existingTransactionCurrency -> {
                transactionCurrencyMapper.partialUpdate(existingTransactionCurrency, transactionCurrencyDTO);

                return existingTransactionCurrency;
            })
            .flatMap(transactionCurrencyRepository::save)
            .flatMap(savedTransactionCurrency -> {
                transactionCurrencySearchRepository.save(savedTransactionCurrency);

                return Mono.just(savedTransactionCurrency);
            })
            .map(transactionCurrencyMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<TransactionCurrencyDTO> findAll(Pageable pageable) {
        log.debug("Request to get all TransactionCurrencies");
        return transactionCurrencyRepository.findAllBy(pageable).map(transactionCurrencyMapper::toDto);
    }

    public Mono<Long> countAll() {
        return transactionCurrencyRepository.count();
    }

    public Mono<Long> searchCount() {
        return transactionCurrencySearchRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<TransactionCurrencyDTO> findOne(Long id) {
        log.debug("Request to get TransactionCurrency : {}", id);
        return transactionCurrencyRepository.findById(id).map(transactionCurrencyMapper::toDto);
    }

    @Override
    public Mono<Void> delete(Long id) {
        log.debug("Request to delete TransactionCurrency : {}", id);
        return transactionCurrencyRepository.deleteById(id).then(transactionCurrencySearchRepository.deleteById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<TransactionCurrencyDTO> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of TransactionCurrencies for query {}", query);
        return transactionCurrencySearchRepository.search(query, pageable).map(transactionCurrencyMapper::toDto);
    }
}
