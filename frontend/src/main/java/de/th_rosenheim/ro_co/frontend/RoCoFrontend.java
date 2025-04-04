package de.th_rosenheim.ro_co.frontend;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.Theme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Theme(value = "ro-co")
public class RoCoFrontend implements AppShellConfigurator {

	public static void main(String[] args) {
		SpringApplication.run(RoCoFrontend.class, args);
	}

}
