package io.github.keeper.service.impl;

import static org.elasticsearch.index.query.QueryBuilders.*;

import io.github.keeper.domain.EventType;
import io.github.keeper.repository.EventTypeRepository;
import io.github.keeper.repository.search.EventTypeSearchRepository;
import io.github.keeper.service.EventTypeService;
import io.github.keeper.service.dto.EventTypeDTO;
import io.github.keeper.service.mapper.EventTypeMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Implementation for managing {@link EventType}.
 */
@Service
@Transactional
public class EventTypeServiceImpl implements EventTypeService {

    private final Logger log = LoggerFactory.getLogger(EventTypeServiceImpl.class);

    private final EventTypeRepository eventTypeRepository;

    private final EventTypeMapper eventTypeMapper;

    private final EventTypeSearchRepository eventTypeSearchRepository;

    public EventTypeServiceImpl(
        EventTypeRepository eventTypeRepository,
        EventTypeMapper eventTypeMapper,
        EventTypeSearchRepository eventTypeSearchRepository
    ) {
        this.eventTypeRepository = eventTypeRepository;
        this.eventTypeMapper = eventTypeMapper;
        this.eventTypeSearchRepository = eventTypeSearchRepository;
    }

    @Override
    public Mono<EventTypeDTO> save(EventTypeDTO eventTypeDTO) {
        log.debug("Request to save EventType : {}", eventTypeDTO);
        return eventTypeRepository
            .save(eventTypeMapper.toEntity(eventTypeDTO))
            .flatMap(eventTypeSearchRepository::save)
            .map(eventTypeMapper::toDto);
    }

    @Override
    public Mono<EventTypeDTO> update(EventTypeDTO eventTypeDTO) {
        log.debug("Request to update EventType : {}", eventTypeDTO);
        return eventTypeRepository
            .save(eventTypeMapper.toEntity(eventTypeDTO))
            .flatMap(eventTypeSearchRepository::save)
            .map(eventTypeMapper::toDto);
    }

    @Override
    public Mono<EventTypeDTO> partialUpdate(EventTypeDTO eventTypeDTO) {
        log.debug("Request to partially update EventType : {}", eventTypeDTO);

        return eventTypeRepository
            .findById(eventTypeDTO.getId())
            .map(existingEventType -> {
                eventTypeMapper.partialUpdate(existingEventType, eventTypeDTO);

                return existingEventType;
            })
            .flatMap(eventTypeRepository::save)
            .flatMap(savedEventType -> {
                eventTypeSearchRepository.save(savedEventType);

                return Mono.just(savedEventType);
            })
            .map(eventTypeMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<EventTypeDTO> findAll(Pageable pageable) {
        log.debug("Request to get all EventTypes");
        return eventTypeRepository.findAllBy(pageable).map(eventTypeMapper::toDto);
    }

    public Mono<Long> countAll() {
        return eventTypeRepository.count();
    }

    public Mono<Long> searchCount() {
        return eventTypeSearchRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<EventTypeDTO> findOne(Long id) {
        log.debug("Request to get EventType : {}", id);
        return eventTypeRepository.findById(id).map(eventTypeMapper::toDto);
    }

    @Override
    public Mono<Void> delete(Long id) {
        log.debug("Request to delete EventType : {}", id);
        return eventTypeRepository.deleteById(id).then(eventTypeSearchRepository.deleteById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<EventTypeDTO> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of EventTypes for query {}", query);
        return eventTypeSearchRepository.search(query, pageable).map(eventTypeMapper::toDto);
    }
}
