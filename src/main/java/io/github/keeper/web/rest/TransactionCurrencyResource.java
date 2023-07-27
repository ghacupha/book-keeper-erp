package io.github.keeper.web.rest;

import io.github.keeper.repository.TransactionCurrencyRepository;
import io.github.keeper.service.TransactionCurrencyService;
import io.github.keeper.service.dto.TransactionCurrencyDTO;
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
 * REST controller for managing {@link io.github.keeper.domain.TransactionCurrency}.
 */
@RestController
@RequestMapping("/api")
public class TransactionCurrencyResource {

    private final Logger log = LoggerFactory.getLogger(TransactionCurrencyResource.class);

    private static final String ENTITY_NAME = "transactionCurrency";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final TransactionCurrencyService transactionCurrencyService;

    private final TransactionCurrencyRepository transactionCurrencyRepository;

    public TransactionCurrencyResource(
        TransactionCurrencyService transactionCurrencyService,
        TransactionCurrencyRepository transactionCurrencyRepository
    ) {
        this.transactionCurrencyService = transactionCurrencyService;
        this.transactionCurrencyRepository = transactionCurrencyRepository;
    }

    /**
     * {@code POST  /transaction-currencies} : Create a new transactionCurrency.
     *
     * @param transactionCurrencyDTO the transactionCurrencyDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new transactionCurrencyDTO, or with status {@code 400 (Bad Request)} if the transactionCurrency has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/transaction-currencies")
    public Mono<ResponseEntity<TransactionCurrencyDTO>> createTransactionCurrency(
        @Valid @RequestBody TransactionCurrencyDTO transactionCurrencyDTO
    ) throws URISyntaxException {
        log.debug("REST request to save TransactionCurrency : {}", transactionCurrencyDTO);
        if (transactionCurrencyDTO.getId() != null) {
            throw new BadRequestAlertException("A new transactionCurrency cannot already have an ID", ENTITY_NAME, "idexists");
        }
        return transactionCurrencyService
            .save(transactionCurrencyDTO)
            .map(result -> {
                try {
                    return ResponseEntity
                        .created(new URI("/api/transaction-currencies/" + result.getId()))
                        .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
                        .body(result);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    /**
     * {@code PUT  /transaction-currencies/:id} : Updates an existing transactionCurrency.
     *
     * @param id the id of the transactionCurrencyDTO to save.
     * @param transactionCurrencyDTO the transactionCurrencyDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated transactionCurrencyDTO,
     * or with status {@code 400 (Bad Request)} if the transactionCurrencyDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the transactionCurrencyDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/transaction-currencies/{id}")
    public Mono<ResponseEntity<TransactionCurrencyDTO>> updateTransactionCurrency(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody TransactionCurrencyDTO transactionCurrencyDTO
    ) throws URISyntaxException {
        log.debug("REST request to update TransactionCurrency : {}, {}", id, transactionCurrencyDTO);
        if (transactionCurrencyDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, transactionCurrencyDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return transactionCurrencyRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                return transactionCurrencyService
                    .update(transactionCurrencyDTO)
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
     * {@code PATCH  /transaction-currencies/:id} : Partial updates given fields of an existing transactionCurrency, field will ignore if it is null
     *
     * @param id the id of the transactionCurrencyDTO to save.
     * @param transactionCurrencyDTO the transactionCurrencyDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated transactionCurrencyDTO,
     * or with status {@code 400 (Bad Request)} if the transactionCurrencyDTO is not valid,
     * or with status {@code 404 (Not Found)} if the transactionCurrencyDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the transactionCurrencyDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/transaction-currencies/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public Mono<ResponseEntity<TransactionCurrencyDTO>> partialUpdateTransactionCurrency(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody TransactionCurrencyDTO transactionCurrencyDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update TransactionCurrency partially : {}, {}", id, transactionCurrencyDTO);
        if (transactionCurrencyDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, transactionCurrencyDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return transactionCurrencyRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                Mono<TransactionCurrencyDTO> result = transactionCurrencyService.partialUpdate(transactionCurrencyDTO);

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
     * {@code GET  /transaction-currencies} : get all the transactionCurrencies.
     *
     * @param pageable the pagination information.
     * @param request a {@link ServerHttpRequest} request.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of transactionCurrencies in body.
     */
    @GetMapping("/transaction-currencies")
    public Mono<ResponseEntity<List<TransactionCurrencyDTO>>> getAllTransactionCurrencies(
        @org.springdoc.api.annotations.ParameterObject Pageable pageable,
        ServerHttpRequest request
    ) {
        log.debug("REST request to get a page of TransactionCurrencies");
        return transactionCurrencyService
            .countAll()
            .zipWith(transactionCurrencyService.findAll(pageable).collectList())
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
     * {@code GET  /transaction-currencies/:id} : get the "id" transactionCurrency.
     *
     * @param id the id of the transactionCurrencyDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the transactionCurrencyDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/transaction-currencies/{id}")
    public Mono<ResponseEntity<TransactionCurrencyDTO>> getTransactionCurrency(@PathVariable Long id) {
        log.debug("REST request to get TransactionCurrency : {}", id);
        Mono<TransactionCurrencyDTO> transactionCurrencyDTO = transactionCurrencyService.findOne(id);
        return ResponseUtil.wrapOrNotFound(transactionCurrencyDTO);
    }

    /**
     * {@code DELETE  /transaction-currencies/:id} : delete the "id" transactionCurrency.
     *
     * @param id the id of the transactionCurrencyDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/transaction-currencies/{id}")
    public Mono<ResponseEntity<Void>> deleteTransactionCurrency(@PathVariable Long id) {
        log.debug("REST request to delete TransactionCurrency : {}", id);
        return transactionCurrencyService
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
     * {@code SEARCH  /_search/transaction-currencies?query=:query} : search for the transactionCurrency corresponding
     * to the query.
     *
     * @param query the query of the transactionCurrency search.
     * @param pageable the pagination information.
     * @param request a {@link ServerHttpRequest} request.
     * @return the result of the search.
     */
    @GetMapping("/_search/transaction-currencies")
    public Mono<ResponseEntity<Flux<TransactionCurrencyDTO>>> searchTransactionCurrencies(
        @RequestParam String query,
        @org.springdoc.api.annotations.ParameterObject Pageable pageable,
        ServerHttpRequest request
    ) {
        log.debug("REST request to search for a page of TransactionCurrencies for query {}", query);
        return transactionCurrencyService
            .searchCount()
            .map(total -> new PageImpl<>(new ArrayList<>(), pageable, total))
            .map(page -> PaginationUtil.generatePaginationHttpHeaders(UriComponentsBuilder.fromHttpRequest(request), page))
            .map(headers -> ResponseEntity.ok().headers(headers).body(transactionCurrencyService.search(query, pageable)));
    }
}
