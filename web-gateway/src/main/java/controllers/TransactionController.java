package controllers;

import com.example.auction.pagination.PaginatedSequence;
import com.example.auction.transaction.api.*;
import com.example.auction.user.api.User;
import com.example.auction.user.api.UserService;
import com.typesafe.config.Config;
import play.data.Form;
import play.data.FormFactory;
import play.i18n.MessagesApi;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Call;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import static com.example.auction.security.ClientSecurity.authenticate;

public class TransactionController extends AbstractController {

    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_PAGE_SIZE = 15;

    private final FormFactory formFactory;
    private final TransactionService transactionService;

    private final Boolean showInlineInstruction;
    private HttpExecutionContext ec;

    @Inject
    public TransactionController(Config config,
                                 MessagesApi messagesApi,
                                 UserService userService,
                                 FormFactory formFactory,
                                 TransactionService transactionService,
                                 HttpExecutionContext ec) {
        super(messagesApi, userService);
        this.formFactory = formFactory;
        this.transactionService = transactionService;

        showInlineInstruction = config.getBoolean("online-auction.instruction.show");
        this.ec = ec;
    }

    public CompletionStage<Result> myTransactions(String statusParam, int page, int pageSize) {
        TransactionInfoStatus status = TransactionInfoStatus.valueOf(statusParam.toUpperCase(Locale.ENGLISH));
        return requireUser(ctx(),
                userId -> loadNav(userId).thenCombineAsync(
                        getTransactionsForUser(userId, status, page, pageSize), (nav, items) ->
                                ok(views.html.myTransactions.render(showInlineInstruction, status, items, nav)),
                        ec.current())
        );
    }

    private CompletionStage<PaginatedSequence<TransactionSummary>> getTransactionsForUser(
            UUID userId, TransactionInfoStatus status, int page, int pageSize) {
        return transactionService
                .getTransactionsForUser(status, Optional.of(page), Optional.of(pageSize))
                .handleRequestHeader(authenticate(userId))
                .invoke();
    }

    public static Call transactionsPage(TransactionInfoStatus status) {
        return transactionsPage(status, DEFAULT_PAGE, DEFAULT_PAGE_SIZE);
    }

    public static Call transactionsPage(TransactionInfoStatus status, int page, int pageSize) {
        return routes.TransactionController.myTransactions(status.name().toLowerCase(Locale.ENGLISH), page, pageSize);
    }

    public CompletionStage<Result> getTransaction(String id) {
        return requireUser(ctx(), user ->
                loadNav(user).thenComposeAsync( nav -> {
                            UUID itemId = UUID.fromString(id);
                            CompletionStage<TransactionInfo> transactionFuture = transactionService.getTransaction(itemId).handleRequestHeader(authenticate(user)).invoke();

                           return transactionFuture.thenCompose(transaction -> {

                                   UUID sellerId = transaction.getCreator();
                                   UUID winnerId = transaction.getWinner();
                                 return getUser(sellerId).thenCombine(getUser(winnerId),
                                       (seller, winner) -> {

                                           Currency currency = Currency.valueOf(transaction.getItemData().getCurrencyId());
                                           return ok(views.html.transaction.render(showInlineInstruction, Optional.of(transaction), user, Optional.of(seller), Optional.of(winner), Optional.of(currency), Optional.empty(), (Nav) nav));
                                       });
                           }).exceptionally(exception -> {
                                    String msg = exception.getCause().getMessage();
                                    return ok(views.html.transaction.render(showInlineInstruction, Optional.empty(), user, Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(msg),(Nav) nav));
                                });



                        }
                        , ec.current())
        );
    }

    public CompletionStage<Result> submitDeliveryDetailsForm(String id) {
        return requireUser(ctx(), user ->
                loadNav(user).thenComposeAsync(nav -> {
                            UUID itemId = UUID.fromString(id);
                            CompletionStage<TransactionInfo> transactionFuture = transactionService.getTransaction(itemId).handleRequestHeader(authenticate(user)).invoke();
                            return transactionFuture.handle((transaction, exception) -> {
                                if (exception == null) {
                                    DeliveryDetailsForm form = new DeliveryDetailsForm();
                                    Optional<DeliveryInfo> maybeDeliveryInfo = transaction.getDeliveryInfo();
                                    if (maybeDeliveryInfo.isPresent()) {
                                        form.setAddressLine1(maybeDeliveryInfo.get().getAddressLine1());
                                        form.setAddressLine2(maybeDeliveryInfo.get().getAddressLine2());
                                        form.setCity(maybeDeliveryInfo.get().getCity());
                                        form.setState(maybeDeliveryInfo.get().getState());
                                        form.setPostalCode(maybeDeliveryInfo.get().getPostalCode());
                                        form.setCountry(maybeDeliveryInfo.get().getCountry());
                                    }
                                    return ok(
                                            views.html.deliveryDetails.render(
                                                    showInlineInstruction,
                                                    !transaction.getCreator().equals(user),
                                                    itemId,
                                                    formFactory.form(DeliveryDetailsForm.class).fill(form),
                                                    transaction.getStatus(),
                                                    Optional.empty(),
                                                    (Nav)nav)
                                    );
                                } else {
                                    String msg = exception.getCause().getMessage();
                                    return ok(views.html.deliveryDetails.render(showInlineInstruction, false, itemId, formFactory.form(DeliveryDetailsForm.class), TransactionInfoStatus.NEGOTIATING_DELIVERY, Optional.of(msg), (Nav)nav));
                                }
                            });
                        },
                        ec.current())
        );
    }

