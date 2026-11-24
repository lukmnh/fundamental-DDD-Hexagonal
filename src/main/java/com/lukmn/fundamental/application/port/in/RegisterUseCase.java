package com.lukmn.fundamental.application.port.in;

import com.lukmn.fundamental.domain.model.User;

public interface RegisterUseCase {

    User register(RegisterCommand command);

    /* Record di dalam interface otomatis jadi public static.
    Command-nya menempel di use case pemiliknya, jadi jumlah file nggak meledak.*/
    record RegisterCommand(String username, String rawPassword) { }
}
