package io.github.keeper.web.rest;

import io.github.keeper.repository.DealerTypeRepository;
import io.github.keeper.service.DealerTypeService;
import io.github.keeper.service.dto.DealerTypeDTO;
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
 * REST controller for managing {@link io.github.keeper.domain.DealerType}.
 */
@RestController
@RequestMapping("/api")
public class DealerTypeResource {

    private final Logger log = LoggerFactory.getLogger(DealerTypeResource.class);

    private static final String ENTITY_NAME = "dealerType";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final DealerTypeService dealerTypeService;

    private final DealerTypeRepository dealerTypeRepository;

    public DealerTypeResource(DealerTypeService dealerTypeService, DealerTypeRepository dealerTypeRepository) {
        this.dealerTypeService = dealerTypeService;
        this.dealerTypeRepository = dealerTypeRepository;
    }

    /**
     * {@code POST  /dealer-types} : Create a new dealerType.
     *
     * @param dealerTypeDTO the dealerTypeDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new dealerTypeDTO, or with status {@code 400 (Bad Request)} if the dealerType has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/dealer-types")
    public Mono<ResponseEntity<DealerTypeDTO>> createDealerType(@Valid @RequestBody DealerTypeDTO dealerTypeDTO) throws URISyntaxException {
        log.debug("REST request to save DealerType : {}", dealerTypeDTO);
        if (dealerTypeDTO.getId() != null) {
            throw new BadRequestAlertException("A new dealerType cannot already have an ID", ENTITY_NAME, "idexists");
        }
        return dealerTypeService
            .save(dealerTypeDTO)
            .map(result -> {
                try {
                    return ResponseEntity
                        .created(new URI("/api/dealer-types/" + result.getId()))
                        .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
                        .body(result);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    /**
     * {@code PUT  /dealer-types/:id} : Updates an existing dealerType.
     *
     * @param id the id of the dealerTypeDTO to save.
     * @param dealerTypeDTO the dealerTypeDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated dealerTypeDTO,
     * or with status {@code 400 (Bad Request)} if the dealerTypeDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the dealerTypeDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/dealer-types/{id}")
    public Mono<ResponseEntity<DealerTypeDTO>> updateDealerType(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody DealerTypeDTO dealerTypeDTO
    ) throws URISyntaxException {
        log.debug("REST request to update DealerType : {}, {}", id, dealerTypeDTO);
        if (dealerTypeDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, dealerTypeDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return dealerTypeRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                return dealerTypeService
                    .update(dealerTypeDTO)
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
     * {@code PATCH  /dealer-types/:id} : Partial updates given fields of an existing dealerType, field will ignore if it is null
     *
     * @param id the id of the dealerTypeDTO to save.
     * @param dealerTypeDTO the dealerTypeDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated dealerTypeDTO,
     * or with status {@code 400 (Bad Request)} if the dealerTypeDTO is not valid,
     * or with status {@code 404 (Not Found)} if the dealerTypeDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the dealerTypeDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/dealer-types/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public Mono<ResponseEntity<DealerTypeDTO>> partialUpdateDealerType(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody DealerTypeDTO dealerTypeDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update DealerType partially : {}, {}", id, dealerTypeDTO);
        if (dealerTypeDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, dealerTypeDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return dealerTypeRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                Mono<DealerTypeDTO> result = dealerTypeService.partialUpdate(dealerTypeDTO);

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
     * {@code GET  /dealer-types} : get all the dealerTypes.
     *
     * @param pageable the pagination information.
     * @param request a {@link ServerHttpRequest} request.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of dealerTypes in body.
     */
    @GetMapping("/dealer-types")
    public Mono<ResponseEntity<List<DealerTypeDTO>>> getAllDealerTypes(
        @org.springdoc.api.annotations.ParameterObject Pageable pageable,
        ServerHttpRequest request
    ) {
        log.debug("REST request to get a page of DealerTypes");
        return dealerTypeService
            .countAll()
            .zipWith(dealerTypeService.findAll(pageable).collectList())
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
     * {@code GET  /dealer-types/:id} : get the "id" dealerType.
     *
     * @param id the id of the dealerTypeDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the dealerTypeDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/dealer-types/{id}")
    public Mono<ResponseEntity<DealerTypeDTO>> getDealerType(@PathVariable Long id) {
        log.debug("REST request to get DealerType : {}", id);
        Mono<DealerTypeDTO> dealerTypeDTO = dealerTypeService.findOne(id);
        return ResponseUtil.wrapOrNotFound(dealerTypeDTO);
    }

    /**
     * {@code DELETE  /dealer-types/:id} : delete the "id" dealerType.
     *
     * @param id the id of the dealerTypeDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/dealer-types/{id}")
    public Mono<ResponseEntity<Void>> deleteDealerType(@PathVariable Long id) {
        log.debug("REST request to delete DealerType : {}", id);
        return dealerTypeService
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
     * {@code SEARCH  /_search/dealer-types?query=:query} : search for the dealerType corresponding
     * to the query.
     *
     * @param query the query of the dealerType search.
     * @param pageable the pagination information.
     * @param request a {@link ServerHttpRequest} request.
     * @return the result of the search.
     */
    @GetMapping("/_search/dealer-types")
    public Mono<ResponseEntity<Flux<DealerTypeDTO>>> searchDealerTypes(
        @RequestParam String query,
        @org.springdoc.api.annotations.ParameterObject Pageable pageable,
        ServerHttpRequest request
    ) {
        log.debug("REST request to search for a page of DealerTypes for query {}", query);
        return dealerTypeService
            .searchCount()
            .map(total -> new PageImpl<>(new ArrayList<>(), pageable, total))
            .map(page -> PaginationUtil.generatePaginationHttpHeaders(UriComponentsBuilder.fromHttpRequest(request), page))
            .map(headers -> ResponseEntity.ok().headers(headers).body(dealerTypeService.search(query, pageable)));
    }
}
