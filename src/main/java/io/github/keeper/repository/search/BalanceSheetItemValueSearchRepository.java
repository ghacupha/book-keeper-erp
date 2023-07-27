package io.github.keeper.repository.search;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

import io.github.keeper.domain.BalanceSheetItemValue;
import io.github.keeper.repository.BalanceSheetItemValueRepository;
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
 * Spring Data Elasticsearch repository for the {@link BalanceSheetItemValue} entity.
 */
public interface BalanceSheetItemValueSearchRepository
    extends ReactiveElasticsearchRepository<BalanceSheetItemValue, Long>, BalanceSheetItemValueSearchRepositoryInternal {}

interface BalanceSheetItemValueSearchRepositoryInternal {
    Flux<BalanceSheetItemValue> search(String query, Pageable pageable);

    Flux<BalanceSheetItemValue> search(Query query);
}

class BalanceSheetItemValueSearchRepositoryInternalImpl implements BalanceSheetItemValueSearchRepositoryInternal {

    private final ReactiveElasticsearchTemplate reactiveElasticsearchTemplate;

    BalanceSheetItemValueSearchRepositoryInternalImpl(ReactiveElasticsearchTemplate reactiveElasticsearchTemplate) {
        this.reactiveElasticsearchTemplate = reactiveElasticsearchTemplate;
    }

    @Override
    public Flux<BalanceSheetItemValue> search(String query, Pageable pageable) {
        NativeSearchQuery nativeSearchQuery = new NativeSearchQuery(queryStringQuery(query));
        nativeSearchQuery.setPageable(pageable);
        return search(nativeSearchQuery);
    }

    @Override
    public Flux<BalanceSheetItemValue> search(Query query) {
        return reactiveElasticsearchTemplate.search(query, BalanceSheetItemValue.class).map(SearchHit::getContent);
    }
}
