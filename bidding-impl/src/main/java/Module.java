import com.example.auction.bidding.api.BiddingService;
import com.example.auction.bidding.impl.AuctionScheduler;
import com.example.auction.bidding.impl.BiddingServiceImpl;
import com.example.auction.item.api.ItemService;
import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;

/**
 * Module that registers the bidding service, the clients it talks to, and the auction scheduler.
 */
public class Module extends AbstractModule implements ServiceGuiceSupport {

    @Override
    protected void configure() {
        bindServices(BiddingService.SERVICE_ID, serviceBinding(BiddingService.class, BiddingServiceImpl.class));

        bindClient(ItemService.class);

        bind(AuctionScheduler.class).asEagerSingleton();
    }
}
