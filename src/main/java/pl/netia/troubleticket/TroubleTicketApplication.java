package pl.netia.troubleticket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class TroubleTicketApplication {

    public static void main(String[] args) {
        SpringApplication.run(TroubleTicketApplication.class, args);
    }
}
