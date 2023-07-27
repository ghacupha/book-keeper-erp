package io.github.keeper.service.impl;

import static org.elasticsearch.index.query.QueryBuilders.*;

import io.github.keeper.domain.BalanceSheetItemType;
import io.github.keeper.repository.BalanceSheetItemTypeRepository;
import io.github.keeper.repository.search.BalanceSheetItemTypeSearchRepository;
import io.github.keeper.service.BalanceSheetItemTypeService;
import io.github.keeper.service.dto.BalanceSheetItemTypeDTO;
import io.github.keeper.service.mapper.BalanceSheetItemTypeMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Implementation for managing {@link BalanceSheetItemType}.
 */
@Service
@Transactional
public class BalanceSheetItemTypeServiceImpl implements BalanceSheetItemTypeService {

    private final Logger log = LoggerFactory.getLogger(BalanceSheetItemTypeServiceImpl.class);

    private final BalanceSheetItemTypeRepository balanceSheetItemTypeRepository;

    private final BalanceSheetItemTypeMapper balanceSheetItemTypeMapper;

    private final BalanceSheetItemTypeSearchRepository balanceSheetItemTypeSearchRepository;

    public BalanceSheetItemTypeServiceImpl(
        BalanceSheetItemTypeRepository balanceSheetItemTypeRepository,
        BalanceSheetItemTypeMapper balanceSheetItemTypeMapper,
        BalanceSheetItemTypeSearchRepository balanceSheetItemTypeSearchRepository
    ) {
        this.balanceSheetItemTypeRepository = balanceSheetItemTypeRepository;
        this.balanceSheetItemTypeMapper = balanceSheetItemTypeMapper;
        this.balanceSheetItemTypeSearchRepository = balanceSheetItemTypeSearchRepository;
    }

    @Override
    public Mono<BalanceSheetItemTypeDTO> save(BalanceSheetItemTypeDTO balanceSheetItemTypeDTO) {
        log.debug("Request to save BalanceSheetItemType : {}", balanceSheetItemTypeDTO);
        return balanceSheetItemTypeRepository
            .save(balanceSheetItemTypeMapper.toEntity(balanceSheetItemTypeDTO))
            .flatMap(balanceSheetItemTypeSearchRepository::save)
            .map(balanceSheetItemTypeMapper::toDto);
    }

    @Override
    public Mono<BalanceSheetItemTypeDTO> update(BalanceSheetItemTypeDTO balanceSheetItemTypeDTO) {
        log.debug("Request to update BalanceSheetItemType : {}", balanceSheetItemTypeDTO);
        return balanceSheetItemTypeRepository
            .save(balanceSheetItemTypeMapper.toEntity(balanceSheetItemTypeDTO))
            .flatMap(balanceSheetItemTypeSearchRepository::save)
            .map(balanceSheetItemTypeMapper::toDto);
    }

    @Override
    public Mono<BalanceSheetItemTypeDTO> partialUpdate(BalanceSheetItemTypeDTO balanceSheetItemTypeDTO) {
        log.debug("Request to partially update BalanceSheetItemType : {}", balanceSheetItemTypeDTO);

        return balanceSheetItemTypeRepository
            .findById(balanceSheetItemTypeDTO.getId())
            .map(existingBalanceSheetItemType -> {
                balanceSheetItemTypeMapper.partialUpdate(existingBalanceSheetItemType, balanceSheetItemTypeDTO);

                return existingBalanceSheetItemType;
            })
            .flatMap(balanceSheetItemTypeRepository::save)
            .flatMap(savedBalanceSheetItemType -> {
                balanceSheetItemTypeSearchRepository.save(savedBalanceSheetItemType);

                return Mono.just(savedBalanceSheetItemType);
            })
            .map(balanceSheetItemTypeMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<BalanceSheetItemTypeDTO> findAll(Pageable pageable) {
        log.debug("Request to get all BalanceSheetItemTypes");
        return balanceSheetItemTypeRepository.findAllBy(pageable).map(balanceSheetItemTypeMapper::toDto);
    }

    public Flux<BalanceSheetItemTypeDTO> findAllWithEagerRelationships(Pageable pageable) {
        return balanceSheetItemTypeRepository.findAllWithEagerRelationships(pageable).map(balanceSheetItemTypeMapper::toDto);
    }

    public Mono<Long> countAll() {
        return balanceSheetItemTypeRepository.count();
    }

    public Mono<Long> searchCount() {
        return balanceSheetItemTypeSearchRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<BalanceSheetItemTypeDTO> findOne(Long id) {
        log.debug("Request to get BalanceSheetItemType : {}", id);
        return balanceSheetItemTypeRepository.findOneWithEagerRelationships(id).map(balanceSheetItemTypeMapper::toDto);
    }

    @Override
    public Mono<Void> delete(Long id) {
        log.debug("Request to delete BalanceSheetItemType : {}", id);
        return balanceSheetItemTypeRepository.deleteById(id).then(balanceSheetItemTypeSearchRepository.deleteById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<BalanceSheetItemTypeDTO> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of BalanceSheetItemTypes for query {}", query);
        return balanceSheetItemTypeSearchRepository.search(query, pageable).map(balanceSheetItemTypeMapper::toDto);
    }
}
