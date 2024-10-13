package com.kloudnuk.webserver.enums;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("TimeRangeOptions")
@Scope("prototype")
public enum TimeRangeOptions {
  day, hour, minute
}
