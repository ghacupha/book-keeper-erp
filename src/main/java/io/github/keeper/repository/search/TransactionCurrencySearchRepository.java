package io.github.keeper.repository.search;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

import io.github.keeper.domain.TransactionCurrency;
import io.github.keeper.repository.TransactionCurrencyRepository;
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
 * Spring Data Elasticsearch repository for the {@link TransactionCurrency} entity.
 */
public interface TransactionCurrencySearchRepository
    extends ReactiveElasticsearchRepository<TransactionCurrency, Long>, TransactionCurrencySearchRepositoryInternal {}

interface TransactionCurrencySearchRepositoryInternal {
    Flux<TransactionCurrency> search(String query, Pageable pageable);

    Flux<TransactionCurrency> search(Query query);
}

class TransactionCurrencySearchRepositoryInternalImpl implements TransactionCurrencySearchRepositoryInternal {

    private final ReactiveElasticsearchTemplate reactiveElasticsearchTemplate;

    TransactionCurrencySearchRepositoryInternalImpl(ReactiveElasticsearchTemplate reactiveElasticsearchTemplate) {
        this.reactiveElasticsearchTemplate = reactiveElasticsearchTemplate;
    }

    @Override
    public Flux<TransactionCurrency> search(String query, Pageable pageable) {
        NativeSearchQuery nativeSearchQuery = new NativeSearchQuery(queryStringQuery(query));
        nativeSearchQuery.setPageable(pageable);
        return search(nativeSearchQuery);
    }

    @Override
    public Flux<TransactionCurrency> search(Query query) {
        return reactiveElasticsearchTemplate.search(query, TransactionCurrency.class).map(SearchHit::getContent);
    }
}
