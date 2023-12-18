package com.tiagotibaes.streams.dto;


import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public record ApproveRequestDTO(String status, String cpf){

}
