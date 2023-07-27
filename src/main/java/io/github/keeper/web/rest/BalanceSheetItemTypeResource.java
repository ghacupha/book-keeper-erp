package io.github.keeper.web.rest;

import io.github.keeper.repository.BalanceSheetItemTypeRepository;
import io.github.keeper.service.BalanceSheetItemTypeService;
import io.github.keeper.service.dto.BalanceSheetItemTypeDTO;
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
 * REST controller for managing {@link io.github.keeper.domain.BalanceSheetItemType}.
 */
@RestController
@RequestMapping("/api")
public class BalanceSheetItemTypeResource {

    private final Logger log = LoggerFactory.getLogger(BalanceSheetItemTypeResource.class);

    private static final String ENTITY_NAME = "balanceSheetItemType";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final BalanceSheetItemTypeService balanceSheetItemTypeService;

    private final BalanceSheetItemTypeRepository balanceSheetItemTypeRepository;

    public BalanceSheetItemTypeResource(
        BalanceSheetItemTypeService balanceSheetItemTypeService,
        BalanceSheetItemTypeRepository balanceSheetItemTypeRepository
    ) {
        this.balanceSheetItemTypeService = balanceSheetItemTypeService;
        this.balanceSheetItemTypeRepository = balanceSheetItemTypeRepository;
    }

