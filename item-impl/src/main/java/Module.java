import com.example.auction.bidding.api.BiddingService;
import com.example.auction.item.api.ItemService;
import com.example.auction.item.impl.ItemRepository;
import com.example.auction.item.impl.ItemServiceImpl;
import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.api.ServiceInfo;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;

public class Module extends AbstractModule implements ServiceGuiceSupport {
    @Override
    protected void configure() {
        bindInfo(new ServiceInfo(ItemService.SERVICE_ID));
        bindServices(serviceBinding(ItemService.class, ItemServiceImpl.class));
        bindClient(BiddingService.class);
        bind(ItemRepository.class);
    }
}
