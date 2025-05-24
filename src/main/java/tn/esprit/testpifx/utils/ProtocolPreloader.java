package tn.esprit.testpifx.utils;

import javafx.application.Preloader;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A preloader that handles URL protocol requests.
 * This class is used to handle application links like account verification and password reset.
 */
public class ProtocolPreloader extends Preloader {
    private static final Logger logger = LoggerFactory.getLogger(ProtocolPreloader.class);
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        // The preloader doesn't need to display anything,
        // it just serves to handle protocol requests
    }
      @Override
    public void handleApplicationNotification(PreloaderNotification info) {
        if (info instanceof ProtocolNotification) {
            String url = ((ProtocolNotification) info).getUrl();
            logger.info("Handling URL: {}", url);
            TokenUrlHandler.handleUrl(url);
        }
    }
    
    /**
     * A notification containing a URL to be handled.
     */
    public static class ProtocolNotification implements PreloaderNotification {
        private final String url;
        
        public ProtocolNotification(String url) {
            this.url = url;
        }
        
        public String getUrl() {
            return url;
        }
    }
}
