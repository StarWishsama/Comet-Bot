package io.github.starwishsama.namelessbot.objects.rainbowsix;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class R6Player {
    /**
     * status : 200
     * found : true
     * player : {"p_id":"4bf2232d-7e8e-4454-a781-b6e48d1b1d56","p_user":"4bf2232d-7e8e-4454-a781-b6e48d1b1d56","p_name":"MacieJay","p_platform":"uplay"}
     * custom : {"customurl":"MacieJay","verified":true,"visitors":2051,"banned":false}
     * refresh : {"queued":false,"possible":true,"qtime":0,"utime":1586323390,"status":2147483647}
     * aliases : {"1":{"name":"MacieJay","utime":1551125146,"date":"25 Feb, 2019"},"2":{"name":"MacieJay","utime":1551125132,"date":"25 Feb, 2019"},"3":{"name":"McDizzle444","utime":1541631282,"date":"07 Nov, 2018"},"4":{"name":"MC.Jay444","utime":1538684326,"date":"04 Oct, 2018"},"5":{"name":"MC.Jay444","utime":1535704124,"date":"31 Aug, 2018"}}
     * stats : {"level":566,"casualpvp_kills":20189,"casualpvp_death":12555,"casualpvp_matchwon":3365,"casualpvp_matchlost":1755,"casualpvp_timeplayed":4073861,"casualpvp_hoursplayed":1132,"casualpvp_matches":5120,"casualpvp_kd":"1.61","casualpvp_wl":"65.72%","rankedpvp_kills":71208,"rankedpvp_death":49158,"rankedpvp_matchwon":8300,"rankedpvp_matchlost":4673,"rankedpvp_timeplayed":15512872,"rankedpvp_hoursplayed":4309,"rankedpvp_matches":12973,"rankedpvp_kd":"1.45","rankedpvp_wl":"63.98%","generalpvp_headshot":38541,"generalpvp_kills":98130,"generalpvp_timeplayed":20818482,"generalpve_kills":31699,"generalpve_death":1410,"generalpve_matchwon":679,"generalpve_matchlost":2243,"generalpve_headshot":22093,"generalpve_timeplayed":329503,"generalpvp_hoursplayed":5783,"generalpvp_death":61713,"generalpvp_kd":"1.59","generalpvp_matchwon":11665,"generalpvp_matchlost":6428,"generalpvp_matches":18093,"generalpvp_wl":"64.47%","generalpvp_hsrate":"39.28%","generalpvp_killassists":23911,"generalpvp_meleekills":2250,"generalpvp_revive":2307,"generalpvp_penetrationkills":7987,"generalpve_hoursplayed":92,"generalpve_matches":2922,"generalpve_kd":"22.48","generalpve_wl":"23.24%","generalpve_hsrate":"69.70%","plantbombpvp_matchwon":9227,"plantbombpvp_matchlost":5356,"secureareapvp_matchwon":2189,"secureareapvp_matchlost":999,"rescuehostagepvp_matchwon":1024,"rescuehostagepvp_matchlost":416,"plantbombpvp_matches":14583,"plantbombpvp_wl":"63.27%","secureareapvp_matches":3188,"secureareapvp_wl":"68.66%","rescuehostagepvp_matches":1440,"rescuehostagepvp_wl":"71.11%","tabmmr":8944,"tabrank":8,"tabrankname":"Major"}
     * ranked : {"AS_kills":0,"AS_deaths":0,"AS_wins":0,"AS_losses":0,"AS_abandons":0,"AS_mmr":0,"AS_maxmmr":0,"AS_champ":0,"AS_mmrchange":0,"AS_actualmmr":2500,"AS_matches":0,"AS_wl":"0%","AS_kd":"0","AS_rank":0,"AS_rankname":"Unranked","AS_maxrank":0,"AS_maxrankname":"Unranked","AS_killpermatch":0,"AS_deathspermatch":0,"allkills":819,"alldeaths":649,"allwins":108,"alllosses":63,"allabandons":1,"EU_kills":5,"EU_deaths":3,"EU_wins":0,"EU_losses":1,"EU_abandons":0,"EU_mmr":0,"EU_maxmmr":0,"EU_champ":0,"EU_mmrchange":-76,"EU_actualmmr":3343,"EU_matches":1,"EU_wl":"0%","EU_kd":"1.67","EU_rank":0,"EU_rankname":"Unranked","EU_maxrank":0,"EU_maxrankname":"Unranked","EU_killpermatch":5,"EU_deathspermatch":3,"NA_kills":814,"NA_deaths":646,"NA_wins":108,"NA_losses":62,"NA_abandons":1,"NA_mmr":4542,"NA_maxmmr":4570,"NA_champ":0,"NA_mmrchange":-28,"NA_actualmmr":4542,"NA_matches":171,"NA_wl":"63%","NA_kd":"1.26","NA_rank":20,"NA_rankname":"Diamond","NA_maxrank":20,"NA_maxrankname":"Diamond","NA_killpermatch":5,"NA_deathspermatch":4,"mmr":4542,"maxmmr":4570,"kd":1.45,"rank":20,"rankname":"Diamond","maxrank":20,"maxrankname":"Diamond","champ":0,"topregion":"America","actualmmr":4542,"allmatches":172,"allkd":"1.26","allwl":"63%","killpermatch":5,"deathspermatch":4}
     * social : {"status":403,"utime":1586330684,"uplay_user":null,"uplay_name":null,"twitter":null,"instagram":null,"twitch":"","twitch_id":null,"youtube":null,"mixer":null,"discord":null,"discord_id":null,"discord_user":null,"esl":null,"bio":"","background":"","embed":"","aliases_hide":0,"twitch_display":0,"premium":{"tabwire":false,"discord":false,"twitch":false},"is_premium":true}
     * history : {"08452f285cf2f20c860967565d1a6082":{"started":1586322001,"ended":1586408400,"date":"08 April","casual_kills":0,"casual_deaths":0,"casual_wins":0,"casual_losses":0,"casual_matches":0,"ranked_kills":4,"ranked_deaths":13,"ranked_wins":2,"ranked_losses":1,"ranked_matches":3,"total_wins":2,"total_losses":1,"total_kills":4,"total_headshots":1,"NA_mmrchange":0,"NA_mmr":4542,"EU_mmrchange":0,"EU_mmr":3343,"AS_mmrchange":0,"AS_mmr":2500,"casual_kd":"0","casual_wl":"0%","ranked_kd":"0.31","ranked_wl":"66.67%","headshot_accuracy":"25.00%","color":"green"},"6c3a86843723e784a633617d67b4afa9":{"started":1586235601,"ended":1586322000,"date":"07 April","casual_kills":0,"casual_deaths":0,"casual_wins":0,"casual_losses":0,"casual_matches":0,"ranked_kills":15,"ranked_deaths":8,"ranked_wins":2,"ranked_losses":0,"ranked_matches":2,"total_wins":2,"total_losses":0,"total_kills":20,"total_headshots":8,"NA_mmrchange":0,"NA_mmr":4523,"EU_mmrchange":0,"EU_mmr":3343,"AS_mmrchange":0,"AS_mmr":2500,"casual_kd":"0","casual_wl":"0%","ranked_kd":"1.88","ranked_wl":"100.00%","headshot_accuracy":"40.00%","color":"green"},"8b691dd930a67c18e748c72e5ab363b7":{"started":1585890001,"ended":1585976400,"date":"03 April","casual_kills":7,"casual_deaths":4,"casual_wins":1,"casual_losses":0,"casual_matches":1,"ranked_kills":0,"ranked_deaths":0,"ranked_wins":0,"ranked_losses":0,"ranked_matches":0,"total_wins":1,"total_losses":0,"total_kills":29,"total_headshots":12,"NA_mmrchange":0,"NA_mmr":4435,"EU_mmrchange":0,"EU_mmr":3343,"AS_mmrchange":0,"AS_mmr":2500,"casual_kd":"1.75","casual_wl":"100.00%","ranked_kd":"0","ranked_wl":"0%","headshot_accuracy":"41.38%","color":"green"},"d1449c1af929f63610125843cb8caec7":{"started":1585803601,"ended":1585890000,"date":"02 April","casual_kills":1,"casual_deaths":0,"casual_wins":0,"casual_losses":0,"casual_matches":0,"ranked_kills":30,"ranked_deaths":29,"ranked_wins":5,"ranked_losses":3,"ranked_matches":8,"total_wins":5,"total_losses":3,"total_kills":31,"total_headshots":10,"NA_mmrchange":-47,"NA_mmr":4435,"EU_mmrchange":0,"EU_mmr":3343,"AS_mmrchange":0,"AS_mmr":2500,"casual_kd":"0","casual_wl":"0%","ranked_kd":"1.03","ranked_wl":"62.50%","headshot_accuracy":"32.26%","color":"green"},"9ada8290feaee56674f70b244427bbb0":{"started":1585717201,"ended":1585803600,"date":"01 April","casual_kills":0,"casual_deaths":0,"casual_wins":0,"casual_losses":0,"casual_matches":0,"ranked_kills":4,"ranked_deaths":5,"ranked_wins":2,"ranked_losses":0,"ranked_matches":2,"total_wins":2,"total_losses":0,"total_kills":4,"total_headshots":0,"NA_mmrchange":0,"NA_mmr":4406,"EU_mmrchange":0,"EU_mmr":3343,"AS_mmrchange":0,"AS_mmr":2500,"casual_kd":"0","casual_wl":"0%","ranked_kd":"0.80","ranked_wl":"100.00%","headshot_accuracy":"0%","color":"green"},"19d7e9d17046cc3368831d8f0816db9b":{"started":1585630801,"ended":1585717200,"date":"31 March","casual_kills":0,"casual_deaths":0,"casual_wins":0,"casual_losses":0,"casual_matches":0,"ranked_kills":4,"ranked_deaths":4,"ranked_wins":1,"ranked_losses":0,"ranked_matches":1,"total_wins":1,"total_losses":0,"total_kills":4,"total_headshots":2,"NA_mmrchange":0,"NA_mmr":4360,"EU_mmrchange":0,"EU_mmr":3343,"AS_mmrchange":0,"AS_mmr":2500,"casual_kd":"0","casual_wl":"0%","ranked_kd":"1.00","ranked_wl":"100.00%","headshot_accuracy":"50.00%","color":"green"},"28b81260ee72327c416714b149be7244":{"started":1585544401,"ended":1585630800,"date":"30 March","casual_kills":5,"casual_deaths":4,"casual_wins":1,"casual_losses":0,"casual_matches":1,"ranked_kills":8,"ranked_deaths":5,"ranked_wins":1,"ranked_losses":0,"ranked_matches":1,"total_wins":2,"total_losses":0,"total_kills":30,"total_headshots":18,"NA_mmrchange":19,"NA_mmr":4335,"EU_mmrchange":0,"EU_mmr":3343,"AS_mmrchange":0,"AS_mmr":2500,"casual_kd":"1.25","casual_wl":"100.00%","ranked_kd":"1.60","ranked_wl":"100.00%","headshot_accuracy":"60.00%","color":"green"},"bf342c59071dcf8843086dc7059d9403":{"started":1585458001,"ended":1585544400,"date":"29 March","casual_kills":3,"casual_deaths":3,"casual_wins":1,"casual_losses":0,"casual_matches":1,"ranked_kills":20,"ranked_deaths":14,"ranked_wins":4,"ranked_losses":0,"ranked_matches":4,"total_wins":5,"total_losses":0,"total_kills":57,"total_headshots":15,"NA_mmrchange":36,"NA_mmr":4316,"EU_mmrchange":0,"EU_mmr":3343,"AS_mmrchange":0,"AS_mmr":2500,"casual_kd":"1.00","casual_wl":"100.00%","ranked_kd":"1.43","ranked_wl":"100.00%","headshot_accuracy":"26.32%","color":"green"},"c937e82d17031c05f8fe957e4d0bfac9":{"started":1585371601,"ended":1585458000,"date":"28 March","casual_kills":0,"casual_deaths":0,"casual_wins":0,"casual_losses":0,"casual_matches":0,"ranked_kills":53,"ranked_deaths":54,"ranked_wins":8,"ranked_losses":5,"ranked_matches":13,"total_wins":8,"total_losses":5,"total_kills":53,"total_headshots":20,"NA_mmrchange":-49,"NA_mmr":4210,"EU_mmrchange":0,"EU_mmr":3343,"AS_mmrchange":0,"AS_mmr":2500,"casual_kd":"0","casual_wl":"0%","ranked_kd":"0.98","ranked_wl":"61.54%","headshot_accuracy":"37.74%","color":"green"}}
     * seasons : {"6":{"NA_mmr":5575,"NA_champ":19,"NA_wins":634,"NA_losses":294,"NA_abandons":5,"NA_kills":0,"NA_deaths":0,"EU_mmr":4781,"EU_champ":182,"EU_wins":174,"EU_losses":103,"EU_abandons":0,"EU_kills":0,"EU_deaths":0,"AS_mmr":0,"AS_champ":0,"AS_wins":0,"AS_losses":0,"AS_abandons":0,"AS_kills":0,"AS_deaths":0,"seasonname":"Health","seasonclass":"health","champ":"","maxmmr":5575,"maxrank":20,"maxrankname":"Diamond"},"7":{"NA_mmr":5375,"NA_champ":79,"NA_wins":514,"NA_losses":284,"NA_abandons":2,"NA_kills":0,"NA_deaths":0,"EU_mmr":4938,"EU_champ":179,"EU_wins":95,"EU_losses":43,"EU_abandons":1,"EU_kills":0,"EU_deaths":0,"AS_mmr":0,"AS_champ":0,"AS_wins":0,"AS_losses":0,"AS_abandons":0,"AS_kills":0,"AS_deaths":0,"seasonname":"Blood Orchid","seasonclass":"bloodorchid","champ":"","maxmmr":5375,"maxrank":20,"maxrankname":"Diamond"},"8":{"NA_mmr":5280,"NA_champ":116,"NA_wins":420,"NA_losses":247,"NA_abandons":2,"NA_kills":0,"NA_deaths":0,"EU_mmr":5133,"EU_champ":162,"EU_wins":98,"EU_losses":46,"EU_abandons":1,"EU_kills":0,"EU_deaths":0,"AS_mmr":0,"AS_champ":0,"AS_wins":0,"AS_losses":0,"AS_abandons":0,"AS_kills":0,"AS_deaths":0,"seasonname":"White Noise","seasonclass":"whitenoise","champ":"","maxmmr":5280,"maxrank":20,"maxrankname":"Diamond"},"9":{"NA_mmr":5106,"NA_champ":1,"NA_wins":388,"NA_losses":228,"NA_abandons":2,"NA_kills":0,"NA_deaths":0,"EU_mmr":4095,"EU_champ":0,"EU_wins":51,"EU_losses":33,"EU_abandons":0,"EU_kills":0,"EU_deaths":0,"AS_mmr":0,"AS_champ":0,"AS_wins":0,"AS_losses":0,"AS_abandons":0,"AS_kills":0,"AS_deaths":0,"seasonname":"Chimera","seasonclass":"chimera","champ":"","maxmmr":5106,"maxrank":20,"maxrankname":"Diamond"},"10":{"NA_mmr":5110,"NA_champ":1,"NA_wins":517,"NA_losses":306,"NA_abandons":2,"NA_kills":0,"NA_deaths":0,"EU_mmr":4552,"EU_champ":1,"EU_wins":161,"EU_losses":104,"EU_abandons":6,"EU_kills":0,"EU_deaths":0,"AS_mmr":0,"AS_champ":0,"AS_wins":0,"AS_losses":0,"AS_abandons":0,"AS_kills":0,"AS_deaths":0,"seasonname":"Para Bellum","seasonclass":"parabellum","champ":"","maxmmr":5110,"maxrank":20,"maxrankname":"Diamond"},"11":{"NA_mmr":4991,"NA_champ":1,"NA_wins":287,"NA_losses":133,"NA_abandons":1,"NA_kills":0,"NA_deaths":0,"EU_mmr":4612,"EU_champ":0,"EU_wins":197,"EU_losses":147,"EU_abandons":3,"EU_kills":0,"EU_deaths":0,"AS_mmr":0,"AS_champ":0,"AS_wins":0,"AS_losses":0,"AS_abandons":0,"AS_kills":0,"AS_deaths":0,"seasonname":"Grim Sky","seasonclass":"grimsky","champ":"","maxmmr":4991,"maxrank":20,"maxrankname":"Diamond"},"12":{"NA_mmr":5037,"NA_champ":1,"NA_wins":383,"NA_losses":227,"NA_abandons":3,"NA_kills":0,"NA_deaths":0,"EU_mmr":4872,"EU_champ":1,"EU_wins":103,"EU_losses":61,"EU_abandons":2,"EU_kills":0,"EU_deaths":0,"AS_mmr":0,"AS_champ":0,"AS_wins":0,"AS_losses":0,"AS_abandons":0,"AS_kills":0,"AS_deaths":0,"seasonname":"Wind Bastion","seasonclass":"windbastion","champ":"","maxmmr":5037,"maxrank":20,"maxrankname":"Diamond"},"13":{"NA_mmr":4699,"NA_champ":0,"NA_wins":417,"NA_losses":299,"NA_abandons":7,"NA_kills":0,"NA_deaths":0,"EU_mmr":4035,"EU_champ":0,"EU_wins":68,"EU_losses":45,"EU_abandons":1,"EU_kills":0,"EU_deaths":0,"AS_mmr":0,"AS_champ":0,"AS_wins":0,"AS_losses":0,"AS_abandons":0,"AS_kills":0,"AS_deaths":0,"seasonname":"Burnt Horizon","seasonclass":"burnthorizon","champ":"","maxmmr":4699,"maxrank":20,"maxrankname":"Diamond"},"14":{"NA_mmr":4747,"NA_champ":0,"NA_wins":192,"NA_losses":101,"NA_abandons":3,"NA_kills":1336,"NA_deaths":1084,"EU_mmr":3963,"EU_champ":0,"EU_wins":42,"EU_losses":30,"EU_abandons":2,"EU_kills":346,"EU_deaths":266,"AS_mmr":0,"AS_champ":0,"AS_wins":0,"AS_losses":0,"AS_abandons":0,"AS_kills":0,"AS_deaths":0,"seasonname":"Phantom Sight","seasonclass":"phantomsight","champ":"","maxmmr":4747,"maxrank":20,"maxrankname":"Diamond"},"15":{"NA_mmr":4450,"NA_champ":0,"NA_wins":240,"NA_losses":131,"NA_abandons":0,"NA_kills":2939,"NA_deaths":2558,"EU_mmr":4317,"EU_champ":0,"EU_wins":71,"EU_losses":68,"EU_abandons":1,"EU_kills":1053,"EU_deaths":879,"AS_mmr":0,"AS_champ":0,"AS_wins":0,"AS_losses":0,"AS_abandons":0,"AS_kills":0,"AS_deaths":0,"seasonname":"Ember Rise","seasonclass":"emberrise","champ":"","maxmmr":4450,"maxrank":20,"maxrankname":"Diamond"},"16":{"NA_mmr":4389,"NA_champ":0,"NA_wins":136,"NA_losses":102,"NA_abandons":0,"NA_kills":1139,"NA_deaths":993,"EU_mmr":4409,"EU_champ":0,"EU_wins":148,"EU_losses":104,"EU_abandons":0,"EU_kills":1415,"EU_deaths":994,"AS_mmr":0,"AS_champ":0,"AS_wins":0,"AS_losses":0,"AS_abandons":0,"AS_kills":0,"AS_deaths":0,"seasonname":"Shifting Tides","seasonclass":"shiftingtides","champ":"","maxmmr":4409,"maxrank":20,"maxrankname":"Diamond"}}
     * operators_old : {"wins":{"6:16":2,"1:3":99,"2:15":380,"2:F":823,"2:D":515,"2:6":2132,"4:16":3,"7:16":4,"3:E":492,"4:1":1637,"5:3":1883,"2:7":914,"4:2":2186,"5:1A":6,"2:17":474,"5:5":1392,"4:1A":7,"3:17":261,"3:B":577,"2:18":169,"4:5":1552,"3:12":299,"3:7":1515,"2:A":1287,"4:3":1496,"6:1A":17,"1:2":215,"1:4":44,"2:16":93,"3:9":1940,"2:19":72,"3:6":1645,"2:10":561,"3:1":2045,"2:C":945,"2:8":1682,"2:1":1900,"3:10":297,"3:5":1473,"2:12":668,"5:2":1588,"3:11":612,"5:1":1131,"2:2":1118,"2:1A":1,"1:1A":11,"3:C":519,"5:16":1,"2:3":1924,"3:3":1173,"8:16":10,"3:1A":1,"2:B":1003,"2:9":983,"2:5":1513,"4:4":1662,"3:2":1659,"2:4":1881,"3:8":1950,"2:13":214,"2:11":805,"5:4":1020,"3:4":1792,"3:A":948,"3:F":878,"1:1":329,"2:14":332,"3:16":30,"3:D":871,"1:16":10,"4:E":388,"1:5":204},"losses":{"3:F":535,"2:B":907,"2:14":186,"2:13":191,"1:1":268,"7:16":3,"4:4":1028,"3:E":383,"2:8":1432,"5:2":1319,"6:16":12,"2:A":1154,"1:16":4,"1:4":37,"4:2":1484,"4:1A":5,"2:10":563,"4:1":1380,"2:1":1223,"2:18":103,"3:D":568,"4:E":286,"3:A":651,"3:17":181,"3:11":405,"2:F":601,"6:1A":12,"5:16":5,"2:2":733,"2:1A":3,"2:9":885,"2:19":61,"3:3":714,"4:16":15,"2:5":1135,"4:3":1186,"3:10":200,"3:9":1165,"5:1A":9,"3:7":997,"3:12":242,"3:B":382,"4:5":911,"5:5":958,"2:7":754,"3:8":1214,"2:17":475,"1:5":201,"2:D":427,"1:1A":3,"3:6":949,"2:4":1491,"8:16":9,"3:C":507,"2:15":236,"1:2":245,"2:C":607,"2:11":693,"1:3":82,"5:4":607,"3:16":11,"2:12":446,"3:5":1226,"3:2":1277,"3:1A":3,"3:1":1252,"2:6":1798,"5:3":1560,"5:1":888,"2:16":91,"3:4":1378,"2:3":1199},"kills":{"4:16":4,"6:16":4,"5:2":2611,"5:1A":12,"1:4":82,"2:11":1323,"3:F":1251,"3:8":3126,"4:E":644,"4:2":3961,"2:12":1012,"3:16":58,"1:5":347,"2:5":2291,"2:16":152,"3:9":2565,"4:1":2932,"3:7":2628,"2:17":863,"2:9":1750,"2:B":1639,"4:5":2704,"5:3":2543,"3:10":238,"1:1A":20,"3:6":2659,"3:C":873,"3:11":864,"2:A":2430,"3:1":3193,"3:17":395,"2:19":128,"5:5":2109,"3:A":1441,"1:16":12,"3:2":3185,"1:2":350,"3:4":2668,"1:1":497,"3:B":842,"5:4":1382,"2:10":927,"4:3":2571,"2:13":327,"3:E":783,"1:3":159,"2:C":1496,"2:D":870,"2:8":2813,"2:4":3348,"4:4":2750,"2:14":499,"2:7":2033,"3:D":1423,"2:18":269,"4:1A":13,"5:1":1730,"2:2":1656,"3:12":482,"2:15":540,"8:16":13,"6:1A":28,"2:3":3143,"3:3":1921,"2:F":1153,"2:1A":1,"2:1":3160,"3:5":2525,"2:6":3709},"deaths":{"3:A":911,"4:1":2076,"2:13":273,"2:D":642,"1:1A":9,"2:7":1029,"3:3":1173,"2:B":1345,"3:12":340,"3:5":1720,"2:16":130,"4:4":1580,"3:C":709,"2:6":2589,"3:6":1536,"2:18":167,"1:5":301,"5:1":1278,"2:10":778,"2:14":343,"2:5":1790,"3:B":609,"3:7":1492,"3:4":1971,"4:3":1664,"2:C":1022,"3:11":654,"4:5":1576,"2:1A":3,"2:19":88,"2:9":1223,"2:8":2099,"3:17":271,"1:3":133,"2:3":1862,"2:17":614,"3:8":1994,"3:9":1710,"4:16":17,"6:1A":20,"3:2":1919,"2:2":1134,"3:F":832,"3:1A":4,"2:15":380,"7:16":4,"5:1A":11,"3:16":7,"2:12":674,"3:D":979,"2:11":959,"5:5":1462,"1:1":434,"2:4":2073,"3:1":1978,"1:16":3,"2:A":1668,"1:2":339,"4:2":2183,"3:E":608,"1:4":54,"4:1A":8,"5:2":1819,"2:1":1866,"6:16":14,"2:F":827,"4:E":455,"5:4":978,"8:16":19,"5:16":6,"5:3":2234,"3:10":310},"timeplayed":{"3:E":160825,"1:3":34343,"3:2":566741,"1:1":112141,"3:1":673923,"3:16":4684,"2:12":216061,"2:19":25149,"2:18":51900,"4:1A":2220,"4:2":771652,"2:15":121627,"5:3":669107,"4:E":121782,"3:5":544249,"4:5":499106,"2:7":336910,"5:1A":2835,"2:14":99932,"2:A":467331,"2:4":683942,"4:3":522005,"5:2":587281,"4:4":553870,"4:16":2022,"3:B":189308,"3:4":630172,"1:5":75017,"1:1A":2793,"2:5":504590,"3:A":318762,"5:4":334340,"2:13":75187,"5:16":713,"7:16":934,"6:1A":5168,"2:F":271866,"2:B":358203,"3:10":95897,"2:16":31061,"1:16":1562,"2:C":307999,"2:1":645938,"3:3":370184,"2:2":384192,"2:11":282342,"2:9":366064,"3:1A":738,"2:10":207441,"5:1":403527,"3:7":517611,"3:6":519658,"2:1A":696,"4:1":590570,"3:D":279021,"3:12":104462,"3:C":196021,"2:3":641601,"2:17":185276,"8:16":2451,"2:6":787975,"1:4":14989,"6:16":1557,"3:11":191638,"3:9":628672,"1:2":84584,"3:8":644298,"3:17":86837,"2:8":604642,"3:F":273329,"5:5":485406,"2:D":179394}}
     */

    private int status;
    private boolean found;
    private PlayerBean player;
    private CustomBean custom;
    private RefreshBean refresh;
    private StatsBean stats;
    private RankedBean ranked;

    @Data
    public static class PlayerBean {
        /**
         * p_id : 4bf2232d-7e8e-4454-a781-b6e48d1b1d56
         * p_user : 4bf2232d-7e8e-4454-a781-b6e48d1b1d56
         * p_name : MacieJay
         * p_platform : uplay
         */

        private String p_id;
        private String p_user;
        private String p_name;
        private String p_platform;
    }

    @Data
    public static class CustomBean {
        /**
         * customurl : MacieJay
         * verified : true
         * visitors : 2051
         * banned : false
         */

        private String customurl;
        private boolean verified;
        private int visitors;
        private boolean banned;
    }

    @Data
    public static class RefreshBean {
        /**
         * queued : false
         * possible : true
         * qtime : 0
         * utime : 1586323390
         * status : 2147483647
         */

        private boolean queued;
        private boolean possible;
        private int qtime;
        private int utime;
        private int status;
    }

    @Data
    public static class StatsBean {
        /**
         * level : 566
         * casualpvp_kills : 20189
         * casualpvp_death : 12555
         * casualpvp_matchwon : 3365
         * casualpvp_matchlost : 1755
         * casualpvp_timeplayed : 4073861
         * casualpvp_hoursplayed : 1132
         * casualpvp_matches : 5120
         * casualpvp_kd : 1.61
         * casualpvp_wl : 65.72%
         * rankedpvp_kills : 71208
         * rankedpvp_death : 49158
         * rankedpvp_matchwon : 8300
         * rankedpvp_matchlost : 4673
         * rankedpvp_timeplayed : 15512872
         * rankedpvp_hoursplayed : 4309
         * rankedpvp_matches : 12973
         * rankedpvp_kd : 1.45
         * rankedpvp_wl : 63.98%
         * generalpvp_headshot : 38541
         * generalpvp_kills : 98130
         * generalpvp_timeplayed : 20818482
         * generalpve_kills : 31699
         * generalpve_death : 1410
         * generalpve_matchwon : 679
         * generalpve_matchlost : 2243
         * generalpve_headshot : 22093
         * generalpve_timeplayed : 329503
         * generalpvp_hoursplayed : 5783
         * generalpvp_death : 61713
         * generalpvp_kd : 1.59
         * generalpvp_matchwon : 11665
         * generalpvp_matchlost : 6428
         * generalpvp_matches : 18093
         * generalpvp_wl : 64.47%
         * generalpvp_hsrate : 39.28%
         * generalpvp_killassists : 23911
         * generalpvp_meleekills : 2250
         * generalpvp_revive : 2307
         * generalpvp_penetrationkills : 7987
         * generalpve_hoursplayed : 92
         * generalpve_matches : 2922
         * generalpve_kd : 22.48
         * generalpve_wl : 23.24%
         * generalpve_hsrate : 69.70%
         * plantbombpvp_matchwon : 9227
         * plantbombpvp_matchlost : 5356
         * secureareapvp_matchwon : 2189
         * secureareapvp_matchlost : 999
         * rescuehostagepvp_matchwon : 1024
         * rescuehostagepvp_matchlost : 416
         * plantbombpvp_matches : 14583
         * plantbombpvp_wl : 63.27%
         * secureareapvp_matches : 3188
         * secureareapvp_wl : 68.66%
         * rescuehostagepvp_matches : 1440
         * rescuehostagepvp_wl : 71.11%
         * tabmmr : 8944
         * tabrank : 8
         * tabrankname : Major
         */

        private int level;
        private int casualpvp_kills;
        private int casualpvp_death;
        private int casualpvp_matchwon;
        private int casualpvp_matchlost;
        private int casualpvp_timeplayed;
        private int casualpvp_hoursplayed;
        private int casualpvp_matches;
        private String casualpvp_kd;
        private String casualpvp_wl;
        private int rankedpvp_kills;
        private int rankedpvp_death;
        private int rankedpvp_matchwon;
        private int rankedpvp_matchlost;
        private int rankedpvp_timeplayed;
        private int rankedpvp_hoursplayed;
        private int rankedpvp_matches;
        private String rankedpvp_kd;
        private String rankedpvp_wl;
        private int generalpvp_headshot;
        private int generalpvp_kills;
        private int generalpvp_timeplayed;
        private int generalpve_kills;
        private int generalpve_death;
        private int generalpve_matchwon;
        private int generalpve_matchlost;
        private int generalpve_headshot;
        private int generalpve_timeplayed;
        private int generalpvp_hoursplayed;
        private int generalpvp_death;
        private double generalpvp_kd;
        private int generalpvp_matchwon;
        private int generalpvp_matchlost;
        private int generalpvp_matches;
        private String generalpvp_wl;
        private String generalpvp_hsrate;
        private int generalpvp_killassists;
        private int generalpvp_meleekills;
        private int generalpvp_revive;
        private int generalpvp_penetrationkills;
        private int generalpve_hoursplayed;
        private int generalpve_matches;
        private String generalpve_kd;
        private String generalpve_wl;
        private String generalpve_hsrate;
        private int plantbombpvp_matchwon;
        private int plantbombpvp_matchlost;
        private int secureareapvp_matchwon;
        private int secureareapvp_matchlost;
        private int rescuehostagepvp_matchwon;
        private int rescuehostagepvp_matchlost;
        private int plantbombpvp_matches;
        private String plantbombpvp_wl;
        private int secureareapvp_matches;
        private String secureareapvp_wl;
        private int rescuehostagepvp_matches;
        private String rescuehostagepvp_wl;
        private int tabmmr;
        private int tabrank;
        private String tabrankname;
    }

    @Data
    public static class RankedBean {
        /**
         * AS_kills : 0
         * AS_deaths : 0
         * AS_wins : 0
         * AS_losses : 0
         * AS_abandons : 0
         * AS_mmr : 0
         * AS_maxmmr : 0
         * AS_champ : 0
         * AS_mmrchange : 0
         * AS_actualmmr : 2500
         * AS_matches : 0
         * AS_wl : 0%
         * AS_kd : 0
         * AS_rank : 0
         * AS_rankname : Unranked
         * AS_maxrank : 0
         * AS_maxrankname : Unranked
         * AS_killpermatch : 0
         * AS_deathspermatch : 0
         * allkills : 819
         * alldeaths : 649
         * allwins : 108
         * alllosses : 63
         * allabandons : 1
         * EU_kills : 5
         * EU_deaths : 3
         * EU_wins : 0
         * EU_losses : 1
         * EU_abandons : 0
         * EU_mmr : 0
         * EU_maxmmr : 0
         * EU_champ : 0
         * EU_mmrchange : -76
         * EU_actualmmr : 3343
         * EU_matches : 1
         * EU_wl : 0%
         * EU_kd : 1.67
         * EU_rank : 0
         * EU_rankname : Unranked
         * EU_maxrank : 0
         * EU_maxrankname : Unranked
         * EU_killpermatch : 5
         * EU_deathspermatch : 3
         * NA_kills : 814
         * NA_deaths : 646
         * NA_wins : 108
         * NA_losses : 62
         * NA_abandons : 1
         * NA_mmr : 4542
         * NA_maxmmr : 4570
         * NA_champ : 0
         * NA_mmrchange : -28
         * NA_actualmmr : 4542
         * NA_matches : 171
         * NA_wl : 63%
         * NA_kd : 1.26
         * NA_rank : 20
         * NA_rankname : Diamond
         * NA_maxrank : 20
         * NA_maxrankname : Diamond
         * NA_killpermatch : 5
         * NA_deathspermatch : 4
         * mmr : 4542
         * maxmmr : 4570
         * kd : 1.45
         * rank : 20
         * rankname : Diamond
         * maxrank : 20
         * maxrankname : Diamond
         * champ : 0
         * topregion : America
         * actualmmr : 4542
         * allmatches : 172
         * allkd : 1.26
         * allwl : 63%
         * killpermatch : 5
         * deathspermatch : 4
         */

        private int AS_kills;
        private int AS_deaths;
        private int AS_wins;
        private int AS_losses;
        private int AS_abandons;
        private int AS_mmr;
        private int AS_maxmmr;
        private int AS_champ;
        private int AS_mmrchange;
        private int AS_actualmmr;
        private int AS_matches;
        private String AS_wl;
        private String AS_kd;
        private int AS_rank;
        private String AS_rankname;
        private int AS_maxrank;
        private String AS_maxrankname;
        private int AS_killpermatch;
        private int AS_deathspermatch;
        private int allkills;
        private int alldeaths;
        private int allwins;
        private int alllosses;
        private int allabandons;
        private int EU_kills;
        private int EU_deaths;
        private int EU_wins;
        private int EU_losses;
        private int EU_abandons;
        private int EU_mmr;
        private int EU_maxmmr;
        private int EU_champ;
        private int EU_mmrchange;
        private int EU_actualmmr;
        private int EU_matches;
        private String EU_wl;
        private String EU_kd;
        private int EU_rank;
        private String EU_rankname;
        private int EU_maxrank;
        private String EU_maxrankname;
        private int EU_killpermatch;
        private int EU_deathspermatch;
        private int NA_kills;
        private int NA_deaths;
        private int NA_wins;
        private int NA_losses;
        private int NA_abandons;
        private int NA_mmr;
        private int NA_maxmmr;
        private int NA_champ;
        private int NA_mmrchange;
        private int NA_actualmmr;
        private int NA_matches;
        private String NA_wl;
        private String NA_kd;
        private int NA_rank;
        private String NA_rankname;
        private int NA_maxrank;
        private String NA_maxrankname;
        private int NA_killpermatch;
        private int NA_deathspermatch;
        private int mmr;
        private int maxmmr;
        private double kd;
        private int rank;
        private String rankname;
        @SerializedName("maxrank")
        private int maxRank;
        private String maxrankname;
        private int champ;
        private String topregion;
        private int actualmmr;
        private int allmatches;
        private String allkd;
        private String allwl;
        private int killpermatch;
        private int deathspermatch;
    }
}
