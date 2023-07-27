package io.github.keeper.web.rest;

import io.github.keeper.repository.TransactionAccountTypeRepository;
import io.github.keeper.service.TransactionAccountTypeService;
import io.github.keeper.service.dto.TransactionAccountTypeDTO;
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
 * REST controller for managing {@link io.github.keeper.domain.TransactionAccountType}.
 */
@RestController
@RequestMapping("/api")
public class TransactionAccountTypeResource {

    private final Logger log = LoggerFactory.getLogger(TransactionAccountTypeResource.class);

    private static final String ENTITY_NAME = "transactionAccountType";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final TransactionAccountTypeService transactionAccountTypeService;

    private final TransactionAccountTypeRepository transactionAccountTypeRepository;

    public TransactionAccountTypeResource(
        TransactionAccountTypeService transactionAccountTypeService,
        TransactionAccountTypeRepository transactionAccountTypeRepository
    ) {
        this.transactionAccountTypeService = transactionAccountTypeService;
        this.transactionAccountTypeRepository = transactionAccountTypeRepository;
    }

    /**
     * {@code POST  /transaction-account-types} : Create a new transactionAccountType.
     *
     * @param transactionAccountTypeDTO the transactionAccountTypeDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new transactionAccountTypeDTO, or with status {@code 400 (Bad Request)} if the transactionAccountType has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/transaction-account-types")
    public Mono<ResponseEntity<TransactionAccountTypeDTO>> createTransactionAccountType(
        @Valid @RequestBody TransactionAccountTypeDTO transactionAccountTypeDTO
    ) throws URISyntaxException {
        log.debug("REST request to save TransactionAccountType : {}", transactionAccountTypeDTO);
        if (transactionAccountTypeDTO.getId() != null) {
            throw new BadRequestAlertException("A new transactionAccountType cannot already have an ID", ENTITY_NAME, "idexists");
        }
        return transactionAccountTypeService
            .save(transactionAccountTypeDTO)
            .map(result -> {
                try {
                    return ResponseEntity
                        .created(new URI("/api/transaction-account-types/" + result.getId()))
                        .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
                        .body(result);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    /**
     * {@code PUT  /transaction-account-types/:id} : Updates an existing transactionAccountType.
     *
     * @param id the id of the transactionAccountTypeDTO to save.
     * @param transactionAccountTypeDTO the transactionAccountTypeDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated transactionAccountTypeDTO,
     * or with status {@code 400 (Bad Request)} if the transactionAccountTypeDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the transactionAccountTypeDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/transaction-account-types/{id}")
    public Mono<ResponseEntity<TransactionAccountTypeDTO>> updateTransactionAccountType(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody TransactionAccountTypeDTO transactionAccountTypeDTO
    ) throws URISyntaxException {
        log.debug("REST request to update TransactionAccountType : {}, {}", id, transactionAccountTypeDTO);
        if (transactionAccountTypeDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, transactionAccountTypeDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return transactionAccountTypeRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                return transactionAccountTypeService
                    .update(transactionAccountTypeDTO)
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
     * {@code PATCH  /transaction-account-types/:id} : Partial updates given fields of an existing transactionAccountType, field will ignore if it is null
     *
     * @param id the id of the transactionAccountTypeDTO to save.
     * @param transactionAccountTypeDTO the transactionAccountTypeDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated transactionAccountTypeDTO,
     * or with status {@code 400 (Bad Request)} if the transactionAccountTypeDTO is not valid,
     * or with status {@code 404 (Not Found)} if the transactionAccountTypeDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the transactionAccountTypeDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/transaction-account-types/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public Mono<ResponseEntity<TransactionAccountTypeDTO>> partialUpdateTransactionAccountType(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody TransactionAccountTypeDTO transactionAccountTypeDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update TransactionAccountType partially : {}, {}", id, transactionAccountTypeDTO);
        if (transactionAccountTypeDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, transactionAccountTypeDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return transactionAccountTypeRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                Mono<TransactionAccountTypeDTO> result = transactionAccountTypeService.partialUpdate(transactionAccountTypeDTO);

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
     * {@code GET  /transaction-account-types} : get all the transactionAccountTypes.
     *
     * @param pageable the pagination information.
     * @param request a {@link ServerHttpRequest} request.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of transactionAccountTypes in body.
     */
    @GetMapping("/transaction-account-types")
    public Mono<ResponseEntity<List<TransactionAccountTypeDTO>>> getAllTransactionAccountTypes(
        @org.springdoc.api.annotations.ParameterObject Pageable pageable,
        ServerHttpRequest request
    ) {
        log.debug("REST request to get a page of TransactionAccountTypes");
        return transactionAccountTypeService
            .countAll()
            .zipWith(transactionAccountTypeService.findAll(pageable).collectList())
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
     * {@code GET  /transaction-account-types/:id} : get the "id" transactionAccountType.
     *
     * @param id the id of the transactionAccountTypeDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the transactionAccountTypeDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/transaction-account-types/{id}")
    public Mono<ResponseEntity<TransactionAccountTypeDTO>> getTransactionAccountType(@PathVariable Long id) {
        log.debug("REST request to get TransactionAccountType : {}", id);
        Mono<TransactionAccountTypeDTO> transactionAccountTypeDTO = transactionAccountTypeService.findOne(id);
        return ResponseUtil.wrapOrNotFound(transactionAccountTypeDTO);
    }

    /**
     * {@code DELETE  /transaction-account-types/:id} : delete the "id" transactionAccountType.
     *
     * @param id the id of the transactionAccountTypeDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/transaction-account-types/{id}")
    public Mono<ResponseEntity<Void>> deleteTransactionAccountType(@PathVariable Long id) {
        log.debug("REST request to delete TransactionAccountType : {}", id);
        return transactionAccountTypeService
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
     * {@code SEARCH  /_search/transaction-account-types?query=:query} : search for the transactionAccountType corresponding
     * to the query.
     *
     * @param query the query of the transactionAccountType search.
     * @param pageable the pagination information.
     * @param request a {@link ServerHttpRequest} request.
     * @return the result of the search.
     */
    @GetMapping("/_search/transaction-account-types")
    public Mono<ResponseEntity<Flux<TransactionAccountTypeDTO>>> searchTransactionAccountTypes(
        @RequestParam String query,
        @org.springdoc.api.annotations.ParameterObject Pageable pageable,
        ServerHttpRequest request
    ) {
        log.debug("REST request to search for a page of TransactionAccountTypes for query {}", query);
        return transactionAccountTypeService
            .searchCount()
            .map(total -> new PageImpl<>(new ArrayList<>(), pageable, total))
            .map(page -> PaginationUtil.generatePaginationHttpHeaders(UriComponentsBuilder.fromHttpRequest(request), page))
            .map(headers -> ResponseEntity.ok().headers(headers).body(transactionAccountTypeService.search(query, pageable)));
    }
}
