package io.github.keeper.service.impl;

import static org.elasticsearch.index.query.QueryBuilders.*;

import io.github.keeper.domain.Dealer;
import io.github.keeper.repository.DealerRepository;
import io.github.keeper.repository.search.DealerSearchRepository;
import io.github.keeper.service.DealerService;
import io.github.keeper.service.dto.DealerDTO;
import io.github.keeper.service.mapper.DealerMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Implementation for managing {@link Dealer}.
 */
@Service
@Transactional
public class DealerServiceImpl implements DealerService {

    private final Logger log = LoggerFactory.getLogger(DealerServiceImpl.class);

    private final DealerRepository dealerRepository;

    private final DealerMapper dealerMapper;

    private final DealerSearchRepository dealerSearchRepository;

    public DealerServiceImpl(DealerRepository dealerRepository, DealerMapper dealerMapper, DealerSearchRepository dealerSearchRepository) {
        this.dealerRepository = dealerRepository;
        this.dealerMapper = dealerMapper;
        this.dealerSearchRepository = dealerSearchRepository;
    }

    @Override
    public Mono<DealerDTO> save(DealerDTO dealerDTO) {
        log.debug("Request to save Dealer : {}", dealerDTO);
        return dealerRepository.save(dealerMapper.toEntity(dealerDTO)).flatMap(dealerSearchRepository::save).map(dealerMapper::toDto);
    }

    @Override
    public Mono<DealerDTO> update(DealerDTO dealerDTO) {
        log.debug("Request to update Dealer : {}", dealerDTO);
        return dealerRepository.save(dealerMapper.toEntity(dealerDTO)).flatMap(dealerSearchRepository::save).map(dealerMapper::toDto);
    }

    @Override
    public Mono<DealerDTO> partialUpdate(DealerDTO dealerDTO) {
        log.debug("Request to partially update Dealer : {}", dealerDTO);

        return dealerRepository
            .findById(dealerDTO.getId())
            .map(existingDealer -> {
                dealerMapper.partialUpdate(existingDealer, dealerDTO);

                return existingDealer;
            })
            .flatMap(dealerRepository::save)
            .flatMap(savedDealer -> {
                dealerSearchRepository.save(savedDealer);

                return Mono.just(savedDealer);
            })
            .map(dealerMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<DealerDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Dealers");
        return dealerRepository.findAllBy(pageable).map(dealerMapper::toDto);
    }

    public Flux<DealerDTO> findAllWithEagerRelationships(Pageable pageable) {
        return dealerRepository.findAllWithEagerRelationships(pageable).map(dealerMapper::toDto);
    }

    public Mono<Long> countAll() {
        return dealerRepository.count();
    }

    public Mono<Long> searchCount() {
        return dealerSearchRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<DealerDTO> findOne(Long id) {
        log.debug("Request to get Dealer : {}", id);
        return dealerRepository.findOneWithEagerRelationships(id).map(dealerMapper::toDto);
    }

    @Override
    public Mono<Void> delete(Long id) {
        log.debug("Request to delete Dealer : {}", id);
        return dealerRepository.deleteById(id).then(dealerSearchRepository.deleteById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<DealerDTO> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of Dealers for query {}", query);
        return dealerSearchRepository.search(query, pageable).map(dealerMapper::toDto);
    }
}
