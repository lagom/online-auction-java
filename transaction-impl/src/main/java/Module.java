import com.example.auction.transaction.impl.TransactionServiceImpl;
import com.example.auction.item.api.ItemService;
import com.example.auction.transaction.api.TransactionService;
import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;

/**
 * Module that registers the transaction service, the clients it talks to (item service).
 */
public class Module extends AbstractModule implements ServiceGuiceSupport {

    @Override
    protected void configure() {
        bindService(TransactionService.class, TransactionServiceImpl.class);

        bindClient(ItemService.class);
    }
}
