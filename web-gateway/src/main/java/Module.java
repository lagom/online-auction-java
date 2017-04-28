import com.example.auction.bidding.api.BiddingService;
import com.example.auction.item.api.ItemService;
import com.example.auction.search.api.SearchService;
import com.example.auction.user.api.UserService;
import com.lightbend.lagom.javadsl.api.ServiceAcl;
import com.lightbend.lagom.javadsl.api.ServiceInfo;
import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.client.ServiceClientGuiceSupport;

public class Module extends AbstractModule implements ServiceClientGuiceSupport {
    @Override
    protected void configure() {
        bindServiceInfo(ServiceInfo.of("web-gateway-module", ServiceAcl.path("(?!/api/).*")));
        bindClient(UserService.class);
        bindClient(ItemService.class);
        bindClient(BiddingService.class);
        bindClient(SearchService.class);
    }
}
