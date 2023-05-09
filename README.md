# playhouse-kotlin-sample
PlayHouse 의 간략한 사용 예제 입니다. 
Client connector 를 통해서 API 서버로 메시지를 보내고 , Play Server 에 방 생성및 입장을 하는 예제입니다.

# How to Build
```
git clone https://github.com/ulala-x/playhouse-kotlin-sample.git

서버간 주소 동기화를 위해서 redis 가 필요합니다.

docker run -d --name redis-simple-server -p 6379:6379 redis/redis-stack-server:latest
```

# Examples

## Simple Client
- Client Connector 를 이용해서 Session 서버에 접속하고 패킷을 보낸다.

## Simple Session
공통적으로 모든 서버는 commonOption 과 각 서버별 특성을 가진 Option 두가지를 가지고 옵션 설정이 가능하다.
``` kotlin
 @SpringBootApplication
class SessionApplication : CommandLineRunner {
    private val log = logger()
    override fun run(vararg args: String?) {
        try{
            val redisPort = 6379

            val sessionSvcId:Short = 1
            val apiSvcId:Short = 2
            val playSvcId:Short = 3

            val commonOption = CommonOption().apply {
                this.port = 30370 // bind 할 서버 주소
                this.serviceId = sessionSvcId// Service Id 
                this.redisPort = redisPort// 주소 동기화를 위한 redis port
                this.serverSystem = {systemPanel,sender -> SessionSystem(systemPanel,sender) }
                this.requestTimeoutSec = 0//request message 에 대한 time out 설정
                this.logger = ConsoleLogger()
            }

            val sessionOption = SessionOption().apply {
                this.sessionPort = 30114// 클리언트가 접속할 server port
                this.clientSessionIdleTimeout = 0//client idle timeout
                this.useWebSocket = false// 웹소켓 사용 여부
                this.urls = arrayListOf("$apiSvcId:${AuthenticateReq.getDescriptor().index}")
                // 인증 패킷 할당. 인증을 처리할 service 와 패킷을 지정한다.
            }

            val sessionServer = SessionServer(commonOption,sessionOption)

            sessionServer.start()

            Runtime.getRuntime().addShutdownHook(object:Thread(){
                override fun run() {
                    log.info("*** shutting down Session server since JVM is shutting down")
                    sessionServer.stop()
                    log.info("*** server shut down")
                    sleep(1000)
                }
            })

            log.info("Session Server Started")
            sessionServer.awaitTermination()

        }catch (e:Exception){
            log.error(ExceptionUtils.getStackTrace(e))
            exitProcess(1)
        }

    }
}
```
  모든 서버는 ServerSystem 인터페이스를 구현해야 하는데 이를 통해서 서버의 운영 시스템을 구축할 수 있다. 
``` kotlin
class SessionSystem(override val systemPanel: SystemPanel,override val sender: Sender) : ServerSystem {

    private val log = logger()
    override suspend fun onDispatch(packet: Packet) {
        log.info("${packet.msgId} is received")
    }

    override suspend fun onPause() {
        log.info("session pause")
    }

    override suspend fun onResume() {
        log.info("session resume")
    }

    override suspend fun onStart() {
        log.info("session start")
    }

    override suspend fun onStop() {
        log.info("session stop")
        log.info("session stop")
    }
}
```

## Simple API
API 서버의 구현 기본 ServerSystem 을 구현하고  아래처럼 ApiService 인터페이스를 상속받아서 Packet 을 등록하고 Packet Handler 를 구현해주면 된다.
``` kotlin

@Component
class SampleApi : ApiService {
    private lateinit var systemPanel: SystemPanel
    private lateinit var sender: Sender
    private val log = logger()


    override suspend fun init(systemPanel: SystemPanel, sender: Sender) {
        this.systemPanel = systemPanel
        this.sender = sender
    }

    override fun instance(): ApiService {
        return SpringContext.getContext().getBean(this::class.java)
    }

    // HandlerRegister 는 Client 에서 전달되는 패킷을 처리할때 사용되고 
    // BackendHandlerRegister 는 서버에서 전달되는 패킷을 처리할때 사용된다.
    override fun handles(register: HandlerRegister,backendRegister: BackendHandlerRegister) {
        register.add(AuthenticateReq.getDescriptor().index,::authenticate)
        register.add(HelloReq.getDescriptor().index,::hello)
        register.add(CloseSessionMsg.getDescriptor().index,::closeSessionMsg)
        register.add(SendMsg.getDescriptor().index,::sendMessage)
    }

    suspend fun authenticate(packet: Packet, apiSender: ApiSender) {
        val req: AuthenticateReq = AuthenticateReq.parseFrom(packet.data())
        val accountId: Long = req.userId
        apiSender.authenticate(accountId)
        val message: AuthenticateRes = AuthenticateRes.newBuilder().setUserInfo(accountId.toString()).build()
        apiSender.reply(ReplyPacket(message))
    }

    suspend fun hello(packet: Packet, apiSender: ApiSender) {
        val req: HelloReq = HelloReq.parseFrom(packet.data())
        apiSender.reply(ReplyPacket(HelloRes.newBuilder().setMessage("hello").build()))
    }

    suspend fun sendMessage(packet:Packet,apiSender: ApiSender){
        val recv = SendMsg.parseFrom(packet.data())
        apiSender.sendToClient(Packet(SendMsg.getDescriptor().index,packet.movePayload()))
    }

    suspend fun closeSessionMsg( packet: Packet, apiSender: ApiSender) {
        apiSender.sendToClient(Packet(CloseSessionMsg.newBuilder().build()))
        apiSender.sessionClose(apiSender.sessionEndpoint, apiSender.sid)
    }

    @Scheduled(fixedRate = 5000)
    fun reportCurrentTime() {
        log.info("The time is now ${dateFormat.format(Date())}")
    }

}
```