    /**
     * {@code POST  /balance-sheet-item-types} : Create a new balanceSheetItemType.
     *
     * @param balanceSheetItemTypeDTO the balanceSheetItemTypeDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new balanceSheetItemTypeDTO, or with status {@code 400 (Bad Request)} if the balanceSheetItemType has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/balance-sheet-item-types")
    public Mono<ResponseEntity<BalanceSheetItemTypeDTO>> createBalanceSheetItemType(
        @Valid @RequestBody BalanceSheetItemTypeDTO balanceSheetItemTypeDTO
    ) throws URISyntaxException {
        log.debug("REST request to save BalanceSheetItemType : {}", balanceSheetItemTypeDTO);
        if (balanceSheetItemTypeDTO.getId() != null) {
            throw new BadRequestAlertException("A new balanceSheetItemType cannot already have an ID", ENTITY_NAME, "idexists");
        }
        return balanceSheetItemTypeService
            .save(balanceSheetItemTypeDTO)
            .map(result -> {
                try {
                    return ResponseEntity
                        .created(new URI("/api/balance-sheet-item-types/" + result.getId()))
                        .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
                        .body(result);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    /**
     * {@code PUT  /balance-sheet-item-types/:id} : Updates an existing balanceSheetItemType.
     *
     * @param id the id of the balanceSheetItemTypeDTO to save.
     * @param balanceSheetItemTypeDTO the balanceSheetItemTypeDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated balanceSheetItemTypeDTO,
     * or with status {@code 400 (Bad Request)} if the balanceSheetItemTypeDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the balanceSheetItemTypeDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/balance-sheet-item-types/{id}")
    public Mono<ResponseEntity<BalanceSheetItemTypeDTO>> updateBalanceSheetItemType(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody BalanceSheetItemTypeDTO balanceSheetItemTypeDTO
    ) throws URISyntaxException {
        log.debug("REST request to update BalanceSheetItemType : {}, {}", id, balanceSheetItemTypeDTO);
        if (balanceSheetItemTypeDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, balanceSheetItemTypeDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return balanceSheetItemTypeRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                return balanceSheetItemTypeService
                    .update(balanceSheetItemTypeDTO)
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
     * {@code PATCH  /balance-sheet-item-types/:id} : Partial updates given fields of an existing balanceSheetItemType, field will ignore if it is null
     *
     * @param id the id of the balanceSheetItemTypeDTO to save.
     * @param balanceSheetItemTypeDTO the balanceSheetItemTypeDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated balanceSheetItemTypeDTO,
     * or with status {@code 400 (Bad Request)} if the balanceSheetItemTypeDTO is not valid,
     * or with status {@code 404 (Not Found)} if the balanceSheetItemTypeDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the balanceSheetItemTypeDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/balance-sheet-item-types/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public Mono<ResponseEntity<BalanceSheetItemTypeDTO>> partialUpdateBalanceSheetItemType(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody BalanceSheetItemTypeDTO balanceSheetItemTypeDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update BalanceSheetItemType partially : {}, {}", id, balanceSheetItemTypeDTO);
        if (balanceSheetItemTypeDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, balanceSheetItemTypeDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return balanceSheetItemTypeRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                Mono<BalanceSheetItemTypeDTO> result = balanceSheetItemTypeService.partialUpdate(balanceSheetItemTypeDTO);

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
     * {@code GET  /balance-sheet-item-types} : get all the balanceSheetItemTypes.
     *
     * @param pageable the pagination information.
     * @param request a {@link ServerHttpRequest} request.
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of balanceSheetItemTypes in body.
     */
    @GetMapping("/balance-sheet-item-types")
    public Mono<ResponseEntity<List<BalanceSheetItemTypeDTO>>> getAllBalanceSheetItemTypes(
        @org.springdoc.api.annotations.ParameterObject Pageable pageable,
        ServerHttpRequest request,
        @RequestParam(required = false, defaultValue = "false") boolean eagerload
    ) {
        log.debug("REST request to get a page of BalanceSheetItemTypes");
        return balanceSheetItemTypeService
            .countAll()
            .zipWith(balanceSheetItemTypeService.findAll(pageable).collectList())
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
     * {@code GET  /balance-sheet-item-types/:id} : get the "id" balanceSheetItemType.
     *
     * @param id the id of the balanceSheetItemTypeDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the balanceSheetItemTypeDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/balance-sheet-item-types/{id}")
    public Mono<ResponseEntity<BalanceSheetItemTypeDTO>> getBalanceSheetItemType(@PathVariable Long id) {
        log.debug("REST request to get BalanceSheetItemType : {}", id);
        Mono<BalanceSheetItemTypeDTO> balanceSheetItemTypeDTO = balanceSheetItemTypeService.findOne(id);
        return ResponseUtil.wrapOrNotFound(balanceSheetItemTypeDTO);
    }

    /**
     * {@code DELETE  /balance-sheet-item-types/:id} : delete the "id" balanceSheetItemType.
     *
     * @param id the id of the balanceSheetItemTypeDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/balance-sheet-item-types/{id}")
    public Mono<ResponseEntity<Void>> deleteBalanceSheetItemType(@PathVariable Long id) {
        log.debug("REST request to delete BalanceSheetItemType : {}", id);
        return balanceSheetItemTypeService
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
     * {@code SEARCH  /_search/balance-sheet-item-types?query=:query} : search for the balanceSheetItemType corresponding
     * to the query.
     *
     * @param query the query of the balanceSheetItemType search.
     * @param pageable the pagination information.
     * @param request a {@link ServerHttpRequest} request.
     * @return the result of the search.
     */
    @GetMapping("/_search/balance-sheet-item-types")
    public Mono<ResponseEntity<Flux<BalanceSheetItemTypeDTO>>> searchBalanceSheetItemTypes(
        @RequestParam String query,
        @org.springdoc.api.annotations.ParameterObject Pageable pageable,
        ServerHttpRequest request
    ) {
        log.debug("REST request to search for a page of BalanceSheetItemTypes for query {}", query);
        return balanceSheetItemTypeService
            .searchCount()
            .map(total -> new PageImpl<>(new ArrayList<>(), pageable, total))
            .map(page -> PaginationUtil.generatePaginationHttpHeaders(UriComponentsBuilder.fromHttpRequest(request), page))
            .map(headers -> ResponseEntity.ok().headers(headers).body(balanceSheetItemTypeService.search(query, pageable)));
    }
}
