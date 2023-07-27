package io.github.keeper.service.impl;

import static org.elasticsearch.index.query.QueryBuilders.*;

import io.github.keeper.domain.DealerType;
import io.github.keeper.repository.DealerTypeRepository;
import io.github.keeper.repository.search.DealerTypeSearchRepository;
import io.github.keeper.service.DealerTypeService;
import io.github.keeper.service.dto.DealerTypeDTO;
import io.github.keeper.service.mapper.DealerTypeMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Implementation for managing {@link DealerType}.
 */
@Service
@Transactional
public class DealerTypeServiceImpl implements DealerTypeService {

    private final Logger log = LoggerFactory.getLogger(DealerTypeServiceImpl.class);

    private final DealerTypeRepository dealerTypeRepository;

    private final DealerTypeMapper dealerTypeMapper;

    private final DealerTypeSearchRepository dealerTypeSearchRepository;

    public DealerTypeServiceImpl(
        DealerTypeRepository dealerTypeRepository,
        DealerTypeMapper dealerTypeMapper,
        DealerTypeSearchRepository dealerTypeSearchRepository
    ) {
        this.dealerTypeRepository = dealerTypeRepository;
        this.dealerTypeMapper = dealerTypeMapper;
        this.dealerTypeSearchRepository = dealerTypeSearchRepository;
    }

    @Override
    public Mono<DealerTypeDTO> save(DealerTypeDTO dealerTypeDTO) {
        log.debug("Request to save DealerType : {}", dealerTypeDTO);
        return dealerTypeRepository
            .save(dealerTypeMapper.toEntity(dealerTypeDTO))
            .flatMap(dealerTypeSearchRepository::save)
            .map(dealerTypeMapper::toDto);
    }

    @Override
    public Mono<DealerTypeDTO> update(DealerTypeDTO dealerTypeDTO) {
        log.debug("Request to update DealerType : {}", dealerTypeDTO);
        return dealerTypeRepository
            .save(dealerTypeMapper.toEntity(dealerTypeDTO))
            .flatMap(dealerTypeSearchRepository::save)
            .map(dealerTypeMapper::toDto);
    }

    @Override
    public Mono<DealerTypeDTO> partialUpdate(DealerTypeDTO dealerTypeDTO) {
        log.debug("Request to partially update DealerType : {}", dealerTypeDTO);

        return dealerTypeRepository
            .findById(dealerTypeDTO.getId())
            .map(existingDealerType -> {
                dealerTypeMapper.partialUpdate(existingDealerType, dealerTypeDTO);

                return existingDealerType;
            })
            .flatMap(dealerTypeRepository::save)
            .flatMap(savedDealerType -> {
                dealerTypeSearchRepository.save(savedDealerType);

                return Mono.just(savedDealerType);
            })
            .map(dealerTypeMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<DealerTypeDTO> findAll(Pageable pageable) {
        log.debug("Request to get all DealerTypes");
        return dealerTypeRepository.findAllBy(pageable).map(dealerTypeMapper::toDto);
    }

    public Mono<Long> countAll() {
        return dealerTypeRepository.count();
    }

    public Mono<Long> searchCount() {
        return dealerTypeSearchRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<DealerTypeDTO> findOne(Long id) {
        log.debug("Request to get DealerType : {}", id);
        return dealerTypeRepository.findById(id).map(dealerTypeMapper::toDto);
    }

    @Override
    public Mono<Void> delete(Long id) {
        log.debug("Request to delete DealerType : {}", id);
        return dealerTypeRepository.deleteById(id).then(dealerTypeSearchRepository.deleteById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<DealerTypeDTO> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of DealerTypes for query {}", query);
        return dealerTypeSearchRepository.search(query, pageable).map(dealerTypeMapper::toDto);
    }
}
