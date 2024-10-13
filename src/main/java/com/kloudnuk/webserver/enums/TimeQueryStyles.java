package com.kloudnuk.webserver.enums;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("TimeQueryStyles")
@Scope("prototype")

public enum TimeQueryStyles {
    options, regex
}
