package br.com.dscproject.validation;

import br.com.dscproject.controller.Exception.FieldMessage;
import br.com.dscproject.domain.Usuario;
import br.com.dscproject.dto.UsuarioDTO;
import br.com.dscproject.repository.UsuarioRepository;
import br.com.dscproject.validation.constraints.UsuarioNovo;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

public class UsuarioValidation implements ConstraintValidator<UsuarioNovo, UsuarioDTO> {

	@Autowired
	private UsuarioRepository usuarioRepository;

    public boolean isValid(UsuarioDTO usuarioDTO, ConstraintValidatorContext context) {

		List<FieldMessage> list = new ArrayList<>();

		boolean semErros = false;

		List<Usuario> resultBuscaPorEmail = usuarioRepository.findByCredenciaisList(usuarioDTO.getEmail());

		if(!resultBuscaPorEmail.isEmpty()){
			list.add(new FieldMessage("email", "Este E-mail já existe no sistema"));
		}

		List<Usuario> resultBuscaPorLogin = usuarioRepository.findByCredenciaisList(usuarioDTO.getLogin());

		if(!resultBuscaPorLogin.isEmpty()){
			list.add(new FieldMessage("login", "Este nome de usuário já existe no sistema"));
		}

		if(usuarioDTO.getGenero() == null){
			list.add(new FieldMessage("genero", "Você deve atribuir um gênero para o usuário"));
		}

		if(list.isEmpty()){
			semErros = true;
		}else{
			for (FieldMessage e : list) {
				context.disableDefaultConstraintViolation();
				context.buildConstraintViolationWithTemplate(e.getMessage()).addPropertyNode(e.getFieldName()).addConstraintViolation();
			}
		}

		return semErros;

	}


}