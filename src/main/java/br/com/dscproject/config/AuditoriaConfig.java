package br.com.dscproject.config;

import br.com.dscproject.services.AuditoriaService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.envers.repository.config.EnableEnversRepositories;
import org.springframework.data.envers.repository.support.EnversRevisionRepositoryFactoryBean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditoriaService")
@EnableEnversRepositories
@EnableJpaRepositories(basePackages = {"br.com.dscproject"}, repositoryFactoryBeanClass = EnversRevisionRepositoryFactoryBean.class)
public class AuditoriaConfig {
    @Bean
    AuditorAware<String> auditorProvider(){
        return new AuditoriaService();
    }

}
