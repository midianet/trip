package midianet.trip.bot.app;

//import midianet.road.bussines.*;
//import midianet.road.domain.*;
//import org.apache.log4j.Logger;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//import org.telegram.telegrambots.api.methods.send.SendMessage;
//import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
//import org.telegram.telegrambots.api.objects.Update;
//import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
//import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
//import org.telegram.telegrambots.bots.TelegramLongPollingBot;
//import org.telegram.telegrambots.exceptions.TelegramApiException;
//
//import java.text.NumberFormat;
//import java.time.format.DateTimeFormatter;
//import java.util.*;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import midianet.trip.bot.app.model.MessageUser;
import midianet.trip.model.Passenger;
import midianet.trip.bot.app.repository.MessageUserRepository;
import midianet.trip.repository.PassengerRepository;
import midianet.trip.repository.PaymentRepository;
import midianet.trip.service.PassengerService;
import midianet.trip.util.DateUtil;
import midianet.trip.util.TelegramUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static midianet.trip.util.MessageUtil.*;

@Log4j2
@Component
@RequiredArgsConstructor
public class AppBot extends TelegramLongPollingBot {
    private static final String PARSE_MODE_HTML = "html";

    private final MessageUserRepository repository;
    private final PassengerRepository passengerRepository;
    private final PaymentRepository paymentRepository;

    @Value("${trip.bot.app.user}")
    private String user;

    @Value("${trip.bot.app.token}")
    private String token;

    @Override
    @Transactional
    public void onUpdateReceived(@NonNull final Update update) {
        try {
            if (update.hasMessage()) {
                if ("/start".equals(update.getMessage().getText()))
                    actionStart(update.getMessage());
            } else if (update.hasCallbackQuery()) {
                final var message = update.getCallbackQuery().getMessage();
                switch (Action.valueOf(update.getCallbackQuery().getData())) {
                    case LIST:
                        actionList(message);
                        break;
                    case PAYMENT:
                        actionPayment(message);
                        break;
                    case ACCOUNT:
                        actionAccount(message);
                        break;
                }
            }
        }catch (TelegramApiRequestException e){
            if(e.getErrorCode() != 400) {
                actionRecovery(update);
                log.error(e.getMessage(),e);
            }
        } catch (TelegramApiException e) {
            log.error(e.getMessage(), e);
            actionRecovery(update);
        }
//        if (update.hasMessage()) {
//            final String text = update.getMessage().getText();
//            if(text.startsWith(CMD_START)){
//                actionStart(update);
//            }else if(text.startsWith(CMD_NTC_ALL)) {
//                actionNoticeAll(text.replace(CMD_NTC_ALL,"").trim());
//            }else if(text.startsWith(CMD_NTC_PAY)) {
//                actionNoticePay(text.replace(CMD_NTC_PAY,"").trim());
//            }else if (text.startsWith(CMD_NTC_NO_PAY)) {
//                actionNoticeNoPay(text.replace(CMD_NTC_NO_PAY, "").trim());
//            }else if(text.startsWith(CMD_NTC_CONFIRM)){
//                actionConfirmMember(text.replace(CMD_NTC_CONFIRM,"").trim());
//            }else{
//                actionInvalid(update);
//            }
//        }else if(update.hasCallbackQuery()){
//            switch (update.getCallbackQuery().getData()) {
//                case CMD_LIST:
//                    actionList(update);
//                    break;
//                case CMD_ACCOUNT:
//                    actionAccount(update);
//                    break;
//                case CMD_BALANCE:
//                    actionBalance(update);
//                    break;
//                case CMD_PROFILE:
//                    actionProfile(update);
//                    break;
//                case CMD_DRINK:
//                    actionDrink(update);
//                    break;
//                case CMD_VOLTAR:
//                    actionBack(update);
//                    break;
//                case CMD_NADA:
//                    actionNothing(update);
//                    break;
//                case CMD_CERVEJA:
//                    actionBeer(update);
//                    break;
//                case CMD_ENERG:
//                    actionEnerg(update);
//                    break;
//                case CMD_REFRIG:
//                    actionRefrig(update);
//                    break;
//                case CMD_ICE:
//                    actionIce(update);
//                    break;
//                case CMD_SUCO:
//                    actionSuco(update);
//                    break;
//                case CMD_TODDY:
//                    actionToddy(update);
//                    break;
//                case CMD_AGUAC:
//                    actionAguaC(update);
//                    break;
////                case CMD_CONDUCT:
////                    actionConduct(update);
////                    break;
////                case CMD_AGREE:
////                    actionAgree(update);
////                    break;
////                case CMD_DISAGREE:
////                    actionDesagree(update);
////                    break;
//                default:
//                    actionInvalid(update);
//                    break;
//            }
//
//        }
    }

    private void actionStart(@NonNull final Message message) throws TelegramApiException {
        final var name = TelegramUtil.buildFullName(message.getFrom().getFirstName(),
                message.getFrom().getLastName());
        final var messageId = execute(SendMessage.builder()
                .chatId(String.valueOf(message.getChatId()))
                .text(getMessage("midianet.trip.message.bot.app.welcome", name))
                .replyMarkup(buildKeyboard())
                .parseMode(PARSE_MODE_HTML)
                .build()).getChatId();
        passengerRepository.findById(String.valueOf(message.getChatId()))
                .orElseGet(() -> passengerRepository.save(Passenger.builder()
                        .id(String.valueOf(message.getChatId()))
                        .status(Passenger.Status.INTERESTED)
                        .name(name).build()));
        repository.save(MessageUser.builder()
                .chatId(message.getChatId())
                .messageId(messageId).build());
    }

