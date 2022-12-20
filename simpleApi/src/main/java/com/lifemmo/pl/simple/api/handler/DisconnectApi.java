package com.lifemmo.pl.simple.api.handler;

import com.lifemmo.pl.base.service.api.ApiCallBackHandler;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

@Slf4j
public class DisconnectApi implements ApiCallBackHandler {

    @Override
    public void onDisconnect(long accountId, @NotNull String sessionInfo){
        log.info("onDisconnect {},{}",accountId,sessionInfo);
    }
}
