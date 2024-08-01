package br.com.dscproject.services;

import br.com.dscproject.domain.InstituicaoFinanceiraUsuario;
import br.com.dscproject.domain.RegistroFinanceiro;
import br.com.dscproject.domain.Usuario;
import br.com.dscproject.dto.RegistroFinanceiroDTO;
import br.com.dscproject.repository.InstituicaoFinanceiraUsuarioRepository;
import br.com.dscproject.repository.RegistroFinanceiroRepository;
import br.com.dscproject.repository.UsuarioRepository;
import br.com.dscproject.services.exceptions.ObjectNotFoundException;
import br.com.dscproject.utils.DateUtils;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
public class RegistroFinanceiroService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private InstituicaoFinanceiraUsuarioRepository instituicaoFinanceiraUsuarioRepository;

    @Autowired
    private RegistroFinanceiroRepository registroFinanceiroRepository;

    @Transactional
    public RegistroFinanceiro inserir(RegistroFinanceiroDTO data) {
        RegistroFinanceiro registroFinanceiro  = new RegistroFinanceiro();
        BeanUtils.copyProperties(data, registroFinanceiro);

        registroFinanceiro.setDtLancamento(Instant.now());

        if(data.getDtVencimento() != null) {
            registroFinanceiro.setDtVencimento(DateUtils.retornaLocalDate(data.getDtVencimento(), "dd/MM/yyyy"));
        }

        Optional<InstituicaoFinanceiraUsuario> ifu  = this.instituicaoFinanceiraUsuarioRepository.findById(data.getInstituicaoFinanceiraUsuarioId());
        if(ifu.isEmpty()){
            throw new ObjectNotFoundException("Não existe uma instituição financeira vinculada ao usuário com o id " + data.getInstituicaoFinanceiraUsuarioId() + ".");
        }

        InstituicaoFinanceiraUsuario instituicaoFinanceiraUsuario = ifu.get();

        registroFinanceiro.setInstituicaoFinanceiraUsuario(instituicaoFinanceiraUsuario);


        Set<Usuario> usuarios = new HashSet<Usuario>();
        for(Long usuarioId : data.getUsuariosResponsaveis()){
            Optional<Usuario> usuario = usuarioRepository.findById(usuarioId);
            if(usuario.isEmpty()){
                throw new ObjectNotFoundException("Não foi possível encontrar o usuario com o id  " + usuarioId + ".");
            }
            usuario.ifPresent(usuarios::add);
        }

        if(!usuarios.isEmpty()){
            registroFinanceiro.setUsuariosResponsaveis(usuarios);
        }

        registroFinanceiroRepository.save(registroFinanceiro);

        return registroFinanceiro;
    }

}