    private void actionRecovery(@NonNull final Update update){
        try {
            actionStart(update.hasMessage() ? update.getMessage() : update.getCallbackQuery().getMessage());
        }catch (Exception e){
            log.error(e.getMessage(),e);
        }
    }

    private void actionList(@NonNull final Message message) throws TelegramApiException {
        final var text = new StringBuilder();
        final var list = passengerRepository.findAll(Sort.by(Sort.DEFAULT_DIRECTION, "name", "status"));
        final var listInterested = list.stream()
                .filter(passenger -> Passenger.Status.INTERESTED.equals(passenger.getStatus()))
                .collect(Collectors.toList());
        final var listAssociate = list.stream()
                .filter(passenger -> Passenger.Status.ASSOCIATED.equals(passenger.getStatus()))
                .collect(Collectors.toList());
        final var listConfirmed = list.stream()
                .filter(passenger -> Passenger.Status.CONFIRMED.equals(passenger.getStatus()))
                .collect(Collectors.toList());
        if (!listInterested.isEmpty()) {
            text.append("\n\uD83D\uDD34 <b>Interessados (").append(listInterested.size()).append(")</b>\n");
            listInterested.forEach(passenger -> text.append(buildUserRow(passenger)));
        }
        if (!listAssociate.isEmpty()) {
            text.append("\n\uD83D\uDFE0 <b>Associados (").append(listAssociate.size()).append(")</b>\n");
            listAssociate.forEach(passenger -> text.append(buildUserRow(passenger)));
        }
        if (!listConfirmed.isEmpty()) {
            text.append("\n\uD83D\uDFE2 <b>Confirmados (").append(listAssociate.size()).append(")</b>\n");
            listConfirmed.forEach(passenger -> text.append(buildUserRow(passenger)));
        }
        if (list.isEmpty()) {
            text.append("Lista Vazia\n");
        }
        final var sum = listAssociate.size() + listConfirmed.size();
        final var percent = BigDecimal.valueOf(sum / .55).setScale(2, RoundingMode.HALF_EVEN);
        text.append("\n\uD83D\uDE8C Lotação atual: <b>").append(percent.doubleValue()).append("%</b> -  <i>").append(sum).append(" de 55</i>");
        execute(EditMessageText.builder()
                .chatId(String.valueOf(message.getChatId()))
                .messageId(message.getMessageId())
                .text(text.toString())
                .parseMode(PARSE_MODE_HTML)
                .replyMarkup(buildKeyboard())
                .build());
    }

    private void actionAccount(@NonNull Message message) throws TelegramApiException {
        execute(EditMessageText.builder()
                .chatId(String.valueOf(message.getChatId()))
                .messageId(message.getMessageId())
                .replyMarkup(buildKeyboard())
                .parseMode(PARSE_MODE_HTML)
                .text("⚙️ Banco: <b>Caixa Econômica</b>\n⚙️ Agência: <b>3621</b>\n⚙️ Operação: <b>001</b>\n⚙️ Conta Corrente: <b>24894-3</b>\n\uD83D\uDC64 <i>Marcos Fernando da Costa</i>\n\uD83D\uDC64 <i>854.024.191-91</i>\n\uD83D\uDCDE <i>62.98417-7762</i>\n✉️ <i>midianet@gmail.com</i>")
                .build());
    }

    private void actionPayment(@NonNull final Message message) throws TelegramApiException {
//        final DateTimeFormatter dtf = DateTimeFormatter.ofPattern ("dd-MM-yyyy");
//        final NumberFormat nbf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR" ));
        final Long chatId = message.getChatId();
        final Integer messageId = message.getMessageId();
        final var report = new StringBuilder("\uD83D\uDCB0 <b>Pagamentos</b>\n\n");
        passengerRepository.findById(String.valueOf(chatId))
            .ifPresent(passenger -> {
                if(Objects.nonNull(passenger.getFamily())){
                    report.append(String.format("\uD83D\uDC68\u200D\uD83D\uDC68\u200D\uD83D\uDC66 <b>Familia:</b> %s\n",passenger.getFamily().getName()));
                    final var payments = paymentRepository.findByFamilyId(passenger.getFamily().getId());
                    payments.forEach(payment -> {
                        report.append(String.format("\uD83D\uDCB5 %s - <b>%s</b>\n",
                                payment.getDate().format(DateUtil.DATE_DDMMYYYY),
                                DateUtil.CURRENCY_FORMAT.format(payment.getAmount())));
                    });
                    final var total = payments.stream()
                        .map(payment -> payment.getAmount())
                        .reduce(0.0, Double::sum).doubleValue();
                    if(total > 0.0){
                        report.append(String.format("\uD83D\uDCB2 Total: <b>%d</b>\n", total ));
                    }else{
                        report.append("\uD83E\uDD14 Ainda não existe pagamentos registrados");
                    }
            }
        });
//        final Optional<Person> person = personRepository.findByTelegram(chatId);
//        person.ifPresent(p -> {
//            Double credit = 0.0;
//            final List<Payment> payments = paymentRepository.findByPerson(p.getId())
//                    .stream()
//                    .filter(pa -> !ObjectUtils.isEmpty(pa.getDateLow()))
//                    .collect(Collectors.toList());
//            if (!payments.isEmpty()) {
//                report.append("\n\uD83D\uDE04 Depósitos");
//                payments.forEach(pa -> {
//                    report.append("\n  \uD83D\uDCB0")
//                            .append(pa.getDate().format(dtf))
//                            .append("   ")
//                            .append(nbf.format(pa.getAmount()));
//                });
//                credit = payments.stream()
//                        .map(Payment::getAmount)
//                        .reduce(BigDecimal::add)
//                        .get().doubleValue();
//                report.append("\n\uD83E\uDD14 Saldo ").append(nbf.format(credit));
//            }else{

//            }
//        });
        execute(EditMessageText.builder()
                .chatId(String.valueOf(message.getChatId()))
                .messageId(message.getMessageId())
                .replyMarkup(buildKeyboard())
                .parseMode(PARSE_MODE_HTML)
                .text(report.toString()).build());
    }

