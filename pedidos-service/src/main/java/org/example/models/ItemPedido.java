package org.example.models;

import jakarta.persistence.*;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Embeddable
public class ItemPedido {
    private Long idProduto;
    private Integer quantidade;
}