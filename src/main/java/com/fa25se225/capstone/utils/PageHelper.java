package com.fa25se225.capstone.utils;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PageHelper {

    public Pageable pageEngine(int pageNo, int pageSize, String... sorts){
        if(pageNo > 0){
            pageNo--;
        }
        List<Sort.Order> orders = new ArrayList<>();
        if(Objects.nonNull(sorts)) {
            for (String sort : sorts) {
                    // field:acs|desc
                    Pattern pattern = Pattern.compile("(\\w+?)(:)(.*)");
                    Matcher matcher = pattern.matcher(sort);
                    if (matcher.find()) {
                        if (matcher.group(3).equalsIgnoreCase("asc")) {
                            orders.add(new Sort.Order(Sort.Direction.ASC, matcher.group(1)));
                        } else {
                            orders.add(new Sort.Order(Sort.Direction.DESC, matcher.group(1)));
                        }
                    }
            }
        }
        return PageRequest.of(pageNo, pageSize, Sort.by(orders));
    }
}
