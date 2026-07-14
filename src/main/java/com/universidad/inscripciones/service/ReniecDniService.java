package com.universidad.inscripciones.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.universidad.inscripciones.dto.publico.ReniecDniResponse;

@Service
public class ReniecDniService {

    private final RestClient restClient;
    private final String token;

    public ReniecDniService(
            RestClient.Builder builder,
            @Value("${app.reniec.api-url}") String apiUrl,
            @Value("${app.reniec.token}") String token) {
        this.restClient = builder
                .baseUrl(apiUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
        this.token = token;
    }

    public ReniecDniResponse consultarDni(String numero) {
        String dni = numero == null ? "" : numero.trim();

        if (!dni.matches("\\d{8}")) {
            throw new IllegalArgumentException("El DNI debe tener exactamente 8 digitos.");
        }

        try {
            DecolectaDniResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1/reniec/dni")
                            .queryParam("numero", dni)
                            .build())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .body(DecolectaDniResponse.class);

            if (response == null || isBlank(response.documentNumber())) {
                throw new IllegalArgumentException(
                        "El numero de documento DNI no se encuentra registrado en la RENIEC. Asegurate de haberlo escrito correctamente.");
            }

            return new ReniecDniResponse(
                    response.firstName(),
                    response.firstLastName(),
                    response.secondLastName(),
                    response.fullName(),
                    response.documentNumber());
        } catch (HttpClientErrorException.NotFound ex) {
            throw new IllegalArgumentException(
                    "El numero de documento DNI no se encuentra registrado en la RENIEC. Asegurate de haberlo escrito correctamente.");
        } catch (RestClientResponseException ex) {
            throw new IllegalArgumentException(
                    "No se pudo consultar RENIEC en este momento. Verifica el DNI o intenta nuevamente.");
        } catch (RestClientException ex) {
            throw new IllegalArgumentException(
                    "No se pudo conectar con RENIEC en este momento. Intenta nuevamente en unos segundos.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private record DecolectaDniResponse(
            @JsonProperty("first_name") String firstName,
            @JsonProperty("first_last_name") String firstLastName,
            @JsonProperty("second_last_name") String secondLastName,
            @JsonProperty("full_name") String fullName,
            @JsonProperty("document_number") String documentNumber) {
    }
}
