import com.example.auction.user.api.UserService;
import com.example.auction.user.impl.UserRepository;
import com.example.auction.user.impl.UserServiceImpl;
import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;

public class Module extends AbstractModule implements ServiceGuiceSupport {
    @Override
    protected void configure() {
        bindService(UserService.class, UserServiceImpl.class);
        bind(UserRepository.class);
    }
}
