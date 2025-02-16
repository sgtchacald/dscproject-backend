package br.com.dscproject.utils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public final class DateUtils {
    private DateUtils() {
        throw new IllegalStateException("DateUtils é uma classe de utilidade e não pode ser instanciada.");
    }

    public static boolean isDataMenorQueHoje(String dataString, String dataFormato) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dataFormato);
            LocalDate data = LocalDate.parse(dataString, formatter);
            LocalDate hoje = LocalDate.now();
            return data.isBefore(hoje);
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public static boolean isDiaDoMesValido(Integer dia) {
        try {
            return (dia > 0) && (dia < 32);
        } catch (Exception e) {
            return false;
        }
    }

    public static LocalDate retornaLocalDate(String dataString, String dataFormato) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dataFormato);
            return LocalDate.parse(dataString, formatter);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public static LocalDate parseData(String data) {
        // Formato para datas no padrão "dd/MM/yyyy"
        DateTimeFormatter formatoDataBrasil = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // Tenta parsear no formato "dd/MM/yyyy"
        try {
            return LocalDate.parse(data, formatoDataBrasil);
        } catch (DateTimeParseException e) {
            // Se falhar, tenta parsear no formato ISO 8601 com UTC
            try {
                // Converte para Instant e depois para LocalDate
                Instant instant = Instant.parse(data);
                return instant.atZone(ZoneId.systemDefault()).toLocalDate();
            } catch (DateTimeParseException ex) {
                // Se ambos falharem, retorna null
                return null;
            }
        }
    }
}
