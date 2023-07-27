package io.github.keeper.web.rest;

import io.github.keeper.repository.AccountTransactionRepository;
import io.github.keeper.service.AccountTransactionService;
import io.github.keeper.service.dto.AccountTransactionDTO;
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
 * REST controller for managing {@link io.github.keeper.domain.AccountTransaction}.
 */
@RestController
@RequestMapping("/api")
public class AccountTransactionResource {

    private final Logger log = LoggerFactory.getLogger(AccountTransactionResource.class);

    private static final String ENTITY_NAME = "accountTransaction";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final AccountTransactionService accountTransactionService;

    private final AccountTransactionRepository accountTransactionRepository;

    public AccountTransactionResource(
        AccountTransactionService accountTransactionService,
        AccountTransactionRepository accountTransactionRepository
    ) {
        this.accountTransactionService = accountTransactionService;
        this.accountTransactionRepository = accountTransactionRepository;
    }

    /**
     * {@code POST  /account-transactions} : Create a new accountTransaction.
     *
     * @param accountTransactionDTO the accountTransactionDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new accountTransactionDTO, or with status {@code 400 (Bad Request)} if the accountTransaction has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/account-transactions")
    public Mono<ResponseEntity<AccountTransactionDTO>> createAccountTransaction(
        @Valid @RequestBody AccountTransactionDTO accountTransactionDTO
    ) throws URISyntaxException {
        log.debug("REST request to save AccountTransaction : {}", accountTransactionDTO);
        if (accountTransactionDTO.getId() != null) {
            throw new BadRequestAlertException("A new accountTransaction cannot already have an ID", ENTITY_NAME, "idexists");
        }
        return accountTransactionService
            .save(accountTransactionDTO)
            .map(result -> {
                try {
                    return ResponseEntity
                        .created(new URI("/api/account-transactions/" + result.getId()))
                        .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
                        .body(result);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    /**
     * {@code PUT  /account-transactions/:id} : Updates an existing accountTransaction.
     *
     * @param id the id of the accountTransactionDTO to save.
     * @param accountTransactionDTO the accountTransactionDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated accountTransactionDTO,
     * or with status {@code 400 (Bad Request)} if the accountTransactionDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the accountTransactionDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/account-transactions/{id}")
    public Mono<ResponseEntity<AccountTransactionDTO>> updateAccountTransaction(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody AccountTransactionDTO accountTransactionDTO
    ) throws URISyntaxException {
        log.debug("REST request to update AccountTransaction : {}, {}", id, accountTransactionDTO);
        if (accountTransactionDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, accountTransactionDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return accountTransactionRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                return accountTransactionService
                    .update(accountTransactionDTO)
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
     * {@code PATCH  /account-transactions/:id} : Partial updates given fields of an existing accountTransaction, field will ignore if it is null
     *
     * @param id the id of the accountTransactionDTO to save.
     * @param accountTransactionDTO the accountTransactionDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated accountTransactionDTO,
     * or with status {@code 400 (Bad Request)} if the accountTransactionDTO is not valid,
     * or with status {@code 404 (Not Found)} if the accountTransactionDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the accountTransactionDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/account-transactions/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public Mono<ResponseEntity<AccountTransactionDTO>> partialUpdateAccountTransaction(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody AccountTransactionDTO accountTransactionDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update AccountTransaction partially : {}, {}", id, accountTransactionDTO);
        if (accountTransactionDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, accountTransactionDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return accountTransactionRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                Mono<AccountTransactionDTO> result = accountTransactionService.partialUpdate(accountTransactionDTO);

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
     * {@code GET  /account-transactions} : get all the accountTransactions.
     *
     * @param pageable the pagination information.
     * @param request a {@link ServerHttpRequest} request.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of accountTransactions in body.
     */
    @GetMapping("/account-transactions")
    public Mono<ResponseEntity<List<AccountTransactionDTO>>> getAllAccountTransactions(
        @org.springdoc.api.annotations.ParameterObject Pageable pageable,
        ServerHttpRequest request
    ) {
        log.debug("REST request to get a page of AccountTransactions");
        return accountTransactionService
            .countAll()
            .zipWith(accountTransactionService.findAll(pageable).collectList())
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
     * {@code GET  /account-transactions/:id} : get the "id" accountTransaction.
     *
     * @param id the id of the accountTransactionDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the accountTransactionDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/account-transactions/{id}")
    public Mono<ResponseEntity<AccountTransactionDTO>> getAccountTransaction(@PathVariable Long id) {
        log.debug("REST request to get AccountTransaction : {}", id);
        Mono<AccountTransactionDTO> accountTransactionDTO = accountTransactionService.findOne(id);
        return ResponseUtil.wrapOrNotFound(accountTransactionDTO);
    }

    /**
     * {@code DELETE  /account-transactions/:id} : delete the "id" accountTransaction.
     *
     * @param id the id of the accountTransactionDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/account-transactions/{id}")
    public Mono<ResponseEntity<Void>> deleteAccountTransaction(@PathVariable Long id) {
        log.debug("REST request to delete AccountTransaction : {}", id);
        return accountTransactionService
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
     * {@code SEARCH  /_search/account-transactions?query=:query} : search for the accountTransaction corresponding
     * to the query.
     *
     * @param query the query of the accountTransaction search.
     * @param pageable the pagination information.
     * @param request a {@link ServerHttpRequest} request.
     * @return the result of the search.
     */
    @GetMapping("/_search/account-transactions")
    public Mono<ResponseEntity<Flux<AccountTransactionDTO>>> searchAccountTransactions(
        @RequestParam String query,
        @org.springdoc.api.annotations.ParameterObject Pageable pageable,
        ServerHttpRequest request
    ) {
        log.debug("REST request to search for a page of AccountTransactions for query {}", query);
        return accountTransactionService
            .searchCount()
            .map(total -> new PageImpl<>(new ArrayList<>(), pageable, total))
            .map(page -> PaginationUtil.generatePaginationHttpHeaders(UriComponentsBuilder.fromHttpRequest(request), page))
            .map(headers -> ResponseEntity.ok().headers(headers).body(accountTransactionService.search(query, pageable)));
    }
}
