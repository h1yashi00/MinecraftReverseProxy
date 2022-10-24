# MinecraftReverseProxy
Minecraft用に作成したリバースプロキシ｡
このソフトウェアは私個人で利用することを目的としていたため､実際にこのソフトウェアを利用してリバースプロキシを作成することは推奨できません｡
ただ､同じようなプログラムを思いついた方がいれば私の知識を共有できればと思い公開するに至りました｡
何かしらの質問等があれば
**Discord: はんかけ#0028**
に連絡いただければサポートもしくは支援できます｡

使用したライブラリ
[Netty](https://netty.io/)
主に参考にしたサイト
[Protocol](https://wiki.vg/Protocol)

できること
Minecraftのパケット以外を通さない｡
ドメイン名の入力をクライアント側に強制することが可能 ← Bypassする方法が存在するのでお気持ち程度に
リバースプロキシプロトコルを利用可能
タイムアウトやThrottleを指定可能

作成した経緯
個人のMinecraftのサーバーを建てる上で鯖主からのDDoS報告が跡を絶たなかったためサーバーを保護するためのプログラムを作成してみたかったから｡
現在のリバースプロキシの問題
現在のリバースプロキシ(主に､NginxやHaproxy)では､
[ここ](https://twitter.com/tsukkkkkun/status/1356646980542287874?s=20&t=PNq3Ki45mQ595VS1rHxIdQ)
で指摘されている通り､TCPパケットをそのままBungeeCordに届けるだけの仕様のため､単純なTCPのパケットを大量に投げる攻撃に対して意味をなさない｡
そこでMinecraftのパケットを調べて､マイクラのパケットだった場合にForwardingするようなリバースプロキシを作成すれば解決しそうだなと思い作成した｡

使い方
NginxやHaproxyなどと同じように､configをいじることにより可能｡
jarを起動すると､config.yamlが作成されるので､

スロットル(throttle)は一定の時間までに一定の回数の接続があった場合相手の通信を強制的に遮断するものです(BugeecordのThrottoleと同じ動作)

スロットルが有効になる回数
throttle_limit: 3

スロットルの回数を保有する時間(ms)  推奨: 3000ms
throttle_time: 3000

リバースプロキシプロトコルの利用
use_proxy_protocol: true

MinecraftのホストIP(Bungeecord)
minecraft_host_ip: bungee

Minecraftホストのポート番号
minecraft_host_port: 25577

チェックするドメイン
check_domain: recraft.click

通信が開始してからパケットが送られなかったときのタイムアウト
timeout_sec: 3

リバースプロキシ(MinecraftReverseProxy)がバインドするポート番号
bind_port: 25565