    public CompletionStage<Result> submitDeliveryDetails(String id, String transactionStatus, boolean isBuyer) {
        Http.Context ctx = ctx();
        return requireUser(ctx(), user -> {

            Form<DeliveryDetailsForm> form = formFactory.form(DeliveryDetailsForm.class).bindFromRequest(ctx.request());
            UUID itemId = UUID.fromString(id);
            TransactionInfoStatus status = TransactionInfoStatus.valueOf(transactionStatus);

            if (form.hasErrors()) {
                return loadNav(user).thenApplyAsync(nav ->
                                ok(views.html.deliveryDetails.render(showInlineInstruction, isBuyer, itemId, form, status, Optional.empty(), nav)),
                        ec.current()
                );
            } else {
                return transactionService.submitDeliveryDetails(itemId)
                        .handleRequestHeader(authenticate(user))
                        .invoke(fromForm(form.get()))
                        .handle((done, exception) -> {
                            if (exception == null) {
                                return CompletableFuture.completedFuture(redirect(routes.TransactionController.getTransaction(id)));
                            } else {
                                String msg = exception.getCause().getMessage();
                                return loadNav(user).thenApplyAsync(nav ->
                                                ok(views.html.deliveryDetails.render(showInlineInstruction, isBuyer, itemId, form, status, Optional.of(msg), nav)),
                                        ec.current());
                            }
                        }).thenComposeAsync(x -> x, ec.current());
            }
        });
    }

    private DeliveryInfo fromForm(DeliveryDetailsForm deliveryForm) {
        return new DeliveryInfo(
                deliveryForm.getAddressLine1(),
                deliveryForm.getAddressLine2(),
                deliveryForm.getCity(),
                deliveryForm.getState(),
                deliveryForm.getPostalCode(),
                deliveryForm.getCountry()
        );
    }

    public CompletionStage<Result> setDeliveryPriceForm(String id) {
        return requireUser(ctx(), user ->
                loadNav(user).thenComposeAsync(nav -> {
                            UUID itemId = UUID.fromString(id);
                            CompletionStage<TransactionInfo> transactionFuture = transactionService.getTransaction(itemId).handleRequestHeader(authenticate(user)).invoke();
                            return transactionFuture.handle((transaction, exception) -> {
                                if (exception == null) {
                                    DeliveryPriceForm form = new DeliveryPriceForm();
                                    Optional<Integer> maybeDeliveryPrice = transaction.getDeliveryPrice();
                                    if (maybeDeliveryPrice.isPresent())
                                        form.setDeliveryPrice(maybeDeliveryPrice.get());
                                    return ok(
                                            views.html.deliveryPrice.render(
                                                    showInlineInstruction,
                                                    transaction.getCreator().equals(user),
                                                    itemId,
                                                    formFactory.form(DeliveryPriceForm.class).fill(form),
                                                    transaction.getStatus(),
                                                    Optional.empty(),
                                                    (Nav)nav)
                                    );
                                } else {
                                    String msg = exception.getCause().getMessage();
                                    return ok(views.html.deliveryPrice.render(showInlineInstruction, false, itemId, formFactory.form(DeliveryPriceForm.class), TransactionInfoStatus.NEGOTIATING_DELIVERY, Optional.of(msg),(Nav) nav));
                                }
                            });
                        },
                        ec.current())
        );
    }

    public CompletionStage<Result> setDeliveryPrice(String id, String transactionStatus, boolean isSeller) {
        Http.Context ctx = ctx();
        return requireUser(ctx(), user -> {

            Form<DeliveryPriceForm> form = formFactory.form(DeliveryPriceForm.class).bindFromRequest(ctx.request());
            UUID itemId = UUID.fromString(id);
            TransactionInfoStatus status = TransactionInfoStatus.valueOf(transactionStatus);

            if (form.hasErrors()) {
                return loadNav(user).thenApplyAsync(nav ->
                                ok(views.html.deliveryPrice.render(showInlineInstruction, isSeller, itemId, form, status, Optional.empty(), nav)),
                        ec.current()
                );
            } else {
                return transactionService.setDeliveryPrice(itemId)
                        .handleRequestHeader(authenticate(user))
                        .invoke(form.get().getDeliveryPrice())
                        .handle((done, exception) -> {
                            if (exception == null) {
                                return CompletableFuture.completedFuture(redirect(routes.TransactionController.getTransaction(id)));
                            } else {
                                String msg = exception.getCause().getMessage();
                                return loadNav(user).thenApplyAsync(nav ->
                                                ok(views.html.deliveryPrice.render(showInlineInstruction, isSeller, itemId, form, status, Optional.of(msg),(Nav) nav)),
                                        ec.current());
                            }
                        }).thenComposeAsync(x -> x, ec.current());
            }
        });
    }

