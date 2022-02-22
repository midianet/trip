package midianet.trip.bot.group;
//
//import midianet.road.bussines.PartnerBussines;
//import org.apache.log4j.Logger;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//import org.telegram.telegrambots.api.methods.send.SendLocation;
//import org.telegram.telegrambots.api.methods.send.SendMessage;
//import org.telegram.telegrambots.api.methods.send.SendPhoto;
//import org.telegram.telegrambots.api.objects.Update;
//import org.telegram.telegrambots.api.objects.User;
//import org.telegram.telegrambots.bots.TelegramLongPollingBot;
//import org.telegram.telegrambots.exceptions.TelegramApiException;
//
//import javax.annotation.PostConstruct;
//import java.io.File;
//import java.text.Normalizer;
//import java.util.Calendar;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.concurrent.Executors;
//import java.util.concurrent.ScheduledExecutorService;
//import java.util.concurrent.TimeUnit;

//@Component
public class AlienBot {//extends TelegramLongPollingBot {
//    private Logger log = Logger.getLogger(getClass());
//
//    private static final String user = "Alien56";
//    private static final String token = "416385795:AAHJ-QhhGUM1jQi08a5zCCVbZdgeD4jymxs";
//    private static final Long   group = -1001140779263l; //-1001107766994l
//
//    ScheduledExecutorService execService;
//
//    private Map<Integer,Integer> calls = new HashMap<>();
//
//    private Map<String,String> badStrings = new HashMap<>();
//
//    @PostConstruct
//    public void task(){
//        badStrings.put("cu","cu");
//        badStrings.put("buceta","buceta");
//        badStrings.put("caralho","caralho");
//        badStrings.put("porra","porra");
//        badStrings.put("cacete","cacete");
//        badStrings.put("vai tomar no cu","vai tomar no cu");
//
//        execService = Executors.newScheduledThreadPool(1);
//        execService.scheduleAtFixedRate(()->{
//            calls = new HashMap<>();
//        }, 0, 24, TimeUnit.HOURS);
//        execService.scheduleAtFixedRate(() -> {
//            if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) == 12) {
//                actionDays();
//            }
//        },0,1,TimeUnit.HOURS);
//    }
//
//    @Autowired
//    private PartnerBussines partnerBussines;
//
//    @Autowired
//    private PieChart pieChart;
//
//    @Override
//    public String getBotToken() {
//        return token;
//    }
//
//    @Override
//    public String getBotUsername() {
//        return user;
//    }
//
//    @Override
//    public void onUpdateReceived(final Update update) {
//        try {
//            if (update.hasMessage()) {
//                if (update.getMessage().isGroupMessage() || update.getMessage().isSuperGroupMessage()) {
//                    if (update.getMessage().getNewChatMembers() != null) {
//                        update.getMessage().getNewChatMembers().forEach(m -> {
//                            if ("alien56_bot".equals(m.getUserName())) {
//                                actionJoinMe(update);
//                            } else {
//                                actionJoinMember(update, m);
//                            }
//                        });
//                    } else if (update.getMessage().hasText()) {
//                        final Integer id = update.getMessage().getFrom().getId();
//
//                        String message = update.getMessage().getText().toLowerCase();
//                        if (message.startsWith("/")) message = message.replace("/","");
//                        message = normalize(message);
//
//                        if (message.equals("lotacao") || message.equals("ola") || message.equals("dia")  || message.equals("saida") || badStrings.containsKey(message)) {
//                            if (calls.containsKey(id)) {
//                                calls.put(id, calls.get(id) + 1);
//                                if (calls.get(id) > 5) {
//                                    actionInsult(update);
//                                    return;
//                                }
//                            } else {
//                                calls.put(id, 0);
//                            }
//                            if (message.equals("lotacao")) {
//                                actionCount(update);
//                            } else if (message.equals("ola")) {
//                                actionSalutation(update);
//                            }else if (message.equals("dia")){
//                                actionDays();
//                            }else if(badStrings.containsKey(message)){
//                                actionBadString(update) ;
//                            }else if(message.equals("saida")){
//                                actionSaida(update);
//                            }
//                        }
//                    }
//                }
//            }
//        }catch (Exception e){
//            log.error(e);
//        }
//    }
//
//    private String normalize(String text) {
//        return Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
//    }
//
//    private void actionInsult(final Update update){
//        final Long chatId = update.getMessage().getChat().getId();
//        try {
//            final SendMessage send = new SendMessage();
//            send.enableHtml(true);
//            send.setChatId(chatId.toString());
//            send.setText("Vai te lasca <b>".concat(update.getMessage().getFrom().getFirstName().concat( "</b>\nVocê ja me encheu o saco hoje, me deixe em paz\nArrume o que fazer \uD83D\uDC4A \uD83D\uDD95\uD83D\uDD95\uD83D\uDD95")));
//            sendMessage(send);
//        } catch (Exception e) {
//            log.error(e);
//        }
//    }
//
//    private void actionSaida(final Update update){
//        final Long chatId = update.getMessage().getChat().getId();
//        try {
//            final SendMessage send = new SendMessage();
//            send.enableHtml(true);
//            send.setChatId(chatId.toString());
//            send.setText("⏰ Saíremos as <b>00:00</b> do dia <b>17 de Outubro</b>\n No endereço abaixo \uD83D\uDC4D\uD83D\uDC4D");
//            sendMessage(send);
//            final SendLocation sendl = new SendLocation();
//            sendl.setLongitude(-49.244799f);
//            sendl.setLatitude(-16.677259f);
//            sendl.setChatId(chatId.toString());
//            sendLocation(sendl);
//        } catch (Exception e) {
//            log.error(e);
//        }
//    }
//
//    private void actionCount(final Update update){
//        final double confirm = partnerBussines.listConfirmeds().size();
//        final Long chatId = update.getMessage().getChat().getId();
//        //final DecimalFormat df = new DecimalFormat();
//        //df.setMaximumFractionDigits(2);
//        try {
//            pieChart.createOccupancy(confirm,55d,200,200);
//            final SendPhoto send = new SendPhoto();
//            send.setChatId(chatId.toString());
//            send.setNewPhoto(new File("/tmp/occupation.jpg"));
//            sendPhoto(send);
//            //final SendMessage send = new SendMessage();
//            //send.enableHtml(true);
//            //send.setText("Companheiros estamos com <b>".concat(df.format(confirm / 55d *100d)).concat("%</b> lotados\n").concat(confirm < 55 ? "Precisamos convidar mais companheiros \uD83D\uDE31": "Parabéns estamos lotados \uD83D\uDC4F\uD83D\uDC4F\uD83D\uDC4F"));
//            //sendMessage(send).getMessageId();
//        } catch (Exception e) {
//            log.error(e);
//        }
//
//    }
//
//
//    private void actionBadString(final Update update){
//        final Long chatId = update.getMessage().getChat().getId();
//        try {
//            final SendMessage send = new SendMessage();
//            send.enableHtml(true);
//            send.setChatId(chatId.toString());
//            send.setText("Ei <b>".concat(update.getMessage().getFrom().getFirstName().concat( "</b>\nNão permito palavrões nesse grupo,\nmantenha-se na linha ou será expulso\n \uD83D\uDE21\uD83D\uDE21\uD83D\uDE21\uD83D\uDE21\uD83D\uDE21")));
//            sendMessage(send).getMessageId();
//        } catch (Exception e) {
//            log.error(e);
//        }
//    }
//
//    public void actionConfirmMember(final String member){
//        try {
//            final Integer confirm = partnerBussines.listConfirmeds().size();
//            final SendMessage send = new SendMessage();
//            send.enableHtml(true);
//            send.setChatId(group.toString());
//            send.setText("Olá companheiros\nConfirmamos mais um\nDeem boas vindas à <b>".concat(member).concat("</b>\nSomos agora <b>").concat(confirm.toString()).concat("</b>"));
//            sendMessage(send).getMessageId();
//        } catch (Exception e) {
//            log.error(e);
//        }
//    }
//
//    private void actionSalutation(final Update update){
//        final Long chatId = update.getMessage().getChat().getId();
//        try {
//            final SendMessage send = new SendMessage();
//            send.enableHtml(true);
//            send.setChatId(chatId.toString());
//            send.setText("Ola <b>".concat(update.getMessage().getFrom().getFirstName().concat( "</b>\nEstou por aqui só observando\nBastante animado\nSe anime também Hurrul\uD83D\uDD7A\uD83D\uDD7A\uD83D\uDD7A\uD83D\uDD7A")));
//            sendMessage(send).getMessageId();
//        } catch (Exception e) {
//            log.error(e);
//        }
//    }
//
//    private void actionJoinMe(final Update update){
//        final Long chatId = update.getMessage().getChat().getId();
//        try {
//            final SendMessage send = new SendMessage();
//            send.enableHtml(true);
//            send.setChatId(chatId.toString());
//            send.setText("Ola coampanheiros\nEu sou o <b>Alien, o 56º Passageiro</b>\nVou ficar por aqui pra manter todos informados da nossa caravana\nUm abraço a todos \uD83D\uDC4D");
//            sendMessage(send).getMessageId();
//        } catch (Exception e) {
//            log.error(e);
//        }
//    }
//
//    private void actionDays(){
//        try {
//            final SendMessage send = new SendMessage();
//            send.enableHtml(true);
//            send.setChatId(group.toString());
//            final Calendar c1 = Calendar.getInstance();
//            final Calendar c2 = Calendar.getInstance();
//            c2.set(Calendar.DAY_OF_MONTH,17);
//            c2.set(Calendar.MONTH,Calendar.OCTOBER);
//            int dias = c2.get(Calendar.DAY_OF_YEAR) - c1.get(Calendar.DAY_OF_YEAR);
//            send.setText("Olá companheiros\nFaltam <b>".concat(String.valueOf(dias)).concat("</b> dias estamos quase lá\nSe prepare para a melhor viagem da sua vida\n\uD83D\uDE01\uD83D\uDE01\uD83D\uDE01\uD83D\uDE01"));
//            sendMessage(send).getMessageId();
//        } catch (Exception e) {
//            log.error(e);
//        }
//    }
//
//    private void actionJoinMember(final Update update, final User u){
//        final Long chatId = update.getMessage().getChatId();
//        try {
//            final SendMessage send = new SendMessage();
//            send.setChatId(chatId.toString());
//            send.enableHtml(true);
//            send.setText("Bem vindo <b>".concat(u.getFirstName()).concat("</b>\nEssa viagem será inesquecível,\nVoce não poderia fircar de fora \uD83D\uDC4F\uD83D\uDC4F\uD83D\uDC4F"));
//            sendMessage(send).getMessageId();
//        } catch (Exception e) {
//            log.error(e);
//        }
//    }

}
