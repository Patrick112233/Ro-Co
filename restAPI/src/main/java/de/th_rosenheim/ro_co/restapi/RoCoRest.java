package de.th_rosenheim.ro_co.restapi;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;



@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
public class RoCoRest{


    public static void main(String[] args) {
        SpringApplication.run(RoCoRest.class, args);
    }




}

