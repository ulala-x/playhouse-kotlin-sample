package com.lifemmo.pl.simple.api.handler;

import com.lifemmo.pl.base.communicator.ServerInfo;
import com.lifemmo.pl.base.protocol.Packet;
import com.lifemmo.pl.base.protocol.ReplyPacket;
import com.lifemmo.pl.base.service.*;
import com.lifemmo.pl.base.service.api.annotation.Api;
import com.lifemmo.pl.base.service.api.annotation.ApiBackendHandler;
import com.lifemmo.pl.base.service.api.annotation.ApiHandler;
import com.lifemmo.pl.base.service.api.annotation.Init;
import com.lifemmo.pl.simple.Simple;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Api
public class SampleApiForRoom {
    private SystemPanel systemPanel;
    private ApiBaseSender apiBaseSender;

    private String roomSvcId = "room";
    private String roomType = "simple";
    private int success = 0;
    private int fail = 1;

    @Init
    public void init(SystemPanel systemPanel,ApiBaseSender apiBaseSender){
        this.systemPanel = systemPanel;
        this.apiBaseSender = apiBaseSender;
    }

    @ApiHandler(msgName = "CreateRoomReq")
    public void createRoom(String sessionInfo, Packet packet, ApiSender apiSender) throws Exception{
        log.info("CreateRoom : sessionInfo:{},msgName:{}",sessionInfo,packet.getMsgName());

        String data = Simple.CreateRoomReq.parseFrom(packet.buffer()).getData();

        ServerInfo randRoomServerInfo  = systemPanel.randomServerInfo("room");
        String roomEndpoint = randRoomServerInfo.getBindEndpoint();

        var result = apiSender.createRoom(
                roomEndpoint,
                roomType,
                new Packet(Simple.CreateRoomAsk.newBuilder().setData(data).build()));

        var createRoomAnswer = Simple.CreateRoomAnswer.parseFrom(result.getCreateRoomRes().buffer());
        var roomId = result.getRoomId();

        if(result.isSuccess()){
            apiSender.reply(new ReplyPacket(
                    Simple.CreateRoomRes.newBuilder()
                            .setData(createRoomAnswer.getData())
                            .setRoomId(roomId)
                            .setRoomEndpoint(roomEndpoint)
                            .build())
            );
        }else{
            apiSender.reply(new ReplyPacket(result.getErrorCode()));
        }

    }

    @ApiHandler(msgName = "JoinRoomReq")
    public void joinRoom(String sessionInfo, Packet packet, ApiSender apiSender) throws Exception{

        log.info("joinRoom : sessionInfo:{},msgName:{}",sessionInfo,packet.getMsgName());
        var request = Simple.JoinRoomReq.parseFrom(packet.buffer());
        var data = request.getData();
        var roomId = request.getRoomId();
        var roomEndpoint = request.getRoomEndpoint();
        var accountId = Long.parseLong(sessionInfo);
        var sessionEndpoint = apiSender.sessionEndpoint();
        var sid = apiSender.sid();

        var result = apiSender.joinRoom(roomEndpoint,
                roomId,
                accountId,
                sessionEndpoint,
                sid,
                new Packet(Simple.JoinRoomAsk.newBuilder().setData(data).build())
        );

        if(result.isSuccess()){
            var joinRoomAnswer = Simple.JoinRoomAnswer.parseFrom(result.getJoinRoomRes().buffer());
            apiSender.reply(
                    new ReplyPacket(Simple.JoinRoomRes.newBuilder().setData(joinRoomAnswer.getData()).build())
            );
        }else{
            apiSender.reply(new ReplyPacket(result.getErrorCode()));
        }


    }

    @ApiHandler(msgName = "CreateJoinRoomReq")
    public void createJoinRoom(String sessionInfo, Packet packet, ApiSender apiSender) throws Exception{
        log.info("createJoinRoom : sessionInfo:{},msgName:{}",sessionInfo,packet.getMsgName());
        var request = Simple.CreateJoinRoomReq.parseFrom(packet.buffer());
        var data = request.getData();
        var roomId = request.getRoomId();
        var roomEndpoint = request.getRoomEndpoint();
        var accountId = Long.parseLong(sessionInfo);
        var sessionEndpoint = apiSender.sessionEndpoint();
        var sid = apiSender.sid();
        var createPayload = new Packet(Simple.CreateRoomAsk.newBuilder().setData(data).build());
        var joinPayload = new Packet(Simple.JoinRoomAsk.newBuilder().setData(data).build());

        var result = apiSender.createJoinRoom(
                roomEndpoint,roomType,roomId,createPayload,
                accountId,sessionEndpoint,sid, joinPayload);

        if(result.isSuccess()){
            var joinRoomAnswer = Simple.CreateJoinRoomAnswer.parseFrom(result.getJoinRoomRes().buffer());
            apiSender.reply(
                    new ReplyPacket(Simple.CreateJoinRoomRes.newBuilder().setData(joinRoomAnswer.getData()).build())
            );
        }else{
            apiSender.reply(new ReplyPacket(result.getErrorCode()));
        }
    }

    @ApiBackendHandler(msgName = "LeaveRoomNotify")
    public void leaveRoomNotify(String sessionInfo, Packet packet, ApiBackendSender apiBackendSender) throws Exception{
        log.info("leaveRoomNotify : sessionInfo:{},msgName:{}",sessionInfo,packet.getMsgName());
        BaseSender baseSender = ControlContext.INSTANCE.getBaseSender();
        var notify = Simple.LeaveRoomNotify.parseFrom(packet.buffer());
        baseSender.sendToClient(notify.getSessionEndpoint(),notify.getSid(),new Packet(notify));
    }

    @ApiBackendHandler(msgName="HelloToApiReq")
    public void helloToApiReq(String sessionInfo, Packet packet, ApiBackendSender apiBackendSender) throws Exception{
        log.info("helloToApiReq : sessionInfo:{},msgName:{}",sessionInfo,packet.getMsgName());
        String data = Simple.HelloToApiReq.parseFrom(packet.buffer()).getData();
        apiBackendSender.reply(new ReplyPacket(Simple.HelloToApiRes.newBuilder().setData(data).build()));

    }
}
