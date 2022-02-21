package midianet.trip;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.telegram.telegrambots.ApiContextInitializer;
//import org.telegram.telegrambots.TelegramBotsApi;
//import org.telegram.telegrambots.exceptions.TelegramApiException;

@SpringBootApplication
public class Application {

    //private TelegramBotsApi telegram;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

//    @Autowired
//    private void initBot(final RoadBot roadBot, final AlienBot alienBot ) {
//        try {
//            telegram = new TelegramBotsApi();
//            log.info("Inicializando RoadBot...");
//            telegram.registerBot(roadBot);
//            log.info("Inicializando AlienBot...");
//            telegram.registerBot(alienBot);
//            log.info("RoadBot iniciado.");
//            log.info("AlienBot iniciado.");
//        } catch (Exception e) {
//            log.error(e);
//        }
//    }

}