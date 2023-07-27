package io.github.keeper.repository.search;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

import io.github.keeper.domain.BalanceSheetItemType;
import io.github.keeper.repository.BalanceSheetItemTypeRepository;
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
 * Spring Data Elasticsearch repository for the {@link BalanceSheetItemType} entity.
 */
public interface BalanceSheetItemTypeSearchRepository
    extends ReactiveElasticsearchRepository<BalanceSheetItemType, Long>, BalanceSheetItemTypeSearchRepositoryInternal {}

interface BalanceSheetItemTypeSearchRepositoryInternal {
    Flux<BalanceSheetItemType> search(String query, Pageable pageable);

    Flux<BalanceSheetItemType> search(Query query);
}

class BalanceSheetItemTypeSearchRepositoryInternalImpl implements BalanceSheetItemTypeSearchRepositoryInternal {

    private final ReactiveElasticsearchTemplate reactiveElasticsearchTemplate;

    BalanceSheetItemTypeSearchRepositoryInternalImpl(ReactiveElasticsearchTemplate reactiveElasticsearchTemplate) {
        this.reactiveElasticsearchTemplate = reactiveElasticsearchTemplate;
    }

    @Override
    public Flux<BalanceSheetItemType> search(String query, Pageable pageable) {
        NativeSearchQuery nativeSearchQuery = new NativeSearchQuery(queryStringQuery(query));
        nativeSearchQuery.setPageable(pageable);
        return search(nativeSearchQuery);
    }

    @Override
    public Flux<BalanceSheetItemType> search(Query query) {
        return reactiveElasticsearchTemplate.search(query, BalanceSheetItemType.class).map(SearchHit::getContent);
    }
}
