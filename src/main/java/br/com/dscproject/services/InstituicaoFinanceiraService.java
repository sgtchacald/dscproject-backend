package br.com.dscproject.services;

import br.com.dscproject.domain.InstituicaoFinanceira;
import br.com.dscproject.repository.InstituicaoFinanceiraRepository;
import br.com.dscproject.services.exceptions.DataIntegrityException;
import br.com.dscproject.services.exceptions.ObjectNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class InstituicaoFinanceiraService {

    @Autowired
    private InstituicaoFinanceiraRepository instituicaoFinanceiraRepository;

    public List<InstituicaoFinanceira> buscarTodos() {
        return (List<InstituicaoFinanceira>) instituicaoFinanceiraRepository.findAll();
    }

    public InstituicaoFinanceira buscarPorId(Long id) {
        Optional<InstituicaoFinanceira> instituicaoFinanceira = instituicaoFinanceiraRepository.findById(id);

        return instituicaoFinanceira
                .orElseThrow(
                   ()-> new ObjectNotFoundException(
                        "Objeto não encontrado!" + "Id:" + id + ", Tipo: "  + InstituicaoFinanceira.class.getName()
                   )
               );
    }

    @Transactional
    public InstituicaoFinanceira inserir(InstituicaoFinanceira instituicaoFinanceira) {
        InstituicaoFinanceira instituicaoFinanceiraComMesmoNome = instituicaoFinanceiraRepository.findByNome(instituicaoFinanceira.getNome());

        if(instituicaoFinanceiraComMesmoNome != null) {
            throw new RuntimeException("Já existe uma instituição financeira com esse nome.");
        }

        instituicaoFinanceiraRepository.save(instituicaoFinanceira);

        return instituicaoFinanceira;
    }

    public InstituicaoFinanceira editar(InstituicaoFinanceira data) throws ObjectNotFoundException {

        InstituicaoFinanceira instituicaoFinanceiraBanco = this.buscarPorId(data.getId());

        InstituicaoFinanceira instituicaoFinanceiraComMesmoNome = instituicaoFinanceiraRepository.findByNome(data.getNome());

        if(instituicaoFinanceiraComMesmoNome != null && !instituicaoFinanceiraComMesmoNome.getId().equals(data.getId())) {
            throw new RuntimeException("Já existe uma instituição financeira com esse nome.");
        }

        instituicaoFinanceiraBanco.setNome(data.getNome());
        instituicaoFinanceiraBanco.setTipoInstituicao(data.getTipoInstituicao());

        return instituicaoFinanceiraRepository.save(instituicaoFinanceiraBanco);
    }

    public void excluir(Long id) throws ObjectNotFoundException {

        this.buscarPorId(id);

        try {
            instituicaoFinanceiraRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityException("Não foi possível excluir este registro pois existem registros vinculados a ele.");
        }

    }

}
