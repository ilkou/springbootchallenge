package io.github.ilkou.springbootchallenge.transverse.dto;

import lombok.Data;

@Data
public class BatchUsersSummaryDto {
    private int totalUsers;
    private int successfulImports;
    private int failedImports;
}
