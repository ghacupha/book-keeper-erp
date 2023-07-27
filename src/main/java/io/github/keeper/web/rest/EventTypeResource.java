package io.github.keeper.web.rest;

import io.github.keeper.repository.EventTypeRepository;
import io.github.keeper.service.EventTypeService;
import io.github.keeper.service.dto.EventTypeDTO;
import io.github.keeper.web.rest.errors.BadRequestAlertException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.reactive.ResponseUtil;

/**
 * REST controller for managing {@link io.github.keeper.domain.EventType}.
 */
@RestController
@RequestMapping("/api")
public class EventTypeResource {

    private final Logger log = LoggerFactory.getLogger(EventTypeResource.class);

    private static final String ENTITY_NAME = "eventType";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final EventTypeService eventTypeService;

    private final EventTypeRepository eventTypeRepository;

    public EventTypeResource(EventTypeService eventTypeService, EventTypeRepository eventTypeRepository) {
        this.eventTypeService = eventTypeService;
        this.eventTypeRepository = eventTypeRepository;
    }

    /**
     * {@code POST  /event-types} : Create a new eventType.
     *
     * @param eventTypeDTO the eventTypeDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new eventTypeDTO, or with status {@code 400 (Bad Request)} if the eventType has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/event-types")
    public Mono<ResponseEntity<EventTypeDTO>> createEventType(@Valid @RequestBody EventTypeDTO eventTypeDTO) throws URISyntaxException {
        log.debug("REST request to save EventType : {}", eventTypeDTO);
        if (eventTypeDTO.getId() != null) {
            throw new BadRequestAlertException("A new eventType cannot already have an ID", ENTITY_NAME, "idexists");
        }
        return eventTypeService
            .save(eventTypeDTO)
            .map(result -> {
                try {
                    return ResponseEntity
                        .created(new URI("/api/event-types/" + result.getId()))
                        .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
                        .body(result);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    /**
     * {@code PUT  /event-types/:id} : Updates an existing eventType.
     *
     * @param id the id of the eventTypeDTO to save.
     * @param eventTypeDTO the eventTypeDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated eventTypeDTO,
     * or with status {@code 400 (Bad Request)} if the eventTypeDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the eventTypeDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/event-types/{id}")
    public Mono<ResponseEntity<EventTypeDTO>> updateEventType(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody EventTypeDTO eventTypeDTO
    ) throws URISyntaxException {
        log.debug("REST request to update EventType : {}, {}", id, eventTypeDTO);
        if (eventTypeDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, eventTypeDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return eventTypeRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                return eventTypeService
                    .update(eventTypeDTO)
                    .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                    .map(result ->
                        ResponseEntity
                            .ok()
                            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
                            .body(result)
                    );
            });
    }

    /**
     * {@code PATCH  /event-types/:id} : Partial updates given fields of an existing eventType, field will ignore if it is null
     *
     * @param id the id of the eventTypeDTO to save.
     * @param eventTypeDTO the eventTypeDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated eventTypeDTO,
     * or with status {@code 400 (Bad Request)} if the eventTypeDTO is not valid,
     * or with status {@code 404 (Not Found)} if the eventTypeDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the eventTypeDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/event-types/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public Mono<ResponseEntity<EventTypeDTO>> partialUpdateEventType(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody EventTypeDTO eventTypeDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update EventType partially : {}, {}", id, eventTypeDTO);
        if (eventTypeDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, eventTypeDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return eventTypeRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                Mono<EventTypeDTO> result = eventTypeService.partialUpdate(eventTypeDTO);

                return result
                    .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                    .map(res ->
                        ResponseEntity
                            .ok()
                            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, res.getId().toString()))
                            .body(res)
                    );
            });
    }

    /**
     * {@code GET  /event-types} : get all the eventTypes.
     *
     * @param pageable the pagination information.
     * @param request a {@link ServerHttpRequest} request.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of eventTypes in body.
     */
    @GetMapping("/event-types")
    public Mono<ResponseEntity<List<EventTypeDTO>>> getAllEventTypes(
        @org.springdoc.api.annotations.ParameterObject Pageable pageable,
        ServerHttpRequest request
    ) {
        log.debug("REST request to get a page of EventTypes");
        return eventTypeService
            .countAll()
            .zipWith(eventTypeService.findAll(pageable).collectList())
            .map(countWithEntities ->
                ResponseEntity
                    .ok()
                    .headers(
                        PaginationUtil.generatePaginationHttpHeaders(
                            UriComponentsBuilder.fromHttpRequest(request),
                            new PageImpl<>(countWithEntities.getT2(), pageable, countWithEntities.getT1())
                        )
                    )
                    .body(countWithEntities.getT2())
            );
    }

    /**
     * {@code GET  /event-types/:id} : get the "id" eventType.
     *
     * @param id the id of the eventTypeDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the eventTypeDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/event-types/{id}")
    public Mono<ResponseEntity<EventTypeDTO>> getEventType(@PathVariable Long id) {
        log.debug("REST request to get EventType : {}", id);
        Mono<EventTypeDTO> eventTypeDTO = eventTypeService.findOne(id);
        return ResponseUtil.wrapOrNotFound(eventTypeDTO);
    }

    /**
     * {@code DELETE  /event-types/:id} : delete the "id" eventType.
     *
     * @param id the id of the eventTypeDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/event-types/{id}")
    public Mono<ResponseEntity<Void>> deleteEventType(@PathVariable Long id) {
        log.debug("REST request to delete EventType : {}", id);
        return eventTypeService
            .delete(id)
            .then(
                Mono.just(
                    ResponseEntity
                        .noContent()
                        .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
                        .build()
                )
            );
    }

    /**
     * {@code SEARCH  /_search/event-types?query=:query} : search for the eventType corresponding
     * to the query.
     *
     * @param query the query of the eventType search.
     * @param pageable the pagination information.
     * @param request a {@link ServerHttpRequest} request.
     * @return the result of the search.
     */
    @GetMapping("/_search/event-types")
    public Mono<ResponseEntity<Flux<EventTypeDTO>>> searchEventTypes(
        @RequestParam String query,
        @org.springdoc.api.annotations.ParameterObject Pageable pageable,
        ServerHttpRequest request
    ) {
        log.debug("REST request to search for a page of EventTypes for query {}", query);
        return eventTypeService
            .searchCount()
            .map(total -> new PageImpl<>(new ArrayList<>(), pageable, total))
            .map(page -> PaginationUtil.generatePaginationHttpHeaders(UriComponentsBuilder.fromHttpRequest(request), page))
            .map(headers -> ResponseEntity.ok().headers(headers).body(eventTypeService.search(query, pageable)));
    }
}
