import com.example.auction.item.api.ItemService;
import com.example.auction.transaction.api.TransactionService;
import com.example.auction.transaction.impl.TransactionServiceImpl;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;
import play.inject.ApplicationLifecycle;
import scala.concurrent.duration.FiniteDuration;

import java.util.concurrent.TimeUnit;

/**
 * Module that registers the transaction service, the clients it talks to (item service).
 */
public class Module extends AbstractModule implements ServiceGuiceSupport {

    @Override
    protected void configure() {
        bindService(TransactionService.class, TransactionServiceImpl.class);
        bindClient(ItemService.class);

        bind(TransactionServiceLifecycle.class).asEagerSingleton();
    }

    private static class TransactionServiceLifecycle {
        @Inject
        public TransactionServiceLifecycle(ApplicationLifecycle applicationLifecycle, PersistentEntityRegistry persistentEntityRegistry) {
            applicationLifecycle.addStopHook(() -> persistentEntityRegistry.gracefulShutdown(FiniteDuration.create(5, TimeUnit.SECONDS)));
        }
    }
}
