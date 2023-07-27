package io.github.keeper.web.rest;

import io.github.keeper.repository.TransactionEntryRepository;
import io.github.keeper.service.TransactionEntryService;
import io.github.keeper.service.dto.TransactionEntryDTO;
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
 * REST controller for managing {@link io.github.keeper.domain.TransactionEntry}.
 */
@RestController
@RequestMapping("/api")
public class TransactionEntryResource {

    private final Logger log = LoggerFactory.getLogger(TransactionEntryResource.class);

    private static final String ENTITY_NAME = "transactionEntry";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final TransactionEntryService transactionEntryService;

    private final TransactionEntryRepository transactionEntryRepository;

    public TransactionEntryResource(
        TransactionEntryService transactionEntryService,
        TransactionEntryRepository transactionEntryRepository
    ) {
        this.transactionEntryService = transactionEntryService;
        this.transactionEntryRepository = transactionEntryRepository;
    }

    /**
     * {@code POST  /transaction-entries} : Create a new transactionEntry.
     *
     * @param transactionEntryDTO the transactionEntryDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new transactionEntryDTO, or with status {@code 400 (Bad Request)} if the transactionEntry has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/transaction-entries")
    public Mono<ResponseEntity<TransactionEntryDTO>> createTransactionEntry(@Valid @RequestBody TransactionEntryDTO transactionEntryDTO)
        throws URISyntaxException {
        log.debug("REST request to save TransactionEntry : {}", transactionEntryDTO);
        if (transactionEntryDTO.getId() != null) {
            throw new BadRequestAlertException("A new transactionEntry cannot already have an ID", ENTITY_NAME, "idexists");
        }
        return transactionEntryService
            .save(transactionEntryDTO)
            .map(result -> {
                try {
                    return ResponseEntity
                        .created(new URI("/api/transaction-entries/" + result.getId()))
                        .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
                        .body(result);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    /**
     * {@code PUT  /transaction-entries/:id} : Updates an existing transactionEntry.
     *
     * @param id the id of the transactionEntryDTO to save.
     * @param transactionEntryDTO the transactionEntryDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated transactionEntryDTO,
     * or with status {@code 400 (Bad Request)} if the transactionEntryDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the transactionEntryDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/transaction-entries/{id}")
    public Mono<ResponseEntity<TransactionEntryDTO>> updateTransactionEntry(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody TransactionEntryDTO transactionEntryDTO
    ) throws URISyntaxException {
        log.debug("REST request to update TransactionEntry : {}, {}", id, transactionEntryDTO);
        if (transactionEntryDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, transactionEntryDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return transactionEntryRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                return transactionEntryService
                    .update(transactionEntryDTO)
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
     * {@code PATCH  /transaction-entries/:id} : Partial updates given fields of an existing transactionEntry, field will ignore if it is null
     *
     * @param id the id of the transactionEntryDTO to save.
     * @param transactionEntryDTO the transactionEntryDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated transactionEntryDTO,
     * or with status {@code 400 (Bad Request)} if the transactionEntryDTO is not valid,
     * or with status {@code 404 (Not Found)} if the transactionEntryDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the transactionEntryDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/transaction-entries/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public Mono<ResponseEntity<TransactionEntryDTO>> partialUpdateTransactionEntry(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody TransactionEntryDTO transactionEntryDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update TransactionEntry partially : {}, {}", id, transactionEntryDTO);
        if (transactionEntryDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, transactionEntryDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return transactionEntryRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                Mono<TransactionEntryDTO> result = transactionEntryService.partialUpdate(transactionEntryDTO);

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
     * {@code GET  /transaction-entries} : get all the transactionEntries.
     *
     * @param pageable the pagination information.
     * @param request a {@link ServerHttpRequest} request.
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of transactionEntries in body.
     */
    @GetMapping("/transaction-entries")
    public Mono<ResponseEntity<List<TransactionEntryDTO>>> getAllTransactionEntries(
        @org.springdoc.api.annotations.ParameterObject Pageable pageable,
        ServerHttpRequest request,
        @RequestParam(required = false, defaultValue = "false") boolean eagerload
    ) {
        log.debug("REST request to get a page of TransactionEntries");
        return transactionEntryService
            .countAll()
            .zipWith(transactionEntryService.findAll(pageable).collectList())
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
     * {@code GET  /transaction-entries/:id} : get the "id" transactionEntry.
     *
     * @param id the id of the transactionEntryDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the transactionEntryDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/transaction-entries/{id}")
    public Mono<ResponseEntity<TransactionEntryDTO>> getTransactionEntry(@PathVariable Long id) {
        log.debug("REST request to get TransactionEntry : {}", id);
        Mono<TransactionEntryDTO> transactionEntryDTO = transactionEntryService.findOne(id);
        return ResponseUtil.wrapOrNotFound(transactionEntryDTO);
    }

    /**
     * {@code DELETE  /transaction-entries/:id} : delete the "id" transactionEntry.
     *
     * @param id the id of the transactionEntryDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/transaction-entries/{id}")
    public Mono<ResponseEntity<Void>> deleteTransactionEntry(@PathVariable Long id) {
        log.debug("REST request to delete TransactionEntry : {}", id);
        return transactionEntryService
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
     * {@code SEARCH  /_search/transaction-entries?query=:query} : search for the transactionEntry corresponding
     * to the query.
     *
     * @param query the query of the transactionEntry search.
     * @param pageable the pagination information.
     * @param request a {@link ServerHttpRequest} request.
     * @return the result of the search.
     */
    @GetMapping("/_search/transaction-entries")
    public Mono<ResponseEntity<Flux<TransactionEntryDTO>>> searchTransactionEntries(
        @RequestParam String query,
        @org.springdoc.api.annotations.ParameterObject Pageable pageable,
        ServerHttpRequest request
    ) {
        log.debug("REST request to search for a page of TransactionEntries for query {}", query);
        return transactionEntryService
            .searchCount()
            .map(total -> new PageImpl<>(new ArrayList<>(), pageable, total))
            .map(page -> PaginationUtil.generatePaginationHttpHeaders(UriComponentsBuilder.fromHttpRequest(request), page))
            .map(headers -> ResponseEntity.ok().headers(headers).body(transactionEntryService.search(query, pageable)));
    }
}
