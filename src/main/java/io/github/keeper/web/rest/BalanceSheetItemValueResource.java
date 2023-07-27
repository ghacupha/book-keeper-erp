package io.github.keeper.web.rest;

import io.github.keeper.repository.BalanceSheetItemValueRepository;
import io.github.keeper.service.BalanceSheetItemValueService;
import io.github.keeper.service.dto.BalanceSheetItemValueDTO;
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
 * REST controller for managing {@link io.github.keeper.domain.BalanceSheetItemValue}.
 */
@RestController
@RequestMapping("/api")
public class BalanceSheetItemValueResource {

    private final Logger log = LoggerFactory.getLogger(BalanceSheetItemValueResource.class);

    private static final String ENTITY_NAME = "balanceSheetItemValue";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final BalanceSheetItemValueService balanceSheetItemValueService;

    private final BalanceSheetItemValueRepository balanceSheetItemValueRepository;

    public BalanceSheetItemValueResource(
        BalanceSheetItemValueService balanceSheetItemValueService,
        BalanceSheetItemValueRepository balanceSheetItemValueRepository
    ) {
        this.balanceSheetItemValueService = balanceSheetItemValueService;
        this.balanceSheetItemValueRepository = balanceSheetItemValueRepository;
    }

    /**
     * {@code POST  /balance-sheet-item-values} : Create a new balanceSheetItemValue.
     *
     * @param balanceSheetItemValueDTO the balanceSheetItemValueDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new balanceSheetItemValueDTO, or with status {@code 400 (Bad Request)} if the balanceSheetItemValue has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/balance-sheet-item-values")
    public Mono<ResponseEntity<BalanceSheetItemValueDTO>> createBalanceSheetItemValue(
        @Valid @RequestBody BalanceSheetItemValueDTO balanceSheetItemValueDTO
    ) throws URISyntaxException {
        log.debug("REST request to save BalanceSheetItemValue : {}", balanceSheetItemValueDTO);
        if (balanceSheetItemValueDTO.getId() != null) {
            throw new BadRequestAlertException("A new balanceSheetItemValue cannot already have an ID", ENTITY_NAME, "idexists");
        }
        return balanceSheetItemValueService
            .save(balanceSheetItemValueDTO)
            .map(result -> {
                try {
                    return ResponseEntity
                        .created(new URI("/api/balance-sheet-item-values/" + result.getId()))
                        .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
                        .body(result);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    /**
     * {@code PUT  /balance-sheet-item-values/:id} : Updates an existing balanceSheetItemValue.
     *
     * @param id the id of the balanceSheetItemValueDTO to save.
     * @param balanceSheetItemValueDTO the balanceSheetItemValueDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated balanceSheetItemValueDTO,
     * or with status {@code 400 (Bad Request)} if the balanceSheetItemValueDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the balanceSheetItemValueDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/balance-sheet-item-values/{id}")
    public Mono<ResponseEntity<BalanceSheetItemValueDTO>> updateBalanceSheetItemValue(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody BalanceSheetItemValueDTO balanceSheetItemValueDTO
    ) throws URISyntaxException {
        log.debug("REST request to update BalanceSheetItemValue : {}, {}", id, balanceSheetItemValueDTO);
        if (balanceSheetItemValueDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, balanceSheetItemValueDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return balanceSheetItemValueRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                return balanceSheetItemValueService
                    .update(balanceSheetItemValueDTO)
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
     * {@code PATCH  /balance-sheet-item-values/:id} : Partial updates given fields of an existing balanceSheetItemValue, field will ignore if it is null
     *
     * @param id the id of the balanceSheetItemValueDTO to save.
     * @param balanceSheetItemValueDTO the balanceSheetItemValueDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated balanceSheetItemValueDTO,
     * or with status {@code 400 (Bad Request)} if the balanceSheetItemValueDTO is not valid,
     * or with status {@code 404 (Not Found)} if the balanceSheetItemValueDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the balanceSheetItemValueDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/balance-sheet-item-values/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public Mono<ResponseEntity<BalanceSheetItemValueDTO>> partialUpdateBalanceSheetItemValue(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody BalanceSheetItemValueDTO balanceSheetItemValueDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update BalanceSheetItemValue partially : {}, {}", id, balanceSheetItemValueDTO);
        if (balanceSheetItemValueDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, balanceSheetItemValueDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return balanceSheetItemValueRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                Mono<BalanceSheetItemValueDTO> result = balanceSheetItemValueService.partialUpdate(balanceSheetItemValueDTO);

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
     * {@code GET  /balance-sheet-item-values} : get all the balanceSheetItemValues.
     *
     * @param pageable the pagination information.
     * @param request a {@link ServerHttpRequest} request.
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of balanceSheetItemValues in body.
     */
    @GetMapping("/balance-sheet-item-values")
    public Mono<ResponseEntity<List<BalanceSheetItemValueDTO>>> getAllBalanceSheetItemValues(
        @org.springdoc.api.annotations.ParameterObject Pageable pageable,
        ServerHttpRequest request,
        @RequestParam(required = false, defaultValue = "false") boolean eagerload
    ) {
        log.debug("REST request to get a page of BalanceSheetItemValues");
        return balanceSheetItemValueService
            .countAll()
            .zipWith(balanceSheetItemValueService.findAll(pageable).collectList())
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
     * {@code GET  /balance-sheet-item-values/:id} : get the "id" balanceSheetItemValue.
     *
     * @param id the id of the balanceSheetItemValueDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the balanceSheetItemValueDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/balance-sheet-item-values/{id}")
    public Mono<ResponseEntity<BalanceSheetItemValueDTO>> getBalanceSheetItemValue(@PathVariable Long id) {
        log.debug("REST request to get BalanceSheetItemValue : {}", id);
        Mono<BalanceSheetItemValueDTO> balanceSheetItemValueDTO = balanceSheetItemValueService.findOne(id);
        return ResponseUtil.wrapOrNotFound(balanceSheetItemValueDTO);
    }

    /**
     * {@code DELETE  /balance-sheet-item-values/:id} : delete the "id" balanceSheetItemValue.
     *
     * @param id the id of the balanceSheetItemValueDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/balance-sheet-item-values/{id}")
    public Mono<ResponseEntity<Void>> deleteBalanceSheetItemValue(@PathVariable Long id) {
        log.debug("REST request to delete BalanceSheetItemValue : {}", id);
        return balanceSheetItemValueService
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
     * {@code SEARCH  /_search/balance-sheet-item-values?query=:query} : search for the balanceSheetItemValue corresponding
     * to the query.
     *
     * @param query the query of the balanceSheetItemValue search.
     * @param pageable the pagination information.
     * @param request a {@link ServerHttpRequest} request.
     * @return the result of the search.
     */
    @GetMapping("/_search/balance-sheet-item-values")
    public Mono<ResponseEntity<Flux<BalanceSheetItemValueDTO>>> searchBalanceSheetItemValues(
        @RequestParam String query,
        @org.springdoc.api.annotations.ParameterObject Pageable pageable,
        ServerHttpRequest request
    ) {
        log.debug("REST request to search for a page of BalanceSheetItemValues for query {}", query);
        return balanceSheetItemValueService
            .searchCount()
            .map(total -> new PageImpl<>(new ArrayList<>(), pageable, total))
            .map(page -> PaginationUtil.generatePaginationHttpHeaders(UriComponentsBuilder.fromHttpRequest(request), page))
            .map(headers -> ResponseEntity.ok().headers(headers).body(balanceSheetItemValueService.search(query, pageable)));
    }
}
