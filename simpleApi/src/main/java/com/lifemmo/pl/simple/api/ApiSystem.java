package com.lifemmo.pl.simple.api;

import com.lifemmo.pl.base.protocol.Packet;
import com.lifemmo.pl.base.protocol.ReplyPacket;
import com.lifemmo.pl.base.service.BaseSender;
import com.lifemmo.pl.base.service.ServerSystem;
import com.lifemmo.pl.base.service.SystemPanel;
import com.lifemmo.pl.simple.Simple;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jetbrains.annotations.NotNull;

@Slf4j
public class ApiSystem implements ServerSystem {

    private BaseSender baseSender;
    private SystemPanel systemPanel;

    ApiSystem(SystemPanel systemPanel,BaseSender baseSender){
        this.baseSender = baseSender;
        this.systemPanel = systemPanel;
    }



    @Override
    public void onDispatch(@NotNull Packet packet) {
        log.info("{} packet received",packet.getMsgName());

        try{
            if(packet.getMsgName().equals(Simple.HelloReq.getDescriptor().getName())){
                var data = Simple.HelloReq.parseFrom(packet.buffer()).getMessage();
                this.baseSender.reply(new ReplyPacket(Simple.HelloRes.newBuilder().setMessage(data).build()));
            }
        }catch (Exception e){
            log.error(ExceptionUtils.getMessage(e));
        }

    }

    @NotNull
    @Override
    public BaseSender getBaseSender() {
        return this.baseSender;
    }

    @NotNull
    @Override
    public SystemPanel getSystemPanel() {
        return this.systemPanel;
    }


    @Override
    public void onPause() {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void onStart() {

    }

    @Override
    public void onStop() {

    }


}
