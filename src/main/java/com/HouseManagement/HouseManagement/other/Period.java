package com.HouseManagement.HouseManagement.other;

import java.time.LocalDate;

public class Period {
    public static String getPeriod(){
            var date = LocalDate.now();
            var monthNames = new String[]{"Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
                    "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"};
            var period = "";
            if(date.getMonthValue() - 1 == 0) period = (monthNames[11] + " " + (date.getYear() - 1));
            else period = (monthNames[date.getMonthValue() - 2] + " " + (date.getYear()));
            return period;
    }
}
