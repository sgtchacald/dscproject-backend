package br.com.dscproject.model;

import br.com.dscproject.enums.Perfis;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Cascade;
import org.springframework.boot.autoconfigure.task.TaskExecutionProperties;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor
@Entity
@Table(name="USUARIOS")
public class Usuario implements Serializable, UserDetails {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "US_ID", nullable = false)
    private Long id;

    @Column(name = "US_NOME", length = 100, nullable = false)
    private String nome;

    @Column(name = "US_GENERO", length = 1, nullable = true)
    private String genero;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    @Column(name = "US_DT_NASCIMENTO", nullable = true)
    private Date nascimento;

    @Column(name = "US_EMAIL", length = 512, nullable = false, unique = true)
    private String email;

    @Column(name = "US_LOGIN", length = 40, nullable = false, unique = true)
    private String login;

    @Column(name = "US_SENHA", length = 1024, nullable = false)
    private String senha;

    @Enumerated(EnumType.STRING)
    @Column(name = "US_PERFIL")
    private Perfis perfil;

    @OneToMany(mappedBy="instituicaoFinanceira", fetch = FetchType.LAZY, cascade=CascadeType.ALL)
    @Cascade(value = org.hibernate.annotations.CascadeType.ALL)
    private List<InstituicaoFinanceiraUsuario> instituicoesFinanceirasUsuario;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if(this.perfil.equals(Perfis.ADMIN)){
            return List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ROLE_USER"));
        }else {
            return List.of(new SimpleGrantedAuthority("ROLE_USER"));
        }
    }

    @Override
    public String getPassword() {
        return senha;
    }

    @Override
    public String getUsername() {
        return login;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
