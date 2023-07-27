package io.github.keeper.web.rest;

import io.github.keeper.repository.TransactionAccountRepository;
import io.github.keeper.service.TransactionAccountService;
import io.github.keeper.service.dto.TransactionAccountDTO;
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
 * REST controller for managing {@link io.github.keeper.domain.TransactionAccount}.
 */
@RestController
@RequestMapping("/api")
public class TransactionAccountResource {

    private final Logger log = LoggerFactory.getLogger(TransactionAccountResource.class);

    private static final String ENTITY_NAME = "transactionAccount";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final TransactionAccountService transactionAccountService;

    private final TransactionAccountRepository transactionAccountRepository;

    public TransactionAccountResource(
        TransactionAccountService transactionAccountService,
        TransactionAccountRepository transactionAccountRepository
    ) {
        this.transactionAccountService = transactionAccountService;
        this.transactionAccountRepository = transactionAccountRepository;
    }

    /**
     * {@code POST  /transaction-accounts} : Create a new transactionAccount.
     *
     * @param transactionAccountDTO the transactionAccountDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new transactionAccountDTO, or with status {@code 400 (Bad Request)} if the transactionAccount has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/transaction-accounts")
    public Mono<ResponseEntity<TransactionAccountDTO>> createTransactionAccount(
        @Valid @RequestBody TransactionAccountDTO transactionAccountDTO
    ) throws URISyntaxException {
        log.debug("REST request to save TransactionAccount : {}", transactionAccountDTO);
        if (transactionAccountDTO.getId() != null) {
            throw new BadRequestAlertException("A new transactionAccount cannot already have an ID", ENTITY_NAME, "idexists");
        }
        return transactionAccountService
            .save(transactionAccountDTO)
            .map(result -> {
                try {
                    return ResponseEntity
                        .created(new URI("/api/transaction-accounts/" + result.getId()))
                        .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
                        .body(result);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    /**
     * {@code PUT  /transaction-accounts/:id} : Updates an existing transactionAccount.
     *
     * @param id the id of the transactionAccountDTO to save.
     * @param transactionAccountDTO the transactionAccountDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated transactionAccountDTO,
     * or with status {@code 400 (Bad Request)} if the transactionAccountDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the transactionAccountDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/transaction-accounts/{id}")
    public Mono<ResponseEntity<TransactionAccountDTO>> updateTransactionAccount(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody TransactionAccountDTO transactionAccountDTO
    ) throws URISyntaxException {
        log.debug("REST request to update TransactionAccount : {}, {}", id, transactionAccountDTO);
        if (transactionAccountDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, transactionAccountDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return transactionAccountRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                return transactionAccountService
                    .update(transactionAccountDTO)
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
     * {@code PATCH  /transaction-accounts/:id} : Partial updates given fields of an existing transactionAccount, field will ignore if it is null
     *
     * @param id the id of the transactionAccountDTO to save.
     * @param transactionAccountDTO the transactionAccountDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated transactionAccountDTO,
     * or with status {@code 400 (Bad Request)} if the transactionAccountDTO is not valid,
     * or with status {@code 404 (Not Found)} if the transactionAccountDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the transactionAccountDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/transaction-accounts/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public Mono<ResponseEntity<TransactionAccountDTO>> partialUpdateTransactionAccount(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody TransactionAccountDTO transactionAccountDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update TransactionAccount partially : {}, {}", id, transactionAccountDTO);
        if (transactionAccountDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, transactionAccountDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return transactionAccountRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                Mono<TransactionAccountDTO> result = transactionAccountService.partialUpdate(transactionAccountDTO);

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
     * {@code GET  /transaction-accounts} : get all the transactionAccounts.
     *
     * @param pageable the pagination information.
     * @param request a {@link ServerHttpRequest} request.
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of transactionAccounts in body.
     */
    @GetMapping("/transaction-accounts")
    public Mono<ResponseEntity<List<TransactionAccountDTO>>> getAllTransactionAccounts(
        @org.springdoc.api.annotations.ParameterObject Pageable pageable,
        ServerHttpRequest request,
        @RequestParam(required = false, defaultValue = "false") boolean eagerload
    ) {
        log.debug("REST request to get a page of TransactionAccounts");
        return transactionAccountService
            .countAll()
            .zipWith(transactionAccountService.findAll(pageable).collectList())
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
     * {@code GET  /transaction-accounts/:id} : get the "id" transactionAccount.
     *
     * @param id the id of the transactionAccountDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the transactionAccountDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/transaction-accounts/{id}")
    public Mono<ResponseEntity<TransactionAccountDTO>> getTransactionAccount(@PathVariable Long id) {
        log.debug("REST request to get TransactionAccount : {}", id);
        Mono<TransactionAccountDTO> transactionAccountDTO = transactionAccountService.findOne(id);
        return ResponseUtil.wrapOrNotFound(transactionAccountDTO);
    }

    /**
     * {@code DELETE  /transaction-accounts/:id} : delete the "id" transactionAccount.
     *
     * @param id the id of the transactionAccountDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/transaction-accounts/{id}")
    public Mono<ResponseEntity<Void>> deleteTransactionAccount(@PathVariable Long id) {
        log.debug("REST request to delete TransactionAccount : {}", id);
        return transactionAccountService
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
     * {@code SEARCH  /_search/transaction-accounts?query=:query} : search for the transactionAccount corresponding
     * to the query.
     *
     * @param query the query of the transactionAccount search.
     * @param pageable the pagination information.
     * @param request a {@link ServerHttpRequest} request.
     * @return the result of the search.
     */
    @GetMapping("/_search/transaction-accounts")
    public Mono<ResponseEntity<Flux<TransactionAccountDTO>>> searchTransactionAccounts(
        @RequestParam String query,
        @org.springdoc.api.annotations.ParameterObject Pageable pageable,
        ServerHttpRequest request
    ) {
        log.debug("REST request to search for a page of TransactionAccounts for query {}", query);
        return transactionAccountService
            .searchCount()
            .map(total -> new PageImpl<>(new ArrayList<>(), pageable, total))
            .map(page -> PaginationUtil.generatePaginationHttpHeaders(UriComponentsBuilder.fromHttpRequest(request), page))
            .map(headers -> ResponseEntity.ok().headers(headers).body(transactionAccountService.search(query, pageable)));
    }
}
