package io.github.keeper.service.impl;

import static org.elasticsearch.index.query.QueryBuilders.*;

import io.github.keeper.domain.AccountingEvent;
import io.github.keeper.repository.AccountingEventRepository;
import io.github.keeper.repository.search.AccountingEventSearchRepository;
import io.github.keeper.service.AccountingEventService;
import io.github.keeper.service.dto.AccountingEventDTO;
import io.github.keeper.service.mapper.AccountingEventMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Implementation for managing {@link AccountingEvent}.
 */
@Service
@Transactional
public class AccountingEventServiceImpl implements AccountingEventService {

    private final Logger log = LoggerFactory.getLogger(AccountingEventServiceImpl.class);

    private final AccountingEventRepository accountingEventRepository;

    private final AccountingEventMapper accountingEventMapper;

    private final AccountingEventSearchRepository accountingEventSearchRepository;

    public AccountingEventServiceImpl(
        AccountingEventRepository accountingEventRepository,
        AccountingEventMapper accountingEventMapper,
        AccountingEventSearchRepository accountingEventSearchRepository
    ) {
        this.accountingEventRepository = accountingEventRepository;
        this.accountingEventMapper = accountingEventMapper;
        this.accountingEventSearchRepository = accountingEventSearchRepository;
    }

    @Override
    public Mono<AccountingEventDTO> save(AccountingEventDTO accountingEventDTO) {
        log.debug("Request to save AccountingEvent : {}", accountingEventDTO);
        return accountingEventRepository
            .save(accountingEventMapper.toEntity(accountingEventDTO))
            .flatMap(accountingEventSearchRepository::save)
            .map(accountingEventMapper::toDto);
    }

    @Override
    public Mono<AccountingEventDTO> update(AccountingEventDTO accountingEventDTO) {
        log.debug("Request to update AccountingEvent : {}", accountingEventDTO);
        return accountingEventRepository
            .save(accountingEventMapper.toEntity(accountingEventDTO))
            .flatMap(accountingEventSearchRepository::save)
            .map(accountingEventMapper::toDto);
    }

    @Override
    public Mono<AccountingEventDTO> partialUpdate(AccountingEventDTO accountingEventDTO) {
        log.debug("Request to partially update AccountingEvent : {}", accountingEventDTO);

        return accountingEventRepository
            .findById(accountingEventDTO.getId())
            .map(existingAccountingEvent -> {
                accountingEventMapper.partialUpdate(existingAccountingEvent, accountingEventDTO);

                return existingAccountingEvent;
            })
            .flatMap(accountingEventRepository::save)
            .flatMap(savedAccountingEvent -> {
                accountingEventSearchRepository.save(savedAccountingEvent);

                return Mono.just(savedAccountingEvent);
            })
            .map(accountingEventMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<AccountingEventDTO> findAll(Pageable pageable) {
        log.debug("Request to get all AccountingEvents");
        return accountingEventRepository.findAllBy(pageable).map(accountingEventMapper::toDto);
    }

    public Flux<AccountingEventDTO> findAllWithEagerRelationships(Pageable pageable) {
        return accountingEventRepository.findAllWithEagerRelationships(pageable).map(accountingEventMapper::toDto);
    }

    public Mono<Long> countAll() {
        return accountingEventRepository.count();
    }

    public Mono<Long> searchCount() {
        return accountingEventSearchRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<AccountingEventDTO> findOne(Long id) {
        log.debug("Request to get AccountingEvent : {}", id);
        return accountingEventRepository.findOneWithEagerRelationships(id).map(accountingEventMapper::toDto);
    }

    @Override
    public Mono<Void> delete(Long id) {
        log.debug("Request to delete AccountingEvent : {}", id);
        return accountingEventRepository.deleteById(id).then(accountingEventSearchRepository.deleteById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<AccountingEventDTO> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of AccountingEvents for query {}", query);
        return accountingEventSearchRepository.search(query, pageable).map(accountingEventMapper::toDto);
    }
}
