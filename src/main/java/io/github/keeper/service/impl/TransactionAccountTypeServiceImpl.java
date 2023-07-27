package io.github.keeper.service.impl;

import static org.elasticsearch.index.query.QueryBuilders.*;

import io.github.keeper.domain.TransactionAccountType;
import io.github.keeper.repository.TransactionAccountTypeRepository;
import io.github.keeper.repository.search.TransactionAccountTypeSearchRepository;
import io.github.keeper.service.TransactionAccountTypeService;
import io.github.keeper.service.dto.TransactionAccountTypeDTO;
import io.github.keeper.service.mapper.TransactionAccountTypeMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Implementation for managing {@link TransactionAccountType}.
 */
@Service
@Transactional
public class TransactionAccountTypeServiceImpl implements TransactionAccountTypeService {

    private final Logger log = LoggerFactory.getLogger(TransactionAccountTypeServiceImpl.class);

    private final TransactionAccountTypeRepository transactionAccountTypeRepository;

    private final TransactionAccountTypeMapper transactionAccountTypeMapper;

    private final TransactionAccountTypeSearchRepository transactionAccountTypeSearchRepository;

    public TransactionAccountTypeServiceImpl(
        TransactionAccountTypeRepository transactionAccountTypeRepository,
        TransactionAccountTypeMapper transactionAccountTypeMapper,
        TransactionAccountTypeSearchRepository transactionAccountTypeSearchRepository
    ) {
        this.transactionAccountTypeRepository = transactionAccountTypeRepository;
        this.transactionAccountTypeMapper = transactionAccountTypeMapper;
        this.transactionAccountTypeSearchRepository = transactionAccountTypeSearchRepository;
    }

    @Override
    public Mono<TransactionAccountTypeDTO> save(TransactionAccountTypeDTO transactionAccountTypeDTO) {
        log.debug("Request to save TransactionAccountType : {}", transactionAccountTypeDTO);
        return transactionAccountTypeRepository
            .save(transactionAccountTypeMapper.toEntity(transactionAccountTypeDTO))
            .flatMap(transactionAccountTypeSearchRepository::save)
            .map(transactionAccountTypeMapper::toDto);
    }

    @Override
    public Mono<TransactionAccountTypeDTO> update(TransactionAccountTypeDTO transactionAccountTypeDTO) {
        log.debug("Request to update TransactionAccountType : {}", transactionAccountTypeDTO);
        return transactionAccountTypeRepository
            .save(transactionAccountTypeMapper.toEntity(transactionAccountTypeDTO))
            .flatMap(transactionAccountTypeSearchRepository::save)
            .map(transactionAccountTypeMapper::toDto);
    }

    @Override
    public Mono<TransactionAccountTypeDTO> partialUpdate(TransactionAccountTypeDTO transactionAccountTypeDTO) {
        log.debug("Request to partially update TransactionAccountType : {}", transactionAccountTypeDTO);

        return transactionAccountTypeRepository
            .findById(transactionAccountTypeDTO.getId())
            .map(existingTransactionAccountType -> {
                transactionAccountTypeMapper.partialUpdate(existingTransactionAccountType, transactionAccountTypeDTO);

                return existingTransactionAccountType;
            })
            .flatMap(transactionAccountTypeRepository::save)
            .flatMap(savedTransactionAccountType -> {
                transactionAccountTypeSearchRepository.save(savedTransactionAccountType);

                return Mono.just(savedTransactionAccountType);
            })
            .map(transactionAccountTypeMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<TransactionAccountTypeDTO> findAll(Pageable pageable) {
        log.debug("Request to get all TransactionAccountTypes");
        return transactionAccountTypeRepository.findAllBy(pageable).map(transactionAccountTypeMapper::toDto);
    }

    public Mono<Long> countAll() {
        return transactionAccountTypeRepository.count();
    }

    public Mono<Long> searchCount() {
        return transactionAccountTypeSearchRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<TransactionAccountTypeDTO> findOne(Long id) {
        log.debug("Request to get TransactionAccountType : {}", id);
        return transactionAccountTypeRepository.findById(id).map(transactionAccountTypeMapper::toDto);
    }

    @Override
    public Mono<Void> delete(Long id) {
        log.debug("Request to delete TransactionAccountType : {}", id);
        return transactionAccountTypeRepository.deleteById(id).then(transactionAccountTypeSearchRepository.deleteById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<TransactionAccountTypeDTO> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of TransactionAccountTypes for query {}", query);
        return transactionAccountTypeSearchRepository.search(query, pageable).map(transactionAccountTypeMapper::toDto);
    }
}
