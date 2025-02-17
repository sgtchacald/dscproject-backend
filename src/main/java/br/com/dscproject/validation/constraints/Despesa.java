package br.com.dscproject.validation.constraints;

import br.com.dscproject.validation.DespesaValidation;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target( { ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DespesaValidation.class)
@Documented
public @interface Despesa {

    String message() default "Erro de validação.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

}
