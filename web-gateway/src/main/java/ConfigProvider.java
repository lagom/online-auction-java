import com.typesafe.config.Config;
import play.Configuration;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

/**
 *
 */
@Singleton
public class ConfigProvider implements Provider<Config> {
    @Inject private Configuration configuration;
    @Override
    public Config get() {
        return configuration.underlying();
    }
}
