package router;

import akka.stream.Materializer;
import controllers.*;
import play.api.mvc.Handler;
import play.api.mvc.RequestHeader;
import play.mvc.Http;
import play.api.routing.Router;
import play.routing.RoutingDsl;
import scala.Option;
import scala.PartialFunction;
import scala.Tuple3;
import scala.collection.Seq;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * We use the Java DSL router instead of the Play routes file, so that this project can also be built by Maven, which
 * as yet does not have a plugin for compiling Play routers.
 */
@Singleton
public class Routes implements Router {

    private final Main main;
    private final ProfileController profile;
    private final ItemController item;
    private final SearchController search;
    private final Assets assets;
    private final WebJarAssets webJars;
    private final Materializer materializer;

    private final Router router;

    @Inject
    public Routes(Main main, ProfileController profile, ItemController item, SearchController search,
            Assets assets, WebJarAssets webJars, Materializer materializer) {
        this.main = main;
        this.profile = profile;
        this.item = item;
        this.search = search;
        this.assets = assets;
        this.webJars = webJars;
        this.materializer = materializer;

        this.router = buildRouter();
    }

    private Router buildRouter() {
        return new RoutingDsl()
                // Index
                .GET("/").routeAsync(main::index)

                // User management
                .GET("/createuser").routeAsync(main::createUserForm)
                .POST("/user").routeAsync(main::createUser)
                .POST("/currentuser/:userId").routeTo(main::currentUser)

                // User profile
                .GET("/my/items/:status").routeAsync((String status) ->
                        profile.myItems(status, getIntQueryParameter("page", ProfileController.DEFAULT_PAGE),
                                getIntQueryParameter("pageSize", ProfileController.DEFAULT_PAGE_SIZE))
                )

                // Items
                .GET("/createitem").routeAsync(item::createItemForm)
                .POST("/item").routeAsync(item::createItem)
                .GET("/item/:id").routeAsync(item::getItem)
                .POST("/item/:id").routeAsync((String id) ->
                        item.editItem(id, requestHeader().getQueryString("itemStatus")))
                .GET("/item/:id/edit").routeAsync(item::editItemForm)
                .POST("/item/:id/start").routeAsync(item::startAuction)
                .POST("/item/:id/bid").routeAsync(item::placeBid)

                // Search
                .GET("/search").routeAsync(search::searchForm)
                .POST("/search").routeAsync(search::search)

                // Assets
                .GET("/assets/*file").routeAsync((String file) ->
                        assets.at("/public", file, false).asJava()
                                .apply(requestHeader()).run(materializer))
                .GET("/webjars/*file").routeAsync((String file) ->
                        webJars.at(file).asJava()
                                .apply(requestHeader()).run(materializer))

                .build().asScala();
    }

    private int getIntQueryParameter(String name, int defaultValue) {
        String value = requestHeader().getQueryString(name);
        if (value == null) {
            return defaultValue;
        } else {
            return Integer.parseInt(value);
        }
    }

    private Http.RequestHeader requestHeader() {
        return Http.Context.current().request();
    }

    @Override
    public PartialFunction<RequestHeader, Handler> routes() {
        return router.routes();
    }

    @Override
    public Seq<Tuple3<String, String, String>> documentation() {
        return router.documentation();
    }

    @Override
    public Router withPrefix(String prefix) {
        return router.withPrefix(prefix);
    }

    @Override
    public Option<Handler> handlerFor(RequestHeader request) {
        return router.handlerFor(request);
    }

    @Override
    public play.routing.Router asJava() {
        return router.asJava();
    }
}
