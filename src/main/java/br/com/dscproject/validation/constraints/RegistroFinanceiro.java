package br.com.dscproject.validation.constraints;

import br.com.dscproject.validation.RegistroFinanceiroValidation;
import br.com.dscproject.validation.UsuarioValidation;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target( { ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = RegistroFinanceiroValidation.class)
@Documented
public @interface RegistroFinanceiro {

    String message() default "Erro de validação.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

}
