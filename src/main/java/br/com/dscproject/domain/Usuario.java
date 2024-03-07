package br.com.dscproject.domain;

import br.com.dscproject.enums.Genero;
import br.com.dscproject.enums.Perfis;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Fetch;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Data @NoArgsConstructor @AllArgsConstructor
@Entity
@Table(name="USUARIOS")
public class Usuario implements Serializable, UserDetails {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "USU_ID", nullable = false)
    private Long id;

    @Column(name = "USU_NOME", length = 100, nullable = false)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(name = "USU_GENERO", nullable = false)
    private Genero genero;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    @Column(name = "USU_DT_NASCIMENTO", nullable = true)
    private Date nascimento;

    @Column(name = "USU_EMAIL", length = 512, nullable = false, unique = true)
    private String email;

    @Column(name = "USU_LOGIN", length = 40, nullable = false, unique = true)
    private String login;

    @Column(name = "USU_SENHA", length = 1024, nullable = false)
    private String senha;

    @Enumerated(EnumType.STRING)
    @Column(name = "USU_PERFIL", nullable = false)
    private Perfis perfil;

    @OneToMany(mappedBy="usuario", fetch = FetchType.LAZY)
    @Cascade(value = org.hibernate.annotations.CascadeType.ALL)
    private List<InstituicaoFinanceiraUsuario> instituicoesFinanceirasUsuario;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "REGISTRO_FINANCEIRO_USUARIO",
            joinColumns = { @JoinColumn(name = "USU_ID") },
            inverseJoinColumns = { @JoinColumn(name = "REFI_ID") }
    )
    private Set<RegistroFinanceiro> registrosFinanceiros;

    @Transient
    private List<Pagamento> pagamentos;

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