## Simple Play
Play 서버도 공통적으로 ServerSystem 인터페이스를 구현해주고 추가적으로 Stage 와  Actor 인터페이스를 구현해주면 된다.
제공되는 방생성 및 입장 callback을 구현하고  , OnDispatch 에서 Stage(room) 으로 전달되는 메시치 처리를 해주면 된다.
``` kotlin
class SimpleRoom(override val stageSender: StageSender) : Stage<SimpleUser> {
    private val log = logger()
    private val packetHandler = PacketHandler<SimpleRoom, SimpleUser>()
    private val userMap:MutableMap<Long, SimpleUser> = mutableMapOf()
    private var count = 0

    val countTimer: TimerCallback = {
        log.info("count timer:$count")
        count++
    }
    init {
        packetHandler.add(LeaveRoomReq.getDescriptor().index, LeaveRoomCmd())
        packetHandler.add(ChatMsg.getDescriptor().index, ChatMsgCmd())

        stageSender.addCountTimer(Duration.ofSeconds(3),3, Duration.ofSeconds(1), countTimer)
        stageSender.addRepeatTimer(Duration.ZERO, Duration.ofMillis(200), suspend{
            log.info("repeat timer")
        })
    }


    override suspend fun onCreate(packet: Packet): ReplyPacket {
        val request = CreateRoomAsk.parseFrom(packet.data())
        return ReplyPacket(CreateRoomAnswer.newBuilder().setData(request.data).build())
    }

    override suspend fun onDispatch(user: SimpleUser, packet: Packet) {
        packetHandler.dispatch(this, user ,packet)
    }

    override suspend fun onJoinStage(user: SimpleUser, packet: Packet): ReplyPacket {
        val request = JoinRoomAsk.parseFrom(packet.data())
        return ReplyPacket(JoinRoomAnswer.newBuilder().setData(request.data).build())

    }

    override suspend fun onPostCreate() {
    }

    override suspend fun onPostJoinStage(user: SimpleUser) {
        userMap[user.accountId()] = user
        val deferred = user.actorSender.asyncToApi(
            Packet(HelloToApiReq.newBuilder().setData("hello").build())
        ).await()
    }

    override suspend fun onDisconnect(user: SimpleUser) {
        leaveRoom(user)
    }

    fun leaveRoom(user: SimpleUser) {
        userMap.remove(user.accountId())
        if(userMap.isEmpty()){
            log.info("add count timer :${Thread.currentThread().id}")
            stageSender.addCountTimer(Duration.ofSeconds(5),1,Duration.ofSeconds(5), suspend {
                if(userMap.isEmpty()){
                    log.info("close room :${Thread.currentThread().id}")
                    stageSender.closeStage() //.closeStage()
                }
            })
        }
    }

    fun sendAll(packet: Packet) {
        userMap.values.forEach {
            it.actorSender.sendToClient(packet)
        }
    }

}
```
Actor(User)
룸에서 생성되는 User객체이다. Actor 인터페이스를 상속받아서 구현하면 된다.
``` kotlin
 class SimpleUser(override val actorSender: ActorSender) : Actor {
    private var log = logger()
    fun accountId():Long{
        return actorSender.accountId()
    }
    override fun onCreate() {
        log.info("onCreate:${actorSender.accountId()}")
    }

    override fun onDestroy() {
        log.info("onDestroy:${actorSender.accountId()}")
    }
}
```
``` kotlin
@EnableScheduling
@SpringBootApplication
class PlayApplication : CommandLineRunner {
    private val log = logger()
    override fun run(vararg args: String?) {
        try{
            val redisPort = 6379

            val commonOption = CommonOption().apply {
                this.port = 30570
                this.serviceId = 3
                this.redisPort = redisPort
                this.serverSystem = {systemPanel,sender -> PlaySystem(systemPanel,sender) }
                this.logger = ConsoleLogger()
                this.requestTimeoutSec = 0
            }

            //playOption 에 위에서 정의한 SimpleRoom과 SimpleUser 를 생성자 함수로 등록해준다.
            //생성자 함수 Type 을 인자로 지정할수 있다. 룸 생성시 지정된 type으로 room 이 생성된다. 
            //즉 여러 type 의 Room 과 User 를 등록해서 사용 할수 있다.
            val playOption = PlayOption().apply {
                this.elementConfigurator.register("simple",
                    {stageSender -> SimpleRoom(stageSender) },{ userSender -> SimpleUser(userSender) })
            }

            val playServer = PlayServer(commonOption,playOption)

            playServer.start()

            Runtime.getRuntime().addShutdownHook(object:Thread(){
                override fun run() {
                    log.info("*** shutting down Room server since JVM is shutting down")
                    playServer.stop()
                    log.info("*** server shutdown")
                    sleep(1000)
                }
            })

            log.info("Room Server Started")
            playServer.awaitTermination()

        }catch (e:Exception){
            log.error(ExceptionUtils.getStackTrace(e))
            exitProcess(1)
        }

    }

}
```
