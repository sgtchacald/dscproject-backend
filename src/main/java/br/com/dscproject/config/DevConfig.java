package br.com.dscproject.config;

import br.com.dscproject.services.DBService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.util.StringUtils;

@Configuration
@Profile(value = "dev")
public class DevConfig {

    @Autowired
    private DBService dbService;

    @Value("${spring.jpa.hibernate.ddl-auto}")
    private String dllAuto = "create";

    @Bean
    public boolean instanciarBancoDeDados(){
        if(StringUtils.pathEquals("create-drop", dllAuto)
            || StringUtils.pathEquals("create", dllAuto)){
                dbService.instanciarBancoDeDados();
                return true;
        }

        return false;
    }
}
