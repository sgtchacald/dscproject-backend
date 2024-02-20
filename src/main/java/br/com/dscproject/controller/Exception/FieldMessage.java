package br.com.dscproject.controller.Exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FieldMessage implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;
	
	private String fieldName;
	private String message;
}