    private String buildUserRow(@NonNull final Passenger passenger) {
        return "  \uD83E\uDD14 ".concat(passenger.getName()).concat("\n");
    }

    private InlineKeyboardMarkup buildKeyboard() {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(
                        InlineKeyboardButton.builder()
                                .callbackData(Action.LIST.name())
                                .text("Passageiros")
                                .build(),
                        InlineKeyboardButton.builder()
                                .callbackData(Action.PAYMENT.name())
                                .text("Pagamentos")
                                .build()))

                .keyboardRow(List.of(
                        InlineKeyboardButton.builder()
                                .callbackData(Action.ACCOUNT.name())
                                .text("Conta Depósito")
                                .build())).build();
    }

    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public String getBotUsername() {
        return user;
    }

    @Getter
    @AllArgsConstructor
    public enum Action {
        START, LIST, ACCOUNT, PAYMENT;
    }

}

//    private static final String CMD_START       = "/start";
//    private static final String CMD_LIST        = "/list";
//    private static final String CMD_ACCOUNT     = "/account";
//    private static final String CMD_BALANCE     = "/balance";
//    private static final String MNU_ROOT        = "MNUROOT";
//    private static final String CMD_NTC_ALL     = "/noticeAll";
//    private static final String CMD_NTC_PAY     = "/noticePay";
//    private static final String CMD_NTC_NO_PAY  = "/noticeNoPay";
//    private static final String CMD_PROFILE     = "/profile";
//    private static final String CMD_NTC_CONFIRM = "/confirm";
//    private static final String CMD_CONTINUE    = "/continue";
//
//
//    private static final String CMD_DRINK   = "/drink";
//    private static final String CMD_NADA   = "/nada";
//    private static final String CMD_CERVEJA = "/cerva";
//    private static final String CMD_REFRIG  = "/refri";
//    private static final String CMD_ENERG   = "/energ";
//    private static final String CMD_SUCO    = "/suco";
//    private static final String CMD_ICE     = "/ice";
//    private static final String CMD_TODDY   = "/toddy";
//    private static final String CMD_AGUAC   = "/agua";
//    private static final String CMD_VOLTAR  = "/back";
//    private static final String MNU_DRINK   = "MNUDRINK";
////    private static final String CMD_CONDUCT = "/conduct";
////    private static final String CMD_AGREE   =  "/concordo";
////    private static final String CMD_DISAGREE=  "/discordo";
//
//    @Autowired
//    private AlienBot alienBot;
//
//    @Autowired
//    private PartnerBussines partnerBussines;
//
//    @Autowired
//    private MessageBussines messageBussines;
//
//    @Autowired
//    private BedroomBussines bedroomBussines;
//
//    @Autowired
//    private BalanceBussines balanceBussines;
//    private void actionBeer(final Update update) {
//        try {
//            final Long chatId = update.getCallbackQuery().getMessage().getChatId();
//            final Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
//            Optional<Partner> partner = partnerBussines.findByTelegram(chatId);
//            final EditMessageText editText = new EditMessageText();
//            editText.setText("Indefinido");
//            partner.ifPresent(p -> {
//                p.setBeer(!p.getBeer());
//                if (!p.getBeer()) {
//                    editText.setText("\uD83D\uDC4E Você não irá beber Cerveja (Seu fraco)");
//                } else {
//                    editText.setText("\uD83D\uDC4D Você irá beber Cerveja (Parabéns)");
//                }
//                partnerBussines.updateDrink(p);
//            });
//            editText.setChatId(chatId.toString());
//            editText.setMessageId(messageId);
//            editText.setReplyMarkup(buildKeyboard(MNU_DRINK));
//            editMessageText(editText);
//        } catch (Exception e) {
//            log.error(e);
//        }
//    }
//
//    private void actionNothing(final Update update){
//        try {
//            final Long chatId = update.getCallbackQuery().getMessage().getChatId();
//            final Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
//            Optional<Partner> partner = partnerBussines.findByTelegram(chatId);
//            final EditMessageText editText = new EditMessageText();
//            editText.setText("Indefinido");
//            partner.ifPresent(p -> {
//                p.setDry(!p.getDry());
//                editText.setText("\uD83D\uDC4E Você não irá beber Nada (pão duro)");
//                partnerBussines.updateDrink(p);
//            });
//            editText.setChatId(chatId.toString());
//            editText.setMessageId(messageId);
//            editText.setReplyMarkup(buildKeyboard(MNU_DRINK));
//            editMessageText(editText);
//        } catch (Exception e) {
//            log.error(e);
//        }
//    }
//
//
//    private void actionRefrig(final Update update){
//        try {
//            final Long chatId = update.getCallbackQuery().getMessage().getChatId();
//            final Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
//            Optional<Partner> partner = partnerBussines.findByTelegram(chatId);
//            final EditMessageText editText = new EditMessageText();
//            editText.setText("Indefinido");
//            partner.ifPresent(p -> {
//                p.setRefreshment(!p.getRefreshment());
//                if (!p.getRefreshment()) {
//                    editText.setText("\uD83D\uDC4E Você não irá beber refrigerante, Ta de dieta?");
//                } else {
//                    editText.setText("\uD83D\uDC4D Você irá beber refrigerante, Engorda viu...");
//                }
//                partnerBussines.updateDrink(p);
//            });
//            editText.setChatId(chatId.toString());
//            editText.setMessageId(messageId);
//            editText.setReplyMarkup(buildKeyboard(MNU_DRINK));
//            editMessageText(editText);
//        } catch (Exception e) {
//            log.error(e);
//        }
//    }
//
//    private void actionEnerg(final Update update){
//        try {
//            final Long chatId = update.getCallbackQuery().getMessage().getChatId();
//            final Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
//            Optional<Partner> partner = partnerBussines.findByTelegram(chatId);
//            final EditMessageText editText = new EditMessageText();
//            editText.setText("Indefinido");
//            partner.ifPresent(p -> {
//                p.setEnergetic(!p.getEnergetic());
//                if (!p.getEnergetic()) {
//                    editText.setText("\uD83D\uDC4E Você não irá beber energético, Vai dormindo?");
//                } else {
//                    editText.setText("\uD83D\uDC4D Você irá beber energético, Vai ficar ligado");
//                }
//                partnerBussines.updateDrink(p);
//            });
//            editText.setChatId(chatId.toString());
//            editText.setMessageId(messageId);
//            editText.setReplyMarkup(buildKeyboard(MNU_DRINK));
//            editMessageText(editText);
//        } catch (Exception e) {
//            log.error(e);
//        }
//    }
//
//    private void actionIce(final Update update){
//        try {
//            final Long chatId = update.getCallbackQuery().getMessage().getChatId();
//            final Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
//            Optional<Partner> partner = partnerBussines.findByTelegram(chatId);
//            final EditMessageText editText = new EditMessageText();
//            editText.setText("Indefinido");
//            partner.ifPresent(p -> {
//                p.setIce(!p.getIce());
//                if (!p.getIce()) {
//                    editText.setText("\uD83D\uDC4E Você não irá beber Smirnof Ice");
//                } else {
//                    editText.setText("\uD83D\uDC4D Você irá beber Smirnof Ice");
//                }
//                partnerBussines.updateDrink(p);
//            });
//            editText.setChatId(chatId.toString());
//            editText.setMessageId(messageId);
//            editText.setReplyMarkup(buildKeyboard(MNU_DRINK));
//            editMessageText(editText);
//        } catch (Exception e) {
//            log.error(e);
//        }
//    }
//
//    private void actionSuco(final Update update){
//        try {
//            final Long chatId = update.getCallbackQuery().getMessage().getChatId();
//            final Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
//            Optional<Partner> partner = partnerBussines.findByTelegram(chatId);
//            final EditMessageText editText = new EditMessageText();
//            editText.setText("Indefinido");
//            partner.ifPresent(p -> {
//                p.setJuice(!p.getJuice());
//                if (!p.getJuice()) {
//                    editText.setText("\uD83D\uDC4E Você não irá beber suco");
//                } else {
//                    editText.setText("\uD83D\uDC4D Você irá beber suco");
//                }
//                partnerBussines.updateDrink(p);
//            });
//            editText.setChatId(chatId.toString());
//            editText.setMessageId(messageId);
//            editText.setReplyMarkup(buildKeyboard(MNU_DRINK));
//            editMessageText(editText);
//        } catch (Exception e) {
//            log.error(e);
//        }
//    }
//
//    private void actionToddy(final Update update){
//        try {
//            final Long chatId = update.getCallbackQuery().getMessage().getChatId();
//            final Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
//            Optional<Partner> partner = partnerBussines.findByTelegram(chatId);
//            final EditMessageText editText = new EditMessageText();
//            editText.setText("Indefinido");
//            partner.ifPresent(p -> {
//                p.setMilk(!p.getMilk());
//                if (!p.getMilk()) {
//                    editText.setText("\uD83D\uDC4E Você não irá beber toddynho, ainda bem ne");
//                } else {
//                    editText.setText("\uD83D\uDC4D Você irá beber toddynho, e criança?");
//                }
//                partnerBussines.updateDrink(p);
//            });
//            editText.setChatId(chatId.toString());
//            editText.setMessageId(messageId);
//            editText.setReplyMarkup(buildKeyboard(MNU_DRINK));
//            editMessageText(editText);
//        } catch (Exception e) {
//            log.error(e);
//        }
//    }
//
//    private void actionAguaC(final Update update){
//        try {
//            final Long chatId = update.getCallbackQuery().getMessage().getChatId();
//            final Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
//            Optional<Partner> partner = partnerBussines.findByTelegram(chatId);
//            final EditMessageText editText = new EditMessageText();
//            editText.setText("Indefinido");
//            partner.ifPresent(p -> {
//                p.setCoconut(!p.getCoconut());
//                if (!p.getCoconut()) {
//                    editText.setText("\uD83D\uDC4E Você não irá beber Água de côco, que bom ne");
//                } else {
//                    editText.setText("\uD83D\uDC4D Você irá beber Água de côco, que fresco");
//                }
//                partnerBussines.updateDrink(p);
//            });
//            editText.setChatId(chatId.toString());
//            editText.setMessageId(messageId);
//            editText.setReplyMarkup(buildKeyboard(MNU_DRINK));
//            editMessageText(editText);
//        } catch (Exception e) {
//            log.error(e);
//        }
//    }
//
//    private void actionBack(final Update update){
//        try {
//            final Long chatId  = update.getCallbackQuery().getMessage().getChatId();
//            final Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
//            final EditMessageText editText = new EditMessageText();
//            editText.setText("Escolha uma das opções abaixo.");
//            editText.setChatId(chatId.toString());
//            editText.setMessageId(messageId);
//            editText.setReplyMarkup(buildKeyboard(MNU_ROOT));
//            editMessageText(editText);
//        }catch(Exception e){
//            log.error(e);
//        }
//    }
//
//    private void actionDrink(final Update update){
//        try {
//            final Long chatId  = update.getCallbackQuery().getMessage().getChatId();
//            final Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
//            final EditMessageText editText = new EditMessageText();
//            editText.setText("Escolha as bebidas\nque irá consumir no ônibus");
//            editText.setChatId(chatId.toString());
//            editText.setMessageId(messageId);
//            editText.setReplyMarkup(buildKeyboard(MNU_DRINK));
//            editMessageText(editText);
//        }catch(Exception e){
//            log.error(e);
//        }
//    }
//
//    Logger log = Logger.getLogger(getClass().getName());
//    private static final String ACTION_START    = "/start";
//    private static final String ACTION_CONTINUE = "/continue";
//    private static final String ACTION_MAIN     = "/main";
//    private static final String ACTION_LIST     = "/list";
//    private static final String ACTION_PROFILE  = "/profile";
//    private static final String ACTION_FLUX     = "/flux";
//    private static final String ACTION_ACCOUNT  = "/account";
//    private static final String ACTION_BALANCE  = "/balance";
//    private static final String ACTION_CONTRACT = "/contract";
//    private static final String ACTION_AGREE    = "/agree";
//    private static final String ACTION_DESAGREE = "/desagree";
//    private static final String ACTION_PHOTO    = "/photo";
//
//    @Value("${telegram-token}")
//    private String token;
//
//    @Value("${telegram-username}")
//    private String username;
//
//    @Autowired
//    private PersonRepository personRepository;
//
//    @Autowired
//    private PhotoRepository photoRepository;
//
//    @Autowired
//    private BedroomRepository bedroomRepository;
//
//    @Autowired
//    private MessageRepository messageRepository;
//
//    @Autowired
//    private PaymentRepository paymentRepository;
//
//    @Override
//    public String getBotToken() {
//        return token;
//    }
//
//    @Override
//    public String getBotUsername() {
//        return username;
//    }
//
//    @Override
//    public void onUpdateReceived(Update update) {
//        String data = null;
//        if (update.hasMessage()){
//            if(update.getMessage().hasText()) {
//                data = update.getMessage().getText();
//            }else if(update.getMessage().hasPhoto()){
//                data = ACTION_PHOTO;
//            }
//        }
//        if(update.hasCallbackQuery()) {
//            data = update.getCallbackQuery().getData();
//        }
//        if(StringUtils.hasText(data)) {
//            switch (data) {
//                case ACTION_START :
//                    actionStart(update);
//                    break;
//                case ACTION_CONTINUE:
//                    actionContinue(update);
//                    break;
//                case ACTION_FLUX:
//                    actionFlux(update);
//                    break;
//                case ACTION_LIST :
//                    actionList(update);
//                    break;
//                case ACTION_ACCOUNT :
//                    actionAccount(update);
//                    break;
//                case ACTION_BALANCE :
//                    actionBalance(update);
//                    break;
//                case ACTION_PROFILE :
//                    actionProfile(update);
//                    break;
//                case ACTION_CONTRACT :
//                    actionContract(update);
//                    break;
//                case ACTION_AGREE :
//                    actionSubscrible(update,Person.Contract.AGREE);
//                    break;
//                case ACTION_DESAGREE:
//                    actionSubscrible(update,Person.Contract.DISAGRE);
//                    break;
//                case ACTION_PHOTO:
//                    actionPhoto(update);
//                    break;
//                default:
//                    actionInvalid(update);
//            }
//        }
//    }
//
//    private void actionStart(Update update) {
//        Try.run(() -> {
//            Integer messageId;
//            Long chatId = update.getMessage().getChatId();
//            String firstName = Optional.ofNullable(update.getMessage().getFrom().getFirstName()).orElse("Indefinido...");
//            StringBuilder text = new StringBuilder();
//            text.append(String.format("Olá <b>%s</b>\n\n", firstName));
//            text.append("Esse ano muita coisa mudou na nossa caravana, para garantir sua vaga você deverá passar pelo fluxo abaixo (clique na imagem para ampliar)<a href=\"http://latinowarego.tk//site/images/fluxo2018.jpg\">.</a>");
//            SendMessage send = new SendMessage();
//            send.setChatId(update.getMessage().getChatId());
//            send.setText(text.toString());
//            send.setReplyMarkup(buildKeyboard(ACTION_START));
//            send.enableHtml(true);
//            messageId = execute(send).getMessageId();
//            messageRepository.save(Message.builder().telegram(chatId).message(messageId).build());
//        }).onFailure(e -> custoLog(e));
//    }
//
//    private void actionContinue(Update update){
//        Long   chatId     = update.getCallbackQuery().getMessage().getChatId();
//        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
//        String firstName  = Optional.ofNullable(update.getCallbackQuery().getFrom().getFirstName()).orElse("Indefinido...");
//        String lastName   = Optional.ofNullable(update.getCallbackQuery().getFrom().getLastName()).orElse("");
//        Try.run(() -> {
//            Person person = personRepository.findByTelegram(chatId).orElse(Person.builder().build());
//            person.setName(String.format("%s %s", firstName, lastName));
//            person.setTelegram(chatId);
//            person.setId(Optional.ofNullable(person.getId()).orElseGet(() -> personRepository.insert(person).getId()));
//            EditMessageText send = new EditMessageText();
//            send.setChatId(chatId);
//            send.setMessageId(messageId);
//            send.setText("Escolha uma opção abaixo");
//            send.setReplyMarkup(buildKeyboard(ACTION_MAIN));
//            send.enableHtml(true);
//            execute(send);
//        }).onFailure(e -> custoLog(e));
//    }
//
//
//
//    private void actionProfile(Update update){
//        Long chatId        = update.getCallbackQuery().getMessage().getChatId();
//        Integer messageId  = update.getCallbackQuery().getMessage().getMessageId();
//        StringBuilder text = new StringBuilder();
//        Try.run(() -> {
//            Optional<Person> p = personRepository.findByTelegram(chatId);
//            text.append("\uD83D\uDC64 <b>Meu Perfil</b>\n\n");
//            p.ifPresent(o -> {
//                text.append("\uD83D\uDE36 Nome :<b>"    ).append(o.getName()                                                       ).append("</b>\n");
//                text.append("\uD83C\uDFF7 CPF: <b>"     ).append(StringUtils.hasText(o.getCpf()) ? o.getCpf() : "Não informado"    ).append("</b>\n");
//                text.append("\uD83D\uDD16 RG: <b>"      ).append(StringUtils.hasText(o.getRg()) ? o.getRg()   : "Não informado");
//                text.append(" "                         ).append(StringUtils.hasText(o.getRgexped()) ? o.getRgexped() : ""         ).append("</b>\n");
//                text.append("\uD83D\uDCDE Fone: <b>"    ).append(StringUtils.hasText(o.getPhone()) ? o.getPhone() : "Não informado").append("</b>\n");
//                text.append("\uD83D\uDC41 Status: <b>"  ).append(o.getState().getDescription()                                     ).append("</b>\n");
//                text.append("\uD83D\uDEBD Poltrona: <b>").append(ObjectUtils.isEmpty(o.getAssent()) ? "Indefinida" : o.getAssent() ).append("</b>\n");
//                text.append("\uD83D\uDCDD Contrato: <b>").append(o.getAgreed().getDescription()                                    ).append("</b>\n");
//                text.append("\uD83D\uDECF Acomodação: ");
//                if(!ObjectUtils.isEmpty(o.getBedroom())){
//                    bedroomRepository.findById(o.getBedroom().getId()).ifPresent(b -> {
//                        text.append(String.format("<b>%s %s</b>\n",b.getType().getDescription(), b.getGender().getDescription()));
//                        personRepository.listByBedroom(b.getId()).forEach(ocupant -> text.append("<i>").append(buildPersonRow(ocupant)).append("</i>"));
//                    });
//                }else{
//                    text.append("<b>Não informada</b>");
//                }
//            });
//            if(!p.isPresent()){
//                text.append("Sem informações");
//            }
//            EditMessageText edit = new EditMessageText();
//            edit.setChatId   (chatId);
//            edit.setMessageId(messageId);
//            edit.setText(text.toString());
//            edit.setReplyMarkup(buildKeyboard(ACTION_MAIN));
//            edit.enableHtml(true);
//            execute(edit);
//        }).onFailure(e -> custoLog(e));
//    }
//
//    private String buildPersonRow(Person person){
//        return String.format(" %s %s\n", person.getSex().equals(Person.Sex.FEMALE) ? "\uD83D\uDE4B" : "\uD83D\uDE4B\u200D♂", person.getNickname());
//    }
//
//    private void actionList(Update update){
//        Try.run(() -> {
//            Long chatId        = update.getCallbackQuery().getMessage().getChatId();
//            Integer messageId  = update.getCallbackQuery().getMessage().getMessageId();
//            StringBuilder text = new StringBuilder();
//            List<Person> listAll        = personRepository.listAll();
//            List<Person> listWaiting    = listAll.stream().filter(p -> p.getState() == Person.State.WAITING   ).collect(Collectors.toList());
//            List<Person> listSelected   = listAll.stream().filter(p -> p.getState() == Person.State.SELECTED  ).collect(Collectors.toList());
//            List<Person> listAssociate  = listAll.stream().filter(p -> p.getState() == Person.State.ASSOCIATE ).collect(Collectors.toList());
//            List<Person> listRegistered = listAll.stream().filter(p -> p.getState() == Person.State.REGISTERED).collect(Collectors.toList());
//            List<Person> listConfirmed  = listAll.stream().filter(p -> p.getState() == Person.State.CONFIRMED ).collect(Collectors.toList());
//            text.append("\uD83D\uDC68\u200D\uD83D\uDCBB\uD83D\uDC69\u200D\uD83D\uDCBB <b>Inscritos</b>\n");
//            if (!listWaiting.isEmpty()) {
//                text.append("<b>Candidatos (").append(listWaiting.size()).append(")</b>\n");
//                listWaiting.forEach(pw -> {
//                    text.append(buildPersonRow(pw));
//                });
//            }
//            if (!listSelected.isEmpty()) {
//                text.append("<b>Selecionados (").append(listSelected.size()).append(")</b>\n");
//                listSelected.forEach(ps -> {
//                    text.append(buildPersonRow(ps));
//                });
//            }
//            if (!listAssociate.isEmpty()) {
//                text.append("<b>Associados (").append(listAssociate.size()).append(")</b>\n");
//                listAssociate.forEach(pa -> {
//                    text.append(buildPersonRow(pa));
//                });
//            }
//            if (!listRegistered.isEmpty()) {
//                text.append("<b>Inscritos (").append(listRegistered.size()).append(")</b>\n");
//                listRegistered.forEach(pr -> {
//                    text.append(buildPersonRow(pr));
//                });
//            }
//            if (!listConfirmed.isEmpty()) {
//                text.append("<b>Confirmados (").append(listConfirmed.size()).append(")</b>\n");
//                listConfirmed.forEach(pc -> {
//                    text.append(buildPersonRow(pc));
//                });
//            }
//            if(listAll.isEmpty()){
//                text.append("Sem inscritos ainda\n");
//            }
//            Integer sum = listAssociate.size() + listRegistered.size() + listConfirmed.size();
//            BigDecimal percent = BigDecimal.valueOf(sum  / .60).setScale(2, RoundingMode.HALF_EVEN);
//            text.append("Lotação atual: <b>").append(percent.doubleValue()).append("%</b>  <i>").append(sum).append(" de 60</i>");
//            EditMessageText edit = new EditMessageText();
//            edit.setChatId(chatId);
//            edit.setMessageId(messageId);
//            edit.setText(text.toString());
//            edit.setReplyMarkup(buildKeyboard(ACTION_MAIN));
//            edit.enableHtml(true);
//            execute(edit);
//        }).onFailure(e -> custoLog(e));
//    }
//
//    private void actionContract(Update update){
//        Long chatId        = update.getCallbackQuery().getMessage().getChatId();
//        Integer messageId  = update.getCallbackQuery().getMessage().getMessageId();
//        StringBuilder text = new StringBuilder();
//        text.append("\uD83D\uDC64 Contrato de Adesão\n\n");
//        Try.run(() -> {
//            Optional<Person> p = personRepository.findByTelegram(chatId);
//            if(p.isPresent() && !StringUtils.isEmpty(p.get().getCpf())){
//                EditMessageText edit = new EditMessageText();
//                edit.setMessageId(messageId);
//                edit.setText("....");
//                edit.setChatId(String.valueOf(chatId));
//                execute(edit);
//                SendDocument document = new SendDocument();
//                document.setCaption("Abra o arquivo, leia atentamente , feche e depois escolha uma das opçoões abaixo");
//                document.setChatId(chatId);
//                File contract = PdfBuilder.create(p.get().getName(), p.get().getCpf(), DateUtil.nowExtense());
//                document.setNewDocument(contract);
//                document.setReplyMarkup(buildKeyboard(ACTION_CONTRACT));
//                Integer id = sendDocument(document).getMessageId();
//                messageRepository.save(Message.builder().message(id).telegram(chatId).build());
//            }else{
//                text.append(" Você ainda não possui as informações necessárias para gerar o contrato, contate @midianet e informe a situação.");
//                EditMessageText edit = new EditMessageText();
//                edit.setChatId(chatId);
//                edit.setMessageId(messageId);
//                edit.setText(text.toString());
//                edit.setReplyMarkup(buildKeyboard(ACTION_MAIN));
//                edit.enableHtml(true);
//                execute(edit);
//            }
//        }).onFailure(e -> custoLog(e));
//    }
//

