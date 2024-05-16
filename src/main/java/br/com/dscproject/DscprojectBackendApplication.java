package br.com.dscproject;

import br.com.dscproject.services.AuditoriaService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
public class DscprojectBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(DscprojectBackendApplication.class, args);
	}

}
