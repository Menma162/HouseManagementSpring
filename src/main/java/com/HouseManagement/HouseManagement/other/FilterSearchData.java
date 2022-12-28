package com.HouseManagement.HouseManagement.other;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FilterSearchData {
    private String search;
    private String searchPeriod;
    private String type;
    private String status;
}
