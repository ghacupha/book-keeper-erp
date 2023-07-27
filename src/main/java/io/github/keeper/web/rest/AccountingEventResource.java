package io.github.keeper.web.rest;

import io.github.keeper.repository.AccountingEventRepository;
import io.github.keeper.service.AccountingEventService;
import io.github.keeper.service.dto.AccountingEventDTO;
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
 * REST controller for managing {@link io.github.keeper.domain.AccountingEvent}.
 */
@RestController
@RequestMapping("/api")
public class AccountingEventResource {

    private final Logger log = LoggerFactory.getLogger(AccountingEventResource.class);

    private static final String ENTITY_NAME = "accountingEvent";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final AccountingEventService accountingEventService;

    private final AccountingEventRepository accountingEventRepository;

    public AccountingEventResource(AccountingEventService accountingEventService, AccountingEventRepository accountingEventRepository) {
        this.accountingEventService = accountingEventService;
        this.accountingEventRepository = accountingEventRepository;
    }

    /**
     * {@code POST  /accounting-events} : Create a new accountingEvent.
     *
     * @param accountingEventDTO the accountingEventDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new accountingEventDTO, or with status {@code 400 (Bad Request)} if the accountingEvent has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/accounting-events")
    public Mono<ResponseEntity<AccountingEventDTO>> createAccountingEvent(@Valid @RequestBody AccountingEventDTO accountingEventDTO)
        throws URISyntaxException {
        log.debug("REST request to save AccountingEvent : {}", accountingEventDTO);
        if (accountingEventDTO.getId() != null) {
            throw new BadRequestAlertException("A new accountingEvent cannot already have an ID", ENTITY_NAME, "idexists");
        }
        return accountingEventService
            .save(accountingEventDTO)
            .map(result -> {
                try {
                    return ResponseEntity
                        .created(new URI("/api/accounting-events/" + result.getId()))
                        .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
                        .body(result);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    /**
     * {@code PUT  /accounting-events/:id} : Updates an existing accountingEvent.
     *
     * @param id the id of the accountingEventDTO to save.
     * @param accountingEventDTO the accountingEventDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated accountingEventDTO,
     * or with status {@code 400 (Bad Request)} if the accountingEventDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the accountingEventDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/accounting-events/{id}")
    public Mono<ResponseEntity<AccountingEventDTO>> updateAccountingEvent(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody AccountingEventDTO accountingEventDTO
    ) throws URISyntaxException {
        log.debug("REST request to update AccountingEvent : {}, {}", id, accountingEventDTO);
        if (accountingEventDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, accountingEventDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return accountingEventRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                return accountingEventService
                    .update(accountingEventDTO)
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
     * {@code PATCH  /accounting-events/:id} : Partial updates given fields of an existing accountingEvent, field will ignore if it is null
     *
     * @param id the id of the accountingEventDTO to save.
     * @param accountingEventDTO the accountingEventDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated accountingEventDTO,
     * or with status {@code 400 (Bad Request)} if the accountingEventDTO is not valid,
     * or with status {@code 404 (Not Found)} if the accountingEventDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the accountingEventDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/accounting-events/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public Mono<ResponseEntity<AccountingEventDTO>> partialUpdateAccountingEvent(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody AccountingEventDTO accountingEventDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update AccountingEvent partially : {}, {}", id, accountingEventDTO);
        if (accountingEventDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, accountingEventDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return accountingEventRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                Mono<AccountingEventDTO> result = accountingEventService.partialUpdate(accountingEventDTO);

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
     * {@code GET  /accounting-events} : get all the accountingEvents.
     *
     * @param pageable the pagination information.
     * @param request a {@link ServerHttpRequest} request.
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of accountingEvents in body.
     */
    @GetMapping("/accounting-events")
    public Mono<ResponseEntity<List<AccountingEventDTO>>> getAllAccountingEvents(
        @org.springdoc.api.annotations.ParameterObject Pageable pageable,
        ServerHttpRequest request,
        @RequestParam(required = false, defaultValue = "false") boolean eagerload
    ) {
        log.debug("REST request to get a page of AccountingEvents");
        return accountingEventService
            .countAll()
            .zipWith(accountingEventService.findAll(pageable).collectList())
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
     * {@code GET  /accounting-events/:id} : get the "id" accountingEvent.
     *
     * @param id the id of the accountingEventDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the accountingEventDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/accounting-events/{id}")
    public Mono<ResponseEntity<AccountingEventDTO>> getAccountingEvent(@PathVariable Long id) {
        log.debug("REST request to get AccountingEvent : {}", id);
        Mono<AccountingEventDTO> accountingEventDTO = accountingEventService.findOne(id);
        return ResponseUtil.wrapOrNotFound(accountingEventDTO);
    }

    /**
     * {@code DELETE  /accounting-events/:id} : delete the "id" accountingEvent.
     *
     * @param id the id of the accountingEventDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/accounting-events/{id}")
    public Mono<ResponseEntity<Void>> deleteAccountingEvent(@PathVariable Long id) {
        log.debug("REST request to delete AccountingEvent : {}", id);
        return accountingEventService
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
     * {@code SEARCH  /_search/accounting-events?query=:query} : search for the accountingEvent corresponding
     * to the query.
     *
     * @param query the query of the accountingEvent search.
     * @param pageable the pagination information.
     * @param request a {@link ServerHttpRequest} request.
     * @return the result of the search.
     */
    @GetMapping("/_search/accounting-events")
    public Mono<ResponseEntity<Flux<AccountingEventDTO>>> searchAccountingEvents(
        @RequestParam String query,
        @org.springdoc.api.annotations.ParameterObject Pageable pageable,
        ServerHttpRequest request
    ) {
        log.debug("REST request to search for a page of AccountingEvents for query {}", query);
        return accountingEventService
            .searchCount()
            .map(total -> new PageImpl<>(new ArrayList<>(), pageable, total))
            .map(page -> PaginationUtil.generatePaginationHttpHeaders(UriComponentsBuilder.fromHttpRequest(request), page))
            .map(headers -> ResponseEntity.ok().headers(headers).body(accountingEventService.search(query, pageable)));
    }
}