    public CompletionStage<Result> approveDelivery(String id) {
        return requireUser(ctx(), user ->
                transactionService.approveDeliveryDetails(UUID.fromString(id))
                        .handleRequestHeader(authenticate(user))
                        .invoke()
                        .thenApplyAsync(done ->
                                        redirect(routes.TransactionController.getTransaction(id)),
                                ec.current()
                        )
        );
    }

    public CompletionStage<Result> submitPaymentDetailsForm(String id) {
        return requireUser(ctx(), user ->
                loadNav(user).thenComposeAsync(nav -> {
                            UUID itemId = UUID.fromString(id);
                            CompletionStage<TransactionInfo> transactionFuture = transactionService.getTransaction(itemId).handleRequestHeader(authenticate(user)).invoke();
                            return transactionFuture.handle((transaction, exception) -> {
                                if (exception == null) {
                                    // For now there is only one payment method supported: Offline payment
                                    OfflinePaymentForm form = new OfflinePaymentForm();
                                    Optional<PaymentInfo> maybePaymentInfo = transaction.getPaymentInfo();
                                    if (maybePaymentInfo.isPresent()) {
                                        form.setComment(((PaymentInfo.Offline) maybePaymentInfo.get()).getComment());
                                    }
                                    return ok(
                                            views.html.paymentDetails.render(
                                                    showInlineInstruction,
                                                    !transaction.getCreator().equals(user),
                                                    itemId,
                                                    formFactory.form(OfflinePaymentForm.class).fill(form),
                                                    transaction.getStatus(),
                                                    Optional.empty(),
                                                    nav)
                                    );
                                } else {
                                    String msg = exception.getCause().getMessage();
                                    return ok(views.html.paymentDetails.render(showInlineInstruction, false, itemId, formFactory.form(OfflinePaymentForm.class), TransactionInfoStatus.NEGOTIATING_DELIVERY, Optional.of(msg), nav));
                                }
                            });
                        },
                        ec.current())
        );
    }

    public CompletionStage<Result> submitPaymentDetails(String id, String transactionStatus, boolean isBuyer) {
        Http.Context ctx = ctx();
        return requireUser(ctx(), user -> {

            Form<OfflinePaymentForm> form = formFactory.form(OfflinePaymentForm.class).bindFromRequest(ctx.request());
            UUID itemId = UUID.fromString(id);
            TransactionInfoStatus status = TransactionInfoStatus.valueOf(transactionStatus);

            if (form.hasErrors()) {
                return loadNav(user).thenApplyAsync(nav ->
                                ok(views.html.paymentDetails.render(showInlineInstruction, isBuyer, itemId, form, status, Optional.empty(), nav)),
                        ec.current()
                );
            } else {
                return transactionService.submitPaymentDetails(itemId)
                        .handleRequestHeader(authenticate(user))
                        .invoke(fromForm(form.get()))
                        .handle((done, exception) -> {
                            if (exception == null) {
                                return CompletableFuture.completedFuture(redirect(routes.TransactionController.getTransaction(id)));
                            } else {
                                String msg = exception.getCause().getMessage();
                                return loadNav(user).thenApplyAsync(nav ->
                                                ok(views.html.paymentDetails.render(showInlineInstruction, isBuyer, itemId, form, status, Optional.of(msg), nav)),
                                        ec.current());
                            }
                        }).thenComposeAsync(x -> x, ec.current());
            }
        });
    }

    public CompletionStage<Result> approvePayment(String id) {
        return submitPaymentStatus(PaymentInfoStatus.APPROVED, id);
    }

    public CompletionStage<Result> rejectPayment(String id) {
        return submitPaymentStatus(PaymentInfoStatus.REJECTED, id);
    }

    private CompletionStage<Result> submitPaymentStatus(PaymentInfoStatus paymentInfoStatus, String id) {
        return requireUser(ctx(), user ->
            transactionService.submitPaymentStatus(UUID.fromString(id))
                .handleRequestHeader(authenticate(user))
                .invoke(paymentInfoStatus)
                .thenApplyAsync(done ->
                        redirect(routes.TransactionController.getTransaction(id)),
                    ec.current()
                )
        );
    }

    private PaymentInfo fromForm(OfflinePaymentForm offlinePayment) {
        return new PaymentInfo.Offline(offlinePayment.getComment());
    }
}
