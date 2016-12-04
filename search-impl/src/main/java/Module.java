import com.example.auction.bidding.api.BiddingService;
import com.example.auction.item.api.ItemService;
import com.example.auction.search.api.SearchService;
import com.example.auction.search.impl.SearchServiceImpl;
import com.example.elasticsearch.ElasticSearch;
import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;

/**
 *
 */
public class Module extends AbstractModule implements ServiceGuiceSupport {
    @Override
    protected void configure() {
        bindServices(serviceBinding(SearchService.class, SearchServiceImpl.class));
        bindClient(ElasticSearch.class);
        bindClient(BiddingService.class);
        bindClient(ItemService.class);
    }
}