//
//    private void actionSubscrible(Update update, Person.Contract contract){
//        Long chatId        = update.getCallbackQuery().getMessage().getChatId();
//        Integer messageId  = update.getCallbackQuery().getMessage().getMessageId();
//        Try.run(() ->{
//            DeleteMessage delete = new DeleteMessage();
//            delete.setMessageId(messageId);
//            delete.setChatId(String.valueOf(chatId));
//            execute(delete);
//            personRepository.updateSubscrible(chatId,contract);
//        }).onFailure(e -> custoLog(e));
//        actionRecovery(update);
//    }
//
//    private void actionFlux(Update update){
//        Try.run(() -> {
//            Long chatId        = update.getCallbackQuery().getMessage().getChatId();
//            Integer messageId  = update.getCallbackQuery().getMessage().getMessageId();
//            StringBuilder text = new StringBuilder();
//            text.append("\uD83D\uDCC8 Fluxo da Inscrição\n")
//                    .append("<a href=\"http://latinowarego.tk//site/images/fluxo2018.jpg\">.</a>");
//            EditMessageText edit = new EditMessageText();
//            edit.setChatId(chatId);
//            edit.enableWebPagePreview();
//            edit.setMessageId(messageId);
//            edit.setText(text.toString());
//            edit.setReplyMarkup(buildKeyboard(ACTION_MAIN));
//            edit.enableHtml(true);
//            execute(edit);
//        }).onFailure(e -> custoLog(e));
//    }
//
//    private void actionInvalid(Update update){
//        Try.run(() -> {
//            Long chatId       = update.getMessage().getChatId();
//            DeleteMessage delete = new DeleteMessage();
//            messageRepository.findByTelegram(chatId).ifPresent(m -> {
//                delete.setChatId(chatId.toString());
//                delete.setMessageId(m.getMessage());
//            });
//            if(delete.getMessageId() != null){
//                execute(delete);
//            }
//            SendMessage send = new SendMessage();
//            String text = "Ei eu sou um bot (Robô), não adianta me enviar mensagens, use os botoes abaixo.";
//            send.setChatId   (chatId);
//            send.setText(text);
//            send.setReplyMarkup(buildKeyboard(ACTION_MAIN));
//            send.enableHtml(true);
//            Integer messageId = execute(send).getMessageId();
//            messageRepository.save(Message.builder().message(messageId).telegram(chatId).build());
//        }).onFailure(e -> custoLog(e));
//    }
//
//    private void actionPhoto(Update update){
//        StringBuilder ret = new StringBuilder("Foto recebida com sucesso.");
//        Long chatId = update.getMessage().getChatId();
//        Try.run(() -> {
//            List<PhotoSize> photos = update.getMessage().getPhoto();
//            int idMax = 3;
//            System.out.println(photos.size());
//            if(photos.size() <= 3) {
//                idMax = photos.size() -1;
//            }
//            GetFile getter = new GetFile();
//            String fId = photos.get(idMax).getFileId();
////                String fId = photos.stream()
////                                     .sorted(Comparator.comparing(PhotoSize::getFileSize).reversed())
////                                     .findFirst()
////                                     .orElse(null).getFileId();
//            getter.setFileId(fId);
//            File f = downloadFile(execute(getter));
//            byte[] bytes = Files.readAllBytes(f.toPath());
//            photoRepository.insert(Photo.builder().telegram(chatId).photo(bytes).build());
//            EditMessageText edit = new EditMessageText();
//            messageRepository.findByTelegram(chatId).ifPresent(m -> {
//                edit.setChatId(chatId.toString());
//                edit.setMessageId(m.getMessage());
//                edit.setText("...");
//            });
//            if(!ObjectUtils.isEmpty(edit.getMessageId())){
//                execute(edit);
//            }
//        }).onFailure(e -> {
//            ret.delete(0,ret.length());
//            ret.append("Ocorreu um erro ao enviar a foto, tente novamente ou entre em contato com @midianet");
//            custoLog(e);
//        });
//        Try.run(() ->{
//            SendMessage send = new SendMessage();
//            String text = ret.toString();
//            send.setChatId(chatId);
//            send.setText(text);
//            send.setReplyMarkup(buildKeyboard(ACTION_MAIN));
//            send.enableHtml(true);
//            Integer messageId = execute(send).getMessageId();
//            messageRepository.save(Message.builder().message(messageId).telegram(chatId).build());
//        }).onFailure(e -> custoLog(e));
//    }
//
//    private InlineKeyboardMarkup buildKeyboard(String scene) {
//        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
//        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
//        ArrayList<InlineKeyboardButton> line = new ArrayList();
//        InlineKeyboardButton button;
//
//        switch (scene) {
//            case ACTION_START:
//                button = new InlineKeyboardButton();
//                button.setText("\uD83D\uDC4D OK Continuar");
//                button.setCallbackData(ACTION_CONTINUE);
//                line.add(button);
//                buttons.add(line);
//                break;
//            case ACTION_CONTRACT:
//                button = new InlineKeyboardButton();
//                button.setText("\uD83D\uDC4D Concordo");
//                button.setCallbackData(ACTION_AGREE);
//                line.add(button);
//                button = new InlineKeyboardButton();
//                button.setText("\uD83D\uDC4E Discordo");
//                button.setCallbackData(ACTION_DESAGREE);
//                line.add(button);
//                buttons.add(line);
//                break;
//            default:
//                button = new InlineKeyboardButton();
//                button.setText("\uD83D\uDCC8 Fluxo da Inscrição");
//                button.setCallbackData(ACTION_FLUX);
//                line.add(button);
//                buttons.add(line);
//                line = new ArrayList<>();
//                button = new InlineKeyboardButton();
//                button.setText("\uD83D\uDC68\u200D\uD83D\uDCBB\uD83D\uDC69\u200D\uD83D\uDCBB Inscritos");
//                button.setCallbackData(ACTION_LIST);
//                line.add(button);
//                buttons.add(line);
//                line = new ArrayList();
//                button = new InlineKeyboardButton();
//                button.setText("\uD83D\uDC64 Meu Perfil");
//                button.setCallbackData(ACTION_PROFILE);
//                line.add(button);
//                buttons.add(line);
//                line = new ArrayList();
//                button = new InlineKeyboardButton();
//                button.setText("\uD83D\uDCB3 Depósito");
//                button.setCallbackData(ACTION_ACCOUNT);
//                line.add(button);
//                button = new InlineKeyboardButton();
//                button.setText("\uD83D\uDCB0 Saldo");
//                button.setCallbackData(ACTION_BALANCE);
//                line.add(button);
//                buttons.add(line);
//                line = new ArrayList();
//                button = new InlineKeyboardButton();
//                button.setText("\uD83D\uDCDD Contrato de Adesão");
//                button.setCallbackData(ACTION_CONTRACT);
//                line.add(button);
//                buttons.add(line);
//        }
//        keyboard.setKeyboard(buttons);
//        return keyboard;
//    }
//
//    private void custoLog(Throwable e ){
//        if(e.getMessage().equals("Error editing message text")){
//            log.log(Level.FINE,e.getMessage(),e);
//        }else{
//            log.log(Level.SEVERE,e.getMessage(),e);
//        }
//    }
