package br.com.dscproject.services;

import br.com.dscproject.domain.InstituicaoFinanceira;
import br.com.dscproject.domain.InstituicaoFinanceiraUsuario;
import br.com.dscproject.domain.Usuario;
import br.com.dscproject.dto.InstituicaoFinanceiraDTO;
import br.com.dscproject.dto.InstituicaoFinanceiraUsuarioDTO;
import br.com.dscproject.repository.InstituicaoFinanceiraRepository;
import br.com.dscproject.repository.InstituicaoFinanceiraUsuarioRepository;
import br.com.dscproject.repository.UsuarioRepository;
import br.com.dscproject.services.exceptions.DataIntegrityException;
import br.com.dscproject.services.exceptions.ObjectNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class InstituicaoFinanceiraUsuarioService {

    @Autowired
    private InstituicaoFinanceiraUsuarioRepository instituticaoFinanceiraUsuarioRepository;

    @Autowired
    private InstituicaoFinanceiraUsuarioRepository instituicaoFinanceiraUsuarioRepository;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public List<InstituicaoFinanceiraUsuario> buscarTodosPorUsuario() {
        String loginUsuarioToken = tokenService.validarToken(tokenService.recuperarToken(request));
        Usuario usuario = usuarioRepository.findByLogin(loginUsuarioToken);
        return (List<InstituicaoFinanceiraUsuario>) instituticaoFinanceiraUsuarioRepository.findByUsuario(usuario);
    }

    public InstituicaoFinanceiraUsuario buscarPorId(Long id) {
        Optional<InstituicaoFinanceiraUsuario> obj = instituticaoFinanceiraUsuarioRepository.findById(id);

        return obj.orElseThrow(
           ()-> new ObjectNotFoundException(
                "Objeto não encontrado!" + "Id:" + id + ", Tipo: "  + InstituicaoFinanceiraUsuario.class.getName()
           )
       );
    }

    @Transactional
    public InstituicaoFinanceiraUsuario inserir(InstituicaoFinanceiraUsuarioDTO data) {

        InstituicaoFinanceiraUsuario instituicaoFinanceiraUsuario = new InstituicaoFinanceiraUsuario();
        InstituicaoFinanceira instFin = new InstituicaoFinanceira();

        BeanUtils.copyProperties(data.getInstituicaoFinanceira(), instFin);
        data.setInstituicaoFinanceira(new InstituicaoFinanceiraDTO());

        BeanUtils.copyProperties(data, instituicaoFinanceiraUsuario);

        String loginUsuarioToken = tokenService.validarToken(tokenService.recuperarToken(request));
        Usuario usuario = usuarioRepository.findByLogin(loginUsuarioToken);

        instituicaoFinanceiraUsuario.setInstituicaoFinanceira(instFin);
        instituicaoFinanceiraUsuario.setUsuario(usuario);

        return instituticaoFinanceiraUsuarioRepository.save(instituicaoFinanceiraUsuario);

    }

    public InstituicaoFinanceiraUsuario editar(InstituicaoFinanceiraUsuarioDTO data) throws ObjectNotFoundException {

        InstituicaoFinanceiraUsuario instituticaoFinanceiraUsuarioBanco = this.buscarPorId(data.getId());

        instituticaoFinanceiraUsuarioBanco.setAgencia(data.getAgencia());
        instituticaoFinanceiraUsuarioBanco.setConta(data.getConta());
        instituticaoFinanceiraUsuarioBanco.setNomeGerente(data.getNomeGerente());
        instituticaoFinanceiraUsuarioBanco.setTelefoneGerente(data.getTelefoneGerente());

        InstituicaoFinanceira instituicaoFinanceira = new InstituicaoFinanceira();
        BeanUtils.copyProperties(data.getInstituicaoFinanceira(), instituicaoFinanceira);
        instituticaoFinanceiraUsuarioBanco.setInstituicaoFinanceira(instituicaoFinanceira);

        return instituticaoFinanceiraUsuarioRepository.save(instituticaoFinanceiraUsuarioBanco);
    }

    public void excluir(Long id) throws ObjectNotFoundException {
        this.buscarPorId(id);
        try {
            instituticaoFinanceiraUsuarioRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityException("Não foi possível excluir este registro pois existem registros vinculados a ele.");
        }
    }
}
