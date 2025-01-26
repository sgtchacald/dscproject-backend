package br.com.dscproject.validation;

import br.com.dscproject.controller.Exception.FieldMessage;
import br.com.dscproject.dto.RegistroFinanceiroDTO;
import br.com.dscproject.enums.TipoRegistroFinanceiro;
import br.com.dscproject.repository.UsuarioRepository;
import br.com.dscproject.utils.DateUtils;
import br.com.dscproject.validation.constraints.RegistroFinanceiro;
import io.micrometer.common.util.StringUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class RegistroFinanceiroValidation implements ConstraintValidator<RegistroFinanceiro, RegistroFinanceiroDTO> {

	@Autowired
	private UsuarioRepository usuarioRepository;

    public boolean isValid(RegistroFinanceiroDTO data, ConstraintValidatorContext context) {

		List<FieldMessage> list = new ArrayList<>();

		if(data.getDescricao().isBlank()){
			list.add(new FieldMessage("descricao", "O campo é obrigatório."));
		}

		if(data.getValor() == null){
			list.add(new FieldMessage("valor", "O campo é obrigatório."));
		}

		if(data.getValor() != null && data.getValor().compareTo(BigDecimal.ZERO) <= 0){
			list.add(new FieldMessage("valor", "O campo deve ser maior que 0."));
		}

		if(data.getTipoRegistroFinanceiro() == null){
			list.add(new FieldMessage("tipoRegistroFinanceiro", "O campo é obrigatório."));
		}

		if(data.getTipoRegistroFinanceiro() == null){
			list.add(new FieldMessage("tipoReceitaDespesa", "O campo é obrigatório."));
		}

		if(data.getTipoRegistroFinanceiro() != null
			&& data.getTipoRegistroFinanceiro().equals(TipoRegistroFinanceiro.DESPESA)
			&& StringUtils.isBlank(data.getDtVencimento())
			&& data.getQtdParcela() < 2 ){
				list.add(new FieldMessage("qtdParcela", "A quantidade de parcelas deve maior que 1."));
		}

		if(data.getInstituicaoFinanceiraUsuarioId() == null){
			list.add(new FieldMessage("instituicaoFinanceiraUsuarioId", "O campo é obrigatório."));
		}

		if(data.getUsuariosResponsaveis() == null || data.getUsuariosResponsaveis().isEmpty()){
			list.add(new FieldMessage("usuariosResponsaveis", "O campo é obrigatório."));
		}

		if(data.getTipoRegistroFinanceiro() != null
			&& data.getTipoRegistroFinanceiro().equals(TipoRegistroFinanceiro.DESPESA)
			&& data.getQtdParcela() > 1
			&& data.getDiaVencimento() == 0){
				list.add(new FieldMessage("diaVencimento", "O campo é obrigatório."));
		}

		if(data.getDiaVencimento() != 0 && !DateUtils.isDiaDoMesValido(data.getDiaVencimento())){
			list.add(new FieldMessage("diaVencimento", "O dia do vencimento deve estar entre 1 e 31."));
		}

		if(data.getTipoRegistroFinanceiro() != null
			&& data.getTipoRegistroFinanceiro().equals(TipoRegistroFinanceiro.DESPESA)
			&& data.getQtdParcela() == 0
			&& StringUtils.isBlank(data.getDtVencimento())){
				list.add(new FieldMessage("dtVencimento", "O campo é obrigatório."));
		}

		if(data.getDtVencimento() != null
				&& DateUtils.isDataMenorQueHoje(data.getDtVencimento(), "dd/MM/yyyy")){
			list.add(new FieldMessage("dtVencimento", "A data não pode ser menor que hoje."));
		}

		if(list.isEmpty()){
			return true;
		}else{
			for (FieldMessage e : list) {
				context.disableDefaultConstraintViolation();
				context.buildConstraintViolationWithTemplate(e.getMessage()).addPropertyNode(e.getFieldName()).addConstraintViolation();
			}
		}

		return false;
	}
}