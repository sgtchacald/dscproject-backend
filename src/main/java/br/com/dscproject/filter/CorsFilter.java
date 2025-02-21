package br.com.dscproject.filter;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.io.IOException;

@Configuration
public class CorsFilter implements Filter {

    private final Environment env;

    public CorsFilter(Environment env) {
        this.env = env;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponse res = (HttpServletResponse) response;
        HttpServletRequest req = (HttpServletRequest) request;

        // Aplica CORS apenas se estiver em produção
        if ("prod".equals(env.getProperty("spring.profiles.active"))) {
            res.setHeader("Access-Control-Allow-Origin", "https://dscproject.diegocordeiro.com.br");
            res.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            res.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
            res.setHeader("Access-Control-Allow-Credentials", "true");

            // Para requisições OPTIONS
            if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
                res.setStatus(HttpServletResponse.SC_NO_CONTENT);
                return;
            }
        }

        chain.doFilter(request, response);
    }
}
