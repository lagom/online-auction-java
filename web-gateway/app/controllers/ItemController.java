package controllers;

import com.example.auction.item.api.Item;
import com.example.auction.item.api.ItemService;
import com.example.auction.user.api.UserService;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

public class ItemController extends AbstractController {

    private final FormFactory formFactory;
    private final ItemService itemService;

    @Inject
    public ItemController(UserService userService, FormFactory formFactory, ItemService itemService) {
        super(userService);
        this.formFactory = formFactory;
        this.itemService = itemService;
    }

    public CompletionStage<Result> createItemForm() {
        return requireUser(user ->
                loadNav(user).thenApply(nav ->
                        ok(views.html.editItem.render(formFactory.form(ItemForm.class).fill(new ItemForm()), nav))
                )
        );
    }

    public CompletionStage<Result> createItem() {
        Http.Request request = request();
        return requireUser(user -> {

            Form<ItemForm> form = formFactory.form(ItemForm.class).bindFromRequest(request);

            if (form.hasErrors()) {
                return loadNav(user).thenApply(nav ->
                        ok(views.html.editItem.render(form, nav))
                );
            } else {
                ItemForm itemForm = form.get();

                Currency currency = Currency.valueOf(itemForm.getCurrency());
                Duration duration = Duration.of(itemForm.getDuration(), ChronoUnit.valueOf(itemForm.getDurationUnits()));

                return itemService.createItem().invoke(new Item(user, itemForm.getTitle(), itemForm.getDescription(), itemForm.getCurrency(),
                        currency.toPriceUnits(itemForm.getIncrement().doubleValue()),
                        currency.toPriceUnits(itemForm.getReserve().doubleValue()), duration)).thenApply(item -> {

                    return redirect(routes.ItemController.getItem(item.getId().toString()));

                 });
            }
        });
    }

    public CompletionStage<Result> getItem(String itemId) {
        return requireUser(user -> {

            return itemService.getItem(UUID.fromString(itemId)).invoke().thenApply(item ->
                    ok()
            );
        });
    }

}
