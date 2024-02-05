package br.com.dscproject.config;

import br.com.dscproject.services.DBService;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("dev")
public class DevConfig {

    private DBService dbService;
}
