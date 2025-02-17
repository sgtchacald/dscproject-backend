package br.com.dscproject.services;

import br.com.dscproject.domain.Despesa;
import br.com.dscproject.domain.Usuario;
import br.com.dscproject.repository.PagamentoRepository;
import br.com.dscproject.repository.DespesaRepository;
import br.com.dscproject.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class PagamentoService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private DespesaRepository despesaRepository;

    @Autowired
    private PagamentoRepository pagamentoRepository;

    @Transactional
    public Despesa inserir(Despesa registroFinanceiro) {
        //Resgata responsáveis pelo registro financeiro
        List<Usuario> usuariosResponsaveis = new ArrayList<Usuario>();

        Integer qtdParcelas = registroFinanceiro.getQtdParcela() == 0 ? 1 : registroFinanceiro.getQtdParcela();

        despesaRepository.save(registroFinanceiro);

        return registroFinanceiro;
    }

}
