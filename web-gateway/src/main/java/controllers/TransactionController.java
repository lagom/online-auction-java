package controllers;

import com.example.auction.pagination.PaginatedSequence;
import com.example.auction.transaction.api.*;
import com.example.auction.user.api.UserService;
import com.typesafe.config.Config;
import play.data.Form;
import play.data.FormFactory;
import play.i18n.Messages;
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

    public CompletionStage<Result> myTransactions(final Http.Request request, String statusParam, int page, int pageSize) {
        TransactionInfoStatus status = TransactionInfoStatus.valueOf(statusParam.toUpperCase(Locale.ENGLISH));
        return requireUser(request.session(),
                userId -> loadNav(userId).thenCombineAsync(
                        getTransactionsForUser(userId, status, page, pageSize), (nav, items) ->
                                ok(views.html.myTransactions.render(showInlineInstruction, status, items, nav, messagesApi.preferred(request))),
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

    public CompletionStage<Result> getTransaction(final Http.Request request, String id) {
        return requireUser(request.session(), user ->
                loadNav(user).thenComposeAsync( nav -> {
                            UUID itemId = UUID.fromString(id);
                            CompletionStage<TransactionInfo> transactionFuture = transactionService.getTransaction(itemId).handleRequestHeader(authenticate(user)).invoke();

                           return transactionFuture.thenCompose(transaction -> {

                                   UUID sellerId = transaction.getCreator();
                                   UUID winnerId = transaction.getWinner();
                                 return getUser(sellerId).thenCombine(getUser(winnerId),
                                       (seller, winner) -> {

                                           Currency currency = Currency.valueOf(transaction.getItemData().getCurrencyId());
                                           return ok(views.html.transaction.render(showInlineInstruction, Optional.of(transaction), user, Optional.of(seller), Optional.of(winner), Optional.of(currency), Optional.empty(), nav, messagesApi.preferred(request)));
                                       });
                           }).exceptionally(exception -> {
                                    String msg = exception.getCause().getMessage();
                                    return ok(views.html.transaction.render(showInlineInstruction, Optional.empty(), user, Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(msg), nav, messagesApi.preferred(request)));
                                });



                        }
                        , ec.current())
        );
    }

    public CompletionStage<Result> submitDeliveryDetailsForm(final Http.Request request, String id) {
        return requireUser(request.session(), user ->
                loadNav(user).thenComposeAsync(nav -> {
                            Messages messages = messagesApi.preferred(request);
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
                                                nav,
                                                messages
                                            )
                                    );
                                } else {
                                    String msg = exception.getCause().getMessage();
                                    return ok(
                                        views.html.deliveryDetails.render(
                                            showInlineInstruction,
                                            false,
                                            itemId,
                                            formFactory.form(DeliveryDetailsForm.class),
                                            TransactionInfoStatus.NEGOTIATING_DELIVERY,
                                            Optional.of(msg),
                                            nav,
                                            messages
                                        )
                                    );
                                }
                            });
                        },
                        ec.current())
        );
    }

    public CompletionStage<Result> submitDeliveryDetails(final Http.Request request, String id, String transactionStatus, boolean isBuyer) {
        return requireUser(request.session(), user -> {

            Form<DeliveryDetailsForm> form = formFactory.form(DeliveryDetailsForm.class).bindFromRequest(request);
            UUID itemId = UUID.fromString(id);
            TransactionInfoStatus status = TransactionInfoStatus.valueOf(transactionStatus);
            Messages messages = messagesApi.preferred(request);

            if (form.hasErrors()) {
                return loadNav(user).thenApplyAsync(nav ->
                                ok(views.html.deliveryDetails.render(showInlineInstruction, isBuyer, itemId, form, status, Optional.empty(), nav, messages)),
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
                                                ok(views.html.deliveryDetails.render(showInlineInstruction, isBuyer, itemId, form, status, Optional.of(msg), nav, messages)),
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

    public CompletionStage<Result> setDeliveryPriceForm(final Http.Request request, String id) {
        return requireUser(request.session(), user ->
                loadNav(user).thenComposeAsync(nav -> {
                            Messages messages = messagesApi.preferred(request);
                            UUID itemId = UUID.fromString(id);
                            CompletionStage<TransactionInfo> transactionFuture = transactionService.getTransaction(itemId).handleRequestHeader(authenticate(user)).invoke();
                            return transactionFuture.handle((transaction, exception) -> {
                                if (exception == null) {
                                    DeliveryPriceForm form = new DeliveryPriceForm();
                                    Optional<Integer> maybeDeliveryPrice = transaction.getDeliveryPrice();
                                    maybeDeliveryPrice.ifPresent(form::setDeliveryPrice);
                                    return ok(
                                            views.html.deliveryPrice.render(
                                                    showInlineInstruction,
                                                    transaction.getCreator().equals(user),
                                                    itemId,
                                                    formFactory.form(DeliveryPriceForm.class).fill(form),
                                                    transaction.getStatus(),
                                                    Optional.empty(),
                                                    nav,
                                                    messages
                                            )
                                    );
                                } else {
                                    String msg = exception.getCause().getMessage();
                                    return ok(
                                        views.html.deliveryPrice.render(
                                            showInlineInstruction,
                                            false,
                                            itemId,
                                            formFactory.form(DeliveryPriceForm.class),
                                            TransactionInfoStatus.NEGOTIATING_DELIVERY,
                                            Optional.of(msg),
                                            nav,
                                            messages
                                        )
                                    );
                                }
                            });
                        },
                        ec.current())
        );
    }

    public CompletionStage<Result> setDeliveryPrice(final Http.Request request, String id, String transactionStatus, boolean isSeller) {
        return requireUser(request.session(), user -> {

            Form<DeliveryPriceForm> form = formFactory.form(DeliveryPriceForm.class).bindFromRequest(request);
            UUID itemId = UUID.fromString(id);
            TransactionInfoStatus status = TransactionInfoStatus.valueOf(transactionStatus);

            Messages messages = messagesApi.preferred(request);

            if (form.hasErrors()) {
                return loadNav(user).thenApplyAsync(nav ->
                                ok(views.html.deliveryPrice.render(showInlineInstruction, isSeller, itemId, form, status, Optional.empty(), nav, messages)),
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
                                                ok(views.html.deliveryPrice.render(showInlineInstruction, isSeller, itemId, form, status, Optional.of(msg), nav, messages)),
                                        ec.current());
                            }
                        }).thenComposeAsync(x -> x, ec.current());
            }
        });
    }

    public CompletionStage<Result> approveDelivery(final Http.Request request, String id) {
        return requireUser(request.session(), user ->
                transactionService.approveDeliveryDetails(UUID.fromString(id))
                        .handleRequestHeader(authenticate(user))
                        .invoke()
                        .thenApplyAsync(done ->
                                        redirect(routes.TransactionController.getTransaction(id)),
                                ec.current()
                        )
        );
    }

    public CompletionStage<Result> submitPaymentDetailsForm(final Http.Request request, String id) {
        return requireUser(request.session(), user ->
                loadNav(user).thenComposeAsync(nav -> {
                            UUID itemId = UUID.fromString(id);
                            CompletionStage<TransactionInfo> transactionFuture = transactionService.getTransaction(itemId).handleRequestHeader(authenticate(user)).invoke();
                            return transactionFuture.handle((transaction, exception) -> {
                                if (exception == null) {
                                    // For now there is only one payment method supported: Offline payment
                                    OfflinePaymentForm form = new OfflinePaymentForm();
                                    Optional<PaymentInfo> maybePaymentInfo = transaction.getPaymentInfo();
                                    maybePaymentInfo.ifPresent(paymentInfo -> form.setComment(((PaymentInfo.Offline) paymentInfo).getComment()));
                                    return ok(
                                            views.html.paymentDetails.render(
                                                    showInlineInstruction,
                                                    !transaction.getCreator().equals(user),
                                                    itemId,
                                                    formFactory.form(OfflinePaymentForm.class).fill(form),
                                                    transaction.getStatus(),
                                                    Optional.empty(),
                                                    nav,
                                                    messagesApi.preferred(request)
                                                )
                                    );
                                } else {
                                    String msg = exception.getCause().getMessage();
                                    return ok(
                                        views.html.paymentDetails.render(
                                            showInlineInstruction,
                                            false,
                                            itemId,
                                            formFactory.form(OfflinePaymentForm.class),
                                            TransactionInfoStatus.NEGOTIATING_DELIVERY,
                                            Optional.of(msg),
                                            nav,
                                            messagesApi.preferred(request)
                                        )
                                    );
                                }
                            });
                        },
                        ec.current())
        );
    }

    public CompletionStage<Result> submitPaymentDetails(final Http.Request request, String id, String transactionStatus, boolean isBuyer) {
        return requireUser(request.session(), user -> {

            Form<OfflinePaymentForm> form = formFactory.form(OfflinePaymentForm.class).bindFromRequest(request);
            UUID itemId = UUID.fromString(id);
            TransactionInfoStatus status = TransactionInfoStatus.valueOf(transactionStatus);

            if (form.hasErrors()) {
                return loadNav(user).thenApplyAsync(nav ->
                                ok(views.html.paymentDetails.render(showInlineInstruction, isBuyer, itemId, form, status, Optional.empty(), nav, messagesApi.preferred(request))),
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
                                                ok(views.html.paymentDetails.render(showInlineInstruction, isBuyer, itemId, form, status, Optional.of(msg), nav, messagesApi.preferred(request))),
                                        ec.current());
                            }
                        }).thenComposeAsync(x -> x, ec.current());
            }
        });
    }

    public CompletionStage<Result> approvePayment(final Http.Request request, String id) {
        return submitPaymentStatus(request, PaymentInfoStatus.APPROVED, id);
    }

    public CompletionStage<Result> rejectPayment(final Http.Request request, String id) {
        return submitPaymentStatus(request, PaymentInfoStatus.REJECTED, id);
    }

    private CompletionStage<Result> submitPaymentStatus(final Http.Request request, PaymentInfoStatus paymentInfoStatus, String id) {
        return requireUser(request.session(), user ->
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
