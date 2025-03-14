package br.com.dscproject.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;
import org.hibernate.envers.Audited;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Data
@MappedSuperclass
@Audited
@EntityListeners(AuditingEntityListener.class)
public class AbstractAuditoria {

    @JsonIgnore
    @CreatedBy
    @Column(name = "audit_criado_por", length = 40, updatable = false)
    private String criadoPor;

    @JsonIgnore
    @CreatedDate
    @Column(name = "audit_data_criacao", updatable = false)
    private Instant dataCriacao = Instant.now();

    @JsonIgnore
    @LastModifiedBy
    @Column(name = "audit_alterado_por", length = 40)
    private String alteradoPor;

    @JsonIgnore
    @LastModifiedDate
    @Column(name = "audit_data_alteracao")
    private Instant dataAlteracao = Instant.now();

}
