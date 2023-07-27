package io.github.keeper.repository.search;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

import io.github.keeper.domain.Dealer;
import io.github.keeper.repository.DealerRepository;
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
 * Spring Data Elasticsearch repository for the {@link Dealer} entity.
 */
public interface DealerSearchRepository extends ReactiveElasticsearchRepository<Dealer, Long>, DealerSearchRepositoryInternal {}

interface DealerSearchRepositoryInternal {
    Flux<Dealer> search(String query, Pageable pageable);

    Flux<Dealer> search(Query query);
}

class DealerSearchRepositoryInternalImpl implements DealerSearchRepositoryInternal {

    private final ReactiveElasticsearchTemplate reactiveElasticsearchTemplate;

    DealerSearchRepositoryInternalImpl(ReactiveElasticsearchTemplate reactiveElasticsearchTemplate) {
        this.reactiveElasticsearchTemplate = reactiveElasticsearchTemplate;
    }

    @Override
    public Flux<Dealer> search(String query, Pageable pageable) {
        NativeSearchQuery nativeSearchQuery = new NativeSearchQuery(queryStringQuery(query));
        nativeSearchQuery.setPageable(pageable);
        return search(nativeSearchQuery);
    }

    @Override
    public Flux<Dealer> search(Query query) {
        return reactiveElasticsearchTemplate.search(query, Dealer.class).map(SearchHit::getContent);
    }
}
