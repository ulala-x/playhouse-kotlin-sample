package com.lifemmo.pl.simple.api.handler;

import com.lifemmo.pl.base.protocol.Packet;
import com.lifemmo.pl.base.protocol.ReplyPacket;
import com.lifemmo.pl.base.service.*;
import com.lifemmo.pl.base.service.api.annotation.Api;
import com.lifemmo.pl.base.service.api.annotation.ApiHandler;
import com.lifemmo.pl.base.service.api.annotation.Init;
import com.lifemmo.pl.simple.Simple;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
@Component
@Api
public class SampleApi {

    private SystemPanel systemPanel;
    private ApiBaseSender apiBaseSender;

    @Init
    public void init(SystemPanel systemPanel,ApiBaseSender apiBaseSender){
        this.systemPanel = systemPanel;
        this.apiBaseSender = apiBaseSender;
    }

    @ApiHandler(msgName = "AuthenticateReq")
    public void authenticate(String sessionInfo,Packet packet, ApiSender apiSender) throws Exception{
        Simple.AuthenticateReq req = Simple.AuthenticateReq.parseFrom(packet.buffer());
        Long accountId = req.getUserId();
        log.info("authenticate: {},{}",accountId,req.getToken());

        apiSender.authenticate(accountId,accountId.toString());


        Simple.AuthenticateRes message = Simple.AuthenticateRes.newBuilder().setUserInfo(String.valueOf(accountId)).build();
        apiSender.reply(new ReplyPacket(message));

    }

    @ApiHandler(msgName = "HelloReq")
    public void hello(String sessionInfo,Packet packet, ApiSender apiSender) throws Exception{
        Simple.HelloReq req = Simple.HelloReq.parseFrom(packet.buffer());
        log.info("hello:{},{}",req.getMessage(),apiSender.sessionInfo());
        apiSender.reply(new ReplyPacket(Simple.HelloRes.newBuilder().setMessage("hello").build()));
    }

    @ApiHandler(msgName = "SessionUpdateMsg")
    public void SessionUpdateMsg(String sessionInfo,Packet packet, ApiSender apiSender) throws Exception{
        Simple.SessionUpdateMsg msg = Simple.SessionUpdateMsg.parseFrom(packet.buffer());
        log.info("SessionUpdateMsg:{}",msg.getSessionInfo());
        apiSender.updateSession(apiSender.serviceId(),msg.getSessionInfo());

    }

    @ApiHandler(msgName = "SessionInfoReq")
    public void SessionInfoReq(String sessionInfo,Packet packet, ApiSender apiSender) throws Exception{
        log.info("SessionInfoReq:{}",sessionInfo);
        apiSender.reply(new ReplyPacket(Simple.SessionInfoRes.newBuilder().setSessionInfo(sessionInfo).build()));
    }

    @ApiHandler(msgName = "CloseSessionMsg")
    public void CloseSessionMsg(String sessionInfo,Packet packet, ApiSender apiSender) throws Exception{
        apiSender.sendToClient(new Packet(Simple.CloseSessionMsg.newBuilder().build()));

        apiSender.sessionClose(apiSender.sessionEndpoint(),apiSender.sid());

    }

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @Scheduled(fixedRate = 5000)
    public void reportCurrentTime() {
        log.info("The time is now {}", dateFormat.format(new Date()));
    }


}
