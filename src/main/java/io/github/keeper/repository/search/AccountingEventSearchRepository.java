package io.github.keeper.repository.search;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

import io.github.keeper.domain.AccountingEvent;
import io.github.keeper.repository.AccountingEventRepository;
import java.util.List;
import org.elasticsearch.search.sort.SortBuilder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.repository.ReactiveElasticsearchRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

/**
 * Spring Data Elasticsearch repository for the {@link AccountingEvent} entity.
 */
public interface AccountingEventSearchRepository
    extends ReactiveElasticsearchRepository<AccountingEvent, Long>, AccountingEventSearchRepositoryInternal {}

interface AccountingEventSearchRepositoryInternal {
    Flux<AccountingEvent> search(String query, Pageable pageable);

    Flux<AccountingEvent> search(Query query);
}

class AccountingEventSearchRepositoryInternalImpl implements AccountingEventSearchRepositoryInternal {

    private final ReactiveElasticsearchTemplate reactiveElasticsearchTemplate;

    AccountingEventSearchRepositoryInternalImpl(ReactiveElasticsearchTemplate reactiveElasticsearchTemplate) {
        this.reactiveElasticsearchTemplate = reactiveElasticsearchTemplate;
    }

    @Override
    public Flux<AccountingEvent> search(String query, Pageable pageable) {
        NativeSearchQuery nativeSearchQuery = new NativeSearchQuery(queryStringQuery(query));
        nativeSearchQuery.setPageable(pageable);
        return search(nativeSearchQuery);
    }

    @Override
    public Flux<AccountingEvent> search(Query query) {
        return reactiveElasticsearchTemplate.search(query, AccountingEvent.class).map(SearchHit::getContent);
    }
}
