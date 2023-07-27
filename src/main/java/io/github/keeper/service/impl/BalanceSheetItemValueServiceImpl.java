package io.github.keeper.service.impl;

import static org.elasticsearch.index.query.QueryBuilders.*;

import io.github.keeper.domain.BalanceSheetItemValue;
import io.github.keeper.repository.BalanceSheetItemValueRepository;
import io.github.keeper.repository.search.BalanceSheetItemValueSearchRepository;
import io.github.keeper.service.BalanceSheetItemValueService;
import io.github.keeper.service.dto.BalanceSheetItemValueDTO;
import io.github.keeper.service.mapper.BalanceSheetItemValueMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Implementation for managing {@link BalanceSheetItemValue}.
 */
@Service
@Transactional
public class BalanceSheetItemValueServiceImpl implements BalanceSheetItemValueService {

    private final Logger log = LoggerFactory.getLogger(BalanceSheetItemValueServiceImpl.class);

    private final BalanceSheetItemValueRepository balanceSheetItemValueRepository;

    private final BalanceSheetItemValueMapper balanceSheetItemValueMapper;

    private final BalanceSheetItemValueSearchRepository balanceSheetItemValueSearchRepository;

    public BalanceSheetItemValueServiceImpl(
        BalanceSheetItemValueRepository balanceSheetItemValueRepository,
        BalanceSheetItemValueMapper balanceSheetItemValueMapper,
        BalanceSheetItemValueSearchRepository balanceSheetItemValueSearchRepository
    ) {
        this.balanceSheetItemValueRepository = balanceSheetItemValueRepository;
        this.balanceSheetItemValueMapper = balanceSheetItemValueMapper;
        this.balanceSheetItemValueSearchRepository = balanceSheetItemValueSearchRepository;
    }

    @Override
    public Mono<BalanceSheetItemValueDTO> save(BalanceSheetItemValueDTO balanceSheetItemValueDTO) {
        log.debug("Request to save BalanceSheetItemValue : {}", balanceSheetItemValueDTO);
        return balanceSheetItemValueRepository
            .save(balanceSheetItemValueMapper.toEntity(balanceSheetItemValueDTO))
            .flatMap(balanceSheetItemValueSearchRepository::save)
            .map(balanceSheetItemValueMapper::toDto);
    }

    @Override
    public Mono<BalanceSheetItemValueDTO> update(BalanceSheetItemValueDTO balanceSheetItemValueDTO) {
        log.debug("Request to update BalanceSheetItemValue : {}", balanceSheetItemValueDTO);
        return balanceSheetItemValueRepository
            .save(balanceSheetItemValueMapper.toEntity(balanceSheetItemValueDTO))
            .flatMap(balanceSheetItemValueSearchRepository::save)
            .map(balanceSheetItemValueMapper::toDto);
    }

    @Override
    public Mono<BalanceSheetItemValueDTO> partialUpdate(BalanceSheetItemValueDTO balanceSheetItemValueDTO) {
        log.debug("Request to partially update BalanceSheetItemValue : {}", balanceSheetItemValueDTO);

        return balanceSheetItemValueRepository
            .findById(balanceSheetItemValueDTO.getId())
            .map(existingBalanceSheetItemValue -> {
                balanceSheetItemValueMapper.partialUpdate(existingBalanceSheetItemValue, balanceSheetItemValueDTO);

                return existingBalanceSheetItemValue;
            })
            .flatMap(balanceSheetItemValueRepository::save)
            .flatMap(savedBalanceSheetItemValue -> {
                balanceSheetItemValueSearchRepository.save(savedBalanceSheetItemValue);

                return Mono.just(savedBalanceSheetItemValue);
            })
            .map(balanceSheetItemValueMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<BalanceSheetItemValueDTO> findAll(Pageable pageable) {
        log.debug("Request to get all BalanceSheetItemValues");
        return balanceSheetItemValueRepository.findAllBy(pageable).map(balanceSheetItemValueMapper::toDto);
    }

    public Flux<BalanceSheetItemValueDTO> findAllWithEagerRelationships(Pageable pageable) {
        return balanceSheetItemValueRepository.findAllWithEagerRelationships(pageable).map(balanceSheetItemValueMapper::toDto);
    }

    public Mono<Long> countAll() {
        return balanceSheetItemValueRepository.count();
    }

    public Mono<Long> searchCount() {
        return balanceSheetItemValueSearchRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<BalanceSheetItemValueDTO> findOne(Long id) {
        log.debug("Request to get BalanceSheetItemValue : {}", id);
        return balanceSheetItemValueRepository.findOneWithEagerRelationships(id).map(balanceSheetItemValueMapper::toDto);
    }

    @Override
    public Mono<Void> delete(Long id) {
        log.debug("Request to delete BalanceSheetItemValue : {}", id);
        return balanceSheetItemValueRepository.deleteById(id).then(balanceSheetItemValueSearchRepository.deleteById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<BalanceSheetItemValueDTO> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of BalanceSheetItemValues for query {}", query);
        return balanceSheetItemValueSearchRepository.search(query, pageable).map(balanceSheetItemValueMapper::toDto);
    }
}
