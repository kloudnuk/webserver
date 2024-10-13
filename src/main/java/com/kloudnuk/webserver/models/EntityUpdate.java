package com.kloudnuk.webserver.models;

public record EntityUpdate( // update ...
        String updateColumn, // the following column ..
        String updateValue, // with the following value ..
        String filterColumn, // where the Entity has a column with the
                             // following name ...
        String filterValue // and such column holds the following value
) {
}
