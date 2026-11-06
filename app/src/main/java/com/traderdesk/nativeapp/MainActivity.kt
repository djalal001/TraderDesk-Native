package com.traderdesk.nativeapp

import android.graphics.ImageDecoder
import android.graphics.drawable.AnimatedImageDrawable
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.os.Build
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.NumberFormat
import java.util.Locale

private val Bg=Color(0xFF0B0F16); private val Surface1=Color(0xFF121823); private val Surface2=Color(0xFF19212F); private val Surface3=Color(0xFF212B3D); private val Text1=Color(0xFFEEF2F8); private val Text2=Color(0xFFA9B4C6); private val Text3=Color(0xFF8793A8); private val Accent=Color(0xFF4C96FF); private val Profit=Color(0xFF34D399); private val Loss=Color(0xFFFF6B7A); private val Warn=Color(0xFFF5B84B)
private val ArabicFont=FontFamily(Font(com.traderdesk.nativeapp.R.font.ibm_plex_arabic_regular,FontWeight.Normal),Font(com.traderdesk.nativeapp.R.font.ibm_plex_arabic_medium,FontWeight.Medium),Font(com.traderdesk.nativeapp.R.font.ibm_plex_arabic_semibold,FontWeight.SemiBold),Font(com.traderdesk.nativeapp.R.font.ibm_plex_arabic_bold,FontWeight.Bold))

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); enableEdgeToEdge(); setContent { TraderDeskApp() } }
}

@Composable fun TraderDeskApp(vm: DeskViewModel = viewModel()) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        val scheme=darkColorScheme(background=Bg,surface=Surface1,surfaceVariant=Surface2,primary=Accent,onPrimary=Color.White,secondary=Profit,error=Loss,onBackground=Text1,onSurface=Text1,onSurfaceVariant=Text2)
        MaterialTheme(colorScheme=scheme,typography=Typography(bodyLarge=androidx.compose.ui.text.TextStyle(fontFamily=ArabicFont,fontSize=14.sp),titleLarge=androidx.compose.ui.text.TextStyle(fontFamily=ArabicFont,fontWeight=FontWeight.Bold,fontSize=20.sp))) { Surface(Modifier.fillMaxSize(),color=Bg){ Scaffold(containerColor=Bg,bottomBar={BottomNav(vm)}) { pad -> Box(Modifier.padding(pad).fillMaxSize()){ when(vm.tab){Tab.OVERVIEW->Overview(vm);Tab.JOURNAL->Journal(vm);Tab.DAYS->Days(vm);Tab.ACCOUNTS->Accounts(vm);Tab.PERFORMANCE->Performance(vm);Tab.SETTINGS->Settings(vm)} } } } }
    }
}

@Composable private fun BottomNav(vm: DeskViewModel) { NavigationBar(containerColor=Surface1,tonalElevation=0.dp) { val entries=listOf(Tab.OVERVIEW to "الرئيسية",Tab.JOURNAL to "الصفقات",Tab.DAYS to "الأيام",Tab.ACCOUNTS to "الحسابات",Tab.PERFORMANCE to "الأداء",Tab.SETTINGS to "الإعدادات"); entries.forEach { (tab,label)-> NavigationBarItem(selected=vm.tab==tab,onClick={vm.navigateTo(tab)},icon={Icon(if(tab==vm.tab) iconFor(tab,true) else iconFor(tab,false),label)},label={Text(label,fontFamily=ArabicFont,fontSize=9.sp)}) } } }
@Composable private fun iconFor(tab:Tab,filled:Boolean)=when(tab){Tab.OVERVIEW->if(filled) Icons.Filled.Home else Icons.Outlined.Home;Tab.JOURNAL->if(filled) Icons.Filled.ReceiptLong else Icons.Outlined.ReceiptLong;Tab.DAYS->if(filled) Icons.Filled.CalendarMonth else Icons.Outlined.CalendarMonth;Tab.ACCOUNTS->if(filled) Icons.Filled.AccountBalanceWallet else Icons.Outlined.AccountBalanceWallet;Tab.PERFORMANCE->if(filled) Icons.Filled.Analytics else Icons.Outlined.Analytics;Tab.SETTINGS->if(filled) Icons.Filled.Settings else Icons.Outlined.Settings}

@Composable private fun PageHeader(title:String,subtitle:String="",action:@Composable (() -> Unit)?=null){ Row(Modifier.fillMaxWidth().padding(horizontal=16.dp,vertical=14.dp),verticalAlignment=Alignment.CenterVertically){ Column(Modifier.weight(1f)){Text(title,color=Text1,fontSize=24.sp,fontWeight=FontWeight.Bold);if(subtitle.isNotBlank())Text(subtitle,color=Text3,fontSize=12.sp)};action?.invoke()} }
@Composable private fun CardBox(modifier:Modifier=Modifier,hero:Boolean=false,content:@Composable ColumnScope.()->Unit){ Card(modifier=modifier,shape=RoundedCornerShape(20.dp),colors=CardDefaults.cardColors(containerColor=if(hero) Color(0xFF192744) else Surface1),elevation=CardDefaults.cardElevation(0.dp)){Column(Modifier.padding(16.dp),content=content)} }
@Composable private fun Empty(title:String,detail:String,onAdd:(()->Unit)?=null){ CardBox(Modifier.padding(16.dp)){Column(horizontalAlignment=Alignment.CenterHorizontally,modifier=Modifier.fillMaxWidth()){Icon(Icons.Outlined.AccountBalanceWallet,null,tint=Accent,modifier=Modifier.size(38.dp));Spacer(Modifier.height(8.dp));Text(title,color=Text1,fontWeight=FontWeight.Bold);Text(detail,color=Text2,textAlign=TextAlign.Center);onAdd?.let{Spacer(Modifier.height(12.dp));Button(onClick=it){Text("إضافة الآن")}}}} }

@Composable private fun Overview(vm:DeskViewModel){ val account=vm.selectedAccount; val trades=vm.accountTrades; val a=analytics(trades,account?.startingBalance?:0.0); LazyColumn(Modifier.fillMaxSize().padding(bottom=12.dp)){item{PageHeader("نظرة عامة", "لوحة متابعة دفتر التداول",if(account!=null){ {AccountPill(account)} } else null)}; if(account==null)item{Empty("لا توجد حسابات","أنشئ حساب تداول لبدء تسجيل صفقاتك",{vm.navigateTo(Tab.ACCOUNTS)})} else {item{Hero(account,a,trades)};item{MetricGrid(a)};if(account.profitTarget>0)item{TargetCard(account,a)};item{SummaryCard(a,trades)}}} }
@Composable private fun AccountPill(account:Account){Surface(shape=RoundedCornerShape(30.dp),color=Accent.copy(alpha=.15f)){Text(account.name,color=Accent,fontSize=12.sp,fontWeight=FontWeight.Bold,modifier=Modifier.padding(horizontal=12.dp,vertical=7.dp))}}
@Composable private fun Hero(account:Account,a:Analytics,trades:List<Trade>){CardBox(Modifier.padding(horizontal=16.dp),true){Row(verticalAlignment=Alignment.CenterVertically){Icon(Icons.Outlined.AccountBalanceWallet,null,tint=Accent);Spacer(Modifier.width(10.dp));Text(account.name,color=Text1,fontWeight=FontWeight.Bold,modifier=Modifier.weight(1f));Surface(shape=RoundedCornerShape(30.dp),color=Accent.copy(alpha=.15f)){Text(if(account.type==AccountType.DEMO)"تجريبي" else "حقيقي",color=Accent,fontSize=11.sp,modifier=Modifier.padding(horizontal=10.dp,vertical=5.dp))}};Spacer(Modifier.height(18.dp));Text("الرصيد الحالي",color=Text2,fontSize=12.sp);Text(money(account.startingBalance+a.net,account.currency),color=Text1,fontSize=32.sp,fontWeight=FontWeight.Bold);Text("الرصيد الابتدائي: ${money(account.startingBalance,account.currency)}",color=Text3,fontSize=11.sp);Spacer(Modifier.height(15.dp));EquityChart(trades,account.startingBalance)}}
@Composable private fun EquityChart(trades:List<Trade>,start:Double){val vals=trades.sortedBy{it.tradedAt}.runningFold(start){sum,t->sum+t.result};Canvas(Modifier.fillMaxWidth().height(105.dp)){if(vals.size>1){val min=vals.min();val max=vals.max();val range=(max-min).takeIf{it>0}?:1.0;val path=Path();vals.forEachIndexed{i,v->val x=size.width*i.toFloat()/(vals.lastIndex);val y=size.height-((v-min)/range*size.height*.8).toFloat()-size.height*.1f;if(i==0)path.moveTo(x,y)else path.lineTo(x,y)};drawPath(path,Brush.horizontalGradient(listOf(Accent,Profit)),style=Stroke(3.dp.toPx(),cap=StrokeCap.Round))}}}
@Composable private fun MetricGrid(a:Analytics){Column(Modifier.padding(horizontal=16.dp,vertical=12.dp)){Row(Modifier.fillMaxWidth()){Metric("صافي الربح",money(a.net,"USD"),if(a.net>=0)Profit else Loss,Icons.Filled.TrendingUp,Modifier.weight(1f));Spacer(Modifier.width(10.dp));Metric("نسبة الفوز","${"%.1f".format(Locale.US,a.winRate)}%",Accent,Icons.Outlined.TrackChanges,Modifier.weight(1f))};Spacer(Modifier.height(10.dp));Row(Modifier.fillMaxWidth()){Metric("عدد الصفقات",a.count.toString(),Text1,Icons.Outlined.ReceiptLong,Modifier.weight(1f));Spacer(Modifier.width(10.dp));Metric("أقصى تراجع",money(a.maxDrawdown,"USD"),if(a.maxDrawdown>0)Loss else Text1,Icons.Filled.SouthEast,Modifier.weight(1f))}}}
@Composable private fun Metric(label:String,value:String,color:Color,icon:androidx.compose.ui.graphics.vector.ImageVector,modifier:Modifier){CardBox(modifier){Icon(icon,null,tint=color,modifier=Modifier.size(21.dp));Spacer(Modifier.height(8.dp));Text(label,color=Text2,fontSize=12.sp);Text(value,color=color,fontSize=17.sp,fontWeight=FontWeight.Bold)}}
@Composable private fun TargetCard(account:Account,a:Analytics){CardBox(Modifier.padding(horizontal=16.dp)){Row{Text("هدف الربح",color=Text1,fontWeight=FontWeight.Bold,modifier=Modifier.weight(1f));Text("${(a.net/account.profitTarget*100).coerceIn(0.0,100.0).toInt()}%",color=Accent)};Spacer(Modifier.height(8.dp));LinearProgressIndicator(progress={(a.net/account.profitTarget).coerceIn(0.0,1.0).toFloat()},modifier=Modifier.fillMaxWidth(),color=Accent,trackColor=Surface3);Spacer(Modifier.height(8.dp));Text("${money(a.net,account.currency)} / ${money(account.profitTarget,account.currency)}",color=Text2,fontSize=12.sp)}}
@Composable private fun SummaryCard(a:Analytics,trades:List<Trade>){CardBox(Modifier.padding(16.dp)){Text("ملخص الأداء",color=Text1,fontSize=16.sp,fontWeight=FontWeight.Bold);Spacer(Modifier.height(10.dp));listOf("نتيجة اليوم" to money(trades.filter{it.tradedAt.take(10)==java.time.Instant.now().toString().take(10)}.sumOf{it.result},"USD"),"الرابحة" to a.wins.toString(),"الخاسرة" to a.losses.toString(),"التعادل" to a.breakEven.toString(),"متوسط الربح" to money(a.averageWin,"USD"),"متوسط الخسارة" to money(a.averageLoss,"USD")).forEach{(l,v)->Row(Modifier.fillMaxWidth().padding(vertical=6.dp)){Text(l,color=Text2,modifier=Modifier.weight(1f));Text(v,color=Text1,fontWeight=FontWeight.Bold)}}}}

@Composable private fun Accounts(vm:DeskViewModel){var show by remember{mutableStateOf(false)};PageColumn{PageHeader("الحسابات","إدارة حسابات التداول",{IconButton(onClick={show=true}){Icon(Icons.Filled.Add,null,tint=Accent)}});if(vm.accounts.isEmpty())Empty("لا توجد حسابات","يمكنك إنشاء حساب تجريبي أو حقيقي",{show=true}) else vm.accounts.forEach{account->CardBox(Modifier.padding(horizontal=16.dp,vertical=5.dp)){Row(verticalAlignment=Alignment.CenterVertically){Icon(Icons.Outlined.AccountBalanceWallet,null,tint=Accent);Spacer(Modifier.width(10.dp));Column(Modifier.weight(1f)){Text(account.name,color=Text1,fontWeight=FontWeight.Bold);Text("${account.currency} · ${money(account.startingBalance,account.currency)}",color=Text2,fontSize=12.sp)};Text(if(account.type==AccountType.DEMO)"تجريبي" else "حقيقي",color=if(account.type==AccountType.DEMO) Accent else Warn,fontSize=12.sp)};if(vm.selectedAccount?.id==account.id)Text("الحساب المحدد",color=Profit,fontSize=11.sp,modifier=Modifier.padding(top=8.dp)) else TextButton(onClick={vm.selectAccount(account.id)}){Text("تحديد")}}};if(show)AccountDialog({name,balance,type,target->vm.addAccount(Account(name=name,startingBalance=balance,type=type,profitTarget=target));show=false},{show=false})}}
@Composable private fun PageColumn(content:@Composable ColumnScope.()->Unit){Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()),content=content)}

@Composable private fun AccountDialog(onSave:(String,Double,AccountType,Double)->Unit,onDismiss:()->Unit){var name by remember{mutableStateOf("")};var balance by remember{mutableStateOf("")};var target by remember{mutableStateOf("")};var real by remember{mutableStateOf(false)};AlertDialog(onDismissRequest=onDismiss,title={Text("إضافة حساب")},text={Column{OutlinedTextField(name,{name=it},label={Text("اسم الحساب")},singleLine=true);OutlinedTextField(balance,{balance=it},label={Text("الرصيد الابتدائي")},singleLine=true);OutlinedTextField(target,{target=it},label={Text("هدف الربح (اختياري)")},singleLine=true);Row(verticalAlignment=Alignment.CenterVertically){Checkbox(real,{real=it});Text("حساب حقيقي")}}},confirmButton={Button(onClick={onSave(name.ifBlank{"Trading Account"},balance.toDoubleOrNull()?:0.0,if(real)AccountType.REAL else AccountType.DEMO,target.toDoubleOrNull()?:0.0)}){Text("حفظ")}},dismissButton={TextButton(onClick=onDismiss){Text("إلغاء")}})}

@Composable
private fun Journal(vm: DeskViewModel) {
    var show by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    PageColumn {
        PageHeader("السجل", "${vm.accountTrades.size} صفقة") {
            Row {
                IconButton(onClick = {}) { Icon(Icons.Outlined.Download, null, tint = Accent) }
                IconButton(onClick = { show = true }, enabled = vm.selectedAccount != null) { Icon(Icons.Filled.Add, null, tint = Accent) }
            }
        }
        if (vm.selectedAccount == null) {
            Empty("لا يوجد حساب محدد", "أضف حسابًا أولًا") { vm.navigateTo(Tab.ACCOUNTS) }
        } else {
            OutlinedTextField(query, { query = it }, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), placeholder = { Text("بحث في الصفقات") }, leadingIcon = { Icon(Icons.Filled.Search, null) }, singleLine = true)
            Row(Modifier.horizontalScroll(rememberScrollState()).padding(16.dp)) {
                FilterPill("الكل", true) {}
                FilterPill("الرابحة", false) {}
                FilterPill("الخاسرة", false) {}
            }
            val visible = vm.accountTrades.filter { query.isBlank() || "${it.market} ${it.reason} ${it.notes}".contains(query, true) }.sortedByDescending { it.tradedAt }
            if (visible.isEmpty()) Text("لا توجد صفقات", color = Text2, modifier = Modifier.padding(24.dp))
            visible.forEach { TradeCard(it, vm) }
        }
    }
    if (show && vm.selectedAccount != null) {
        TradeDialog(vm.selectedAccount!!.id, onDismiss = { show = false }) { trade -> vm.addTrade(trade); show = false }
    }
}
@Composable private fun FilterPill(text:String,selected:Boolean,onClick:()->Unit){Surface(shape=RoundedCornerShape(30.dp),color=if(selected)Accent else Surface2,modifier=Modifier.padding(end=8.dp).clickable(onClick=onClick)){Text(text,color=if(selected)Color.White else Text2,fontSize=12.sp,modifier=Modifier.padding(horizontal=15.dp,vertical=8.dp))}}
@Composable
private fun TradeCard(t:Trade,vm:DeskViewModel){
    var edit by remember { mutableStateOf(false) }
    CardBox(Modifier.padding(horizontal=16.dp,vertical=5.dp)){
        Row(verticalAlignment=Alignment.CenterVertically){
            Column(Modifier.weight(1f)){Text(t.market.ifBlank{"—"},color=Text1,fontWeight=FontWeight.Bold);Text("${t.reason.ifBlank{"بدون سبب"}} · ${t.mood.ifBlank{"—"}}",color=Text2,fontSize=12.sp)}
            Column(horizontalAlignment=Alignment.End){Text(money(t.result,vm.selectedAccount?.currency?:"USD"),color=if(t.result>0) Profit else if(t.result<0) Loss else Text2,fontWeight=FontWeight.Bold);Text(if(t.side==TradeSide.BUY)"شراء" else "بيع",color=Accent,fontSize=11.sp)}
        }
        Spacer(Modifier.height(6.dp)); Row(verticalAlignment=Alignment.CenterVertically){
            Text(t.confirmation.ifBlank{"بدون تأكيد"},color=Text3,fontSize=11.sp,modifier=Modifier.weight(1f));
            t.strategyAdherence?.let{Text(if(it==Adherence.FOLLOWED)"ملتزم بالخطة" else "خارج الخطة",color=if(it==Adherence.FOLLOWED) Profit else Warn,fontSize=11.sp)}
            IconButton(onClick={edit=true}){Icon(Icons.Outlined.Edit,null,tint=Accent)}; IconButton(onClick={vm.deleteTrade(t.id)}){Icon(Icons.Outlined.Delete,null,tint=Loss)}
        }
    }
    if(edit) TradeDialog(t.accountId, existing=t, onDismiss={edit=false}){updated->vm.updateTrade(updated);edit=false}
}

private val executionOptions=listOf("followedPlan" to "اتباع الخطة","avoidedEntryBecauseFear" to "خوف من الدخول","enteredEarly" to "دخول مبكر","entryConditionsNotMet" to "الشروط غير مكتملة","hesitationPartialSize" to "تردد / حجم جزئي","lateEntry" to "دخول متأخر","stopLossMoved" to "تحريك وقف الخسارة","exitedEarlyBecauseFearOfLosingProfit" to "خروج مبكر")

@Composable
private fun TradeDialog(accountId:String,existing:Trade?=null,onDismiss:()->Unit,onSave:(Trade)->Unit){
    var market by remember{mutableStateOf(existing?.market ?: "")}; var reason by remember{mutableStateOf(existing?.reason ?: "")}; var result by remember{mutableStateOf(existing?.result?.toString() ?: "")}; var expectedLoss by remember{mutableStateOf(existing?.expectedLoss?.toString() ?: "")}; var risk by remember{mutableStateOf(existing?.riskPercent?.toString() ?: "")}; var entryTimeframe by remember{mutableStateOf(existing?.entryTimeframe ?: "")}; var confirmation by remember{mutableStateOf(existing?.confirmation ?: "")}; var confirmationTimeframe by remember{mutableStateOf(existing?.confirmationSetups?.firstOrNull()?.timeframe ?: "")}; var mood by remember{mutableStateOf(existing?.mood ?: "")}; var sequence by remember{mutableStateOf(existing?.sequence ?: "")}; var execution by remember{mutableStateOf(existing?.executionReviewKeys?.joinToString(", ") ?: "")}; var executionSet by remember{mutableStateOf(existing?.executionReviewKeys?.toSet() ?: emptySet())}; var noEntry by remember{mutableStateOf(existing?.noEntryReasons?.joinToString(", ") ?: "")}; var notes by remember{mutableStateOf(existing?.notes ?: "")}; var beforeImage by remember{mutableStateOf(existing?.beforeImagePath)}; var afterImage by remember{mutableStateOf(existing?.afterImagePath)}; var confirmationImage by remember{mutableStateOf(existing?.confirmationImagePath)}; var imageSlot by remember{mutableStateOf("")}; var buy by remember{mutableStateOf(existing?.side != TradeSide.SELL)}; var followed by remember{mutableStateOf(existing?.strategyAdherence?.let{it==Adherence.FOLLOWED})}; val picker=rememberLauncherForActivityResult(ActivityResultContracts.GetContent()){uri->when(imageSlot){"before"->beforeImage=uri?.toString();"after"->afterImage=uri?.toString();"confirmation"->confirmationImage=uri?.toString()}}
    AlertDialog(onDismissRequest=onDismiss,title={Text("إضافة صفقة")},text={Column(Modifier.verticalScroll(rememberScrollState())){
        OutlinedTextField(market,{market=it},label={Text("السوق / الرمز")},singleLine=true); OutlinedTextField(reason,{reason=it},label={Text("سبب الدخول")},singleLine=true); OutlinedTextField(entryTimeframe,{entryTimeframe=it},label={Text("فريم الدخول")},singleLine=true)
        Row{FilterPill("شراء",buy){buy=true};FilterPill("بيع",!buy){buy=false}}
        OutlinedTextField(result,{result=it},label={Text("النتيجة المالية")},singleLine=true); OutlinedTextField(expectedLoss,{expectedLoss=it},label={Text("الخسارة المتوقعة")},singleLine=true); OutlinedTextField(risk,{risk=it},label={Text("نسبة المخاطرة %")},singleLine=true)
        OutlinedTextField(confirmation,{confirmation=it},label={Text("تأكيد الدخول")},singleLine=true); OutlinedTextField(confirmationTimeframe,{confirmationTimeframe=it},label={Text("فريم التأكيد")},singleLine=true); OutlinedTextField(sequence,{sequence=it},label={Text("تسلسل الخطة")},singleLine=true); OutlinedTextField(mood,{mood=it},label={Text("الحالة النفسية")},singleLine=true)
        Text("الالتزام بالخطة",color=Text2,fontSize=12.sp,modifier=Modifier.padding(top=8.dp)); Row{FilterPill("ملتزم",followed==true){followed=true};FilterPill("غير ملتزم",followed==false){followed=false};FilterPill("غير محدد",followed==null){followed=null}}
        Text("سجل التنفيذ",color=Text2,fontSize=12.sp,modifier=Modifier.padding(top=8.dp)); Row(Modifier.horizontalScroll(rememberScrollState())){executionOptions.forEach{(key,label)->FilterPill(label,executionSet.contains(key)){executionSet=if(executionSet.contains(key))executionSet-key else executionSet+key}}}; OutlinedTextField(noEntry,{noEntry=it},label={Text("أسباب عدم الدخول مفصولة بفواصل")},singleLine=false); OutlinedTextField(notes,{notes=it},label={Text("ملاحظات")},singleLine=false); Text("صور الصفقة",color=Text2,fontSize=12.sp,modifier=Modifier.padding(top=8.dp)); Row(Modifier.horizontalScroll(rememberScrollState())){Button(onClick={imageSlot="before";picker.launch("image/*")} ){Text(if(beforeImage==null)"قبل" else "قبل ✓")};Spacer(Modifier.width(6.dp));Button(onClick={imageSlot="after";picker.launch("image/*")} ){Text(if(afterImage==null)"بعد" else "بعد ✓")};Spacer(Modifier.width(6.dp));Button(onClick={imageSlot="confirmation";picker.launch("image/*")} ){Text(if(confirmationImage==null)"التأكيد" else "التأكيد ✓")}}
    }},confirmButton={Button(onClick={val reasonValue=reason.trim();onSave(Trade(id=existing?.id ?: java.util.UUID.randomUUID().toString(),accountId=accountId,market=market.trim(),reason=reasonValue,result=result.toDoubleOrNull()?:0.0,expectedLoss=expectedLoss.toDoubleOrNull(),riskPercent=risk.toDoubleOrNull()?:0.0,side=if(buy)TradeSide.BUY else TradeSide.SELL,confirmation=confirmation.trim(),sequence=sequence.trim(),mood=mood.trim(),entryTimeframe=entryTimeframe.trim().ifBlank{null},entrySetups=if(reasonValue.isBlank()) emptyList() else listOf(EntrySetup(reasonValue,entryTimeframe.trim())),confirmationSetups=if(confirmation.isBlank()) emptyList() else listOf(ConfirmationSetup(confirmation.trim(),confirmationTimeframe.trim())),executionReviewKeys=executionSet.toList(),noEntryReasons=noEntry.split(",").map{it.trim()}.filter{it.isNotBlank()},strategyAdherence=when(followed){true->Adherence.FOLLOWED;false->Adherence.NOT_FOLLOWED;else->null},beforeImagePath=beforeImage,afterImagePath=afterImage,confirmationImagePath=confirmationImage,notes=notes.trim()));onDismiss()}){Text("حفظ")}},dismissButton={TextButton(onClick=onDismiss){Text("إلغاء")}})
}

@Composable private fun Tools(vm:DeskViewModel){var balance by remember{mutableStateOf(vm.selectedAccount?.startingBalance?.toString()?:(""))};var risk by remember{mutableStateOf("1")};var entry by remember{mutableStateOf("")};var stop by remember{mutableStateOf("")};var target by remember{mutableStateOf("")};var output by remember{mutableStateOf<RiskResult?>(null)};PageColumn{PageHeader("الأدوات","حاسبة المخاطرة والعائد");CardBox(Modifier.padding(16.dp)){Text("حاسبة حجم الصفقة",color=Text1,fontSize=18.sp,fontWeight=FontWeight.Bold);Spacer(Modifier.height(12.dp));OutlinedTextField(balance,{balance=it},label={Text("رصيد الحساب")},singleLine=true);OutlinedTextField(risk,{risk=it},label={Text("المخاطرة %")},singleLine=true);OutlinedTextField(entry,{entry=it},label={Text("سعر الدخول")},singleLine=true);OutlinedTextField(stop,{stop=it},label={Text("وقف الخسارة")},singleLine=true);OutlinedTextField(target,{target=it},label={Text("الهدف (اختياري)")},singleLine=true);Button(onClick={runCatching{output=calculateRisk(balance.toDouble(),risk.toDouble(),entry.toDouble(),stop.toDouble(),target.toDoubleOrNull())}},modifier=Modifier.fillMaxWidth()){Text("حساب")};output?.let{r->Spacer(Modifier.height(14.dp));Text("المخاطرة النقدية: ${money(r.riskCash,"USD")}",color=Text1);Text("المسافة: ${"%.4f".format(r.stopDistance)} · الوحدات: ${"%.2f".format(r.units)}",color=Text2);r.targetCash?.let{x->Text("قيمة الهدف: ${money(x,"USD")} · R:R ${"%.2f".format(r.rewardRisk)}",color=Profit)}}};CardBox(Modifier.padding(horizontal=16.dp)){Text("أصول الإيموجي الأصلية",color=Text1,fontWeight=FontWeight.Bold);Text("تم تضمين الإيموجيات المتحركة من المشروع الأصلي داخل نسخة Native.",color=Text2,fontSize=12.sp);Spacer(Modifier.height(10.dp));AnimatedEmoji(R.drawable.emoji_executed_per_plan)}}}

@Composable
private fun Settings(vm: DeskViewModel) {
    val context = LocalContext.current
    var restoreOpen by remember { mutableStateOf(false) }
    var restoreText by remember { mutableStateOf("") }
    PageColumn {
        PageHeader("الإعدادات", "تخصيص TraderDesk")
        CardBox(Modifier.padding(16.dp)) {
            Row(verticalAlignment=Alignment.CenterVertically) { Icon(Icons.Outlined.DarkMode,null,tint=Accent); Spacer(Modifier.width(12.dp)); Text("الوضع الداكن",color=Text1,modifier=Modifier.weight(1f)); Switch(checked=vm.isDark,onCheckedChange={vm.toggleTheme()}) }
            HorizontalDivider(Modifier.padding(vertical=12.dp),color=Surface3)
            Row(verticalAlignment=Alignment.CenterVertically) { Icon(Icons.Outlined.Language,null,tint=Accent); Spacer(Modifier.width(12.dp)); Column { Text("اللغة",color=Text1); Text("العربية · RTL",color=Text2,fontSize=12.sp) } }
        }
        CardBox(Modifier.padding(horizontal=16.dp,vertical=6.dp)) {
            Text("النسخ الاحتياطي والبيانات",color=Text1,fontWeight=FontWeight.Bold)
            Text("يحفظ الحسابات والصفقات والإعدادات وكل الحقول المتقدمة بصيغة JSON.",color=Text2,fontSize=12.sp,modifier=Modifier.padding(top=6.dp))
            Row(Modifier.fillMaxWidth().padding(top=12.dp),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                Button(onClick={ val clip=context.getSystemService(ClipboardManager::class.java); clip.setPrimaryClip(ClipData.newPlainText("TraderDesk backup",vm.exportJson())) },modifier=Modifier.weight(1f)){Text("نسخ النسخة")}
                OutlinedButton(onClick={ val send=Intent(Intent.ACTION_SEND).apply{type="application/json";putExtra(Intent.EXTRA_TEXT,vm.exportJson())};context.startActivity(Intent.createChooser(send,"مشاركة النسخة")) },modifier=Modifier.weight(1f)){Text("مشاركة")}
            }
            OutlinedButton(onClick={restoreOpen=true},modifier=Modifier.fillMaxWidth().padding(top=8.dp)){Text("استعادة من JSON")}
            OutlinedButton(onClick={ val csv=buildCsv(vm.trades,vm.accounts); val clip=context.getSystemService(ClipboardManager::class.java); clip.setPrimaryClip(ClipData.newPlainText("TraderDesk CSV",csv)) },modifier=Modifier.fillMaxWidth().padding(top=8.dp)){Text("نسخ سجل الصفقات CSV")}
        }
        CardBox(Modifier.padding(horizontal=16.dp)) { Text("عن التطبيق",color=Text1,fontWeight=FontWeight.Bold); Text("TraderDesk Native · Kotlin + Jetpack Compose\nإعادة كتابة أصلية مع الحفاظ على تصميم المشروع والمنطق والأصول.",color=Text2,fontSize=12.sp,modifier=Modifier.padding(top=8.dp)) }
    }
    if(restoreOpen) AlertDialog(onDismissRequest={restoreOpen=false},title={Text("استعادة النسخة")},text={OutlinedTextField(restoreText,{restoreText=it},modifier=Modifier.fillMaxWidth().heightIn(min=150.dp),label={Text("ألصق JSON هنا")})},confirmButton={Button(onClick={if(vm.restoreJson(restoreText)){restoreOpen=false;restoreText=""}}){Text("استعادة")}},dismissButton={TextButton(onClick={restoreOpen=false}){Text("إلغاء")}})
}
private fun buildCsv(trades:List<Trade>,accounts:List<Account>):String { val header="id,account,market,side,result,riskPercent,expectedLoss,mood,reason,confirmation,tradedAt,notes\n"; return header+trades.joinToString("\n"){t->val a=accounts.firstOrNull{it.id==t.accountId};listOf(t.id,a?.name.orEmpty(),t.market,t.side.name,t.result,t.riskPercent,t.expectedLoss?:"",t.mood,t.reason,t.confirmation,t.tradedAt,t.notes).joinToString(","){v->"\"${v.toString().replace("\"","\"\"")}\""}} }
@Composable private fun AnimatedEmoji(@DrawableRes id:Int){val context=LocalContext.current;AndroidView(factory={ImageView(it).apply{scaleType=ImageView.ScaleType.CENTER_INSIDE; if(Build.VERSION.SDK_INT>=28){val d=ImageDecoder.decodeDrawable(ImageDecoder.createSource(resources,id));setImageDrawable(d);if(d is AnimatedImageDrawable)d.start()}}},modifier=Modifier.size(84.dp))}
private fun money(value:Double,currency:String):String{val sign=if(value<0)"-" else "+";val format=NumberFormat.getCurrencyInstance(Locale.US);format.currency=java.util.Currency.getInstance(currency.ifBlank{"USD"});return "$sign${format.format(kotlin.math.abs(value))}"}


@Composable
private fun Days(vm: DeskViewModel) {
    var month by remember { mutableStateOf(java.time.YearMonth.now()) }
    var selected by remember { mutableStateOf(java.time.LocalDate.now()) }
    val account = vm.selectedAccount
    val grouped = vm.accountTrades.groupBy { runCatching { java.time.LocalDate.parse(it.tradedAt.take(10)) }.getOrNull() }
    val days = (1..month.lengthOfMonth()).map { month.atDay(it) }
    val leading = month.atDay(1).dayOfWeek.value % 7
    val cells = List(leading) { null as java.time.LocalDate? } + days
    val monthTrades = days.sumOf { grouped[it]?.size ?: 0 }
    val monthNet = days.sumOf { grouped[it].orEmpty().sumOf { t -> t.result } }
    PageColumn {
        PageHeader("دفتر الأيام", account?.name ?: "اختر حسابًا") {
            IconButton(onClick = { month = java.time.YearMonth.now(); selected = java.time.LocalDate.now() }) { Icon(Icons.Filled.Today, null, tint = Accent) }
        }
        if (account == null) Empty("لا توجد حسابات", "أضف حسابًا أولًا") { vm.navigateTo(Tab.ACCOUNTS) } else {
            CardBox(Modifier.padding(horizontal = 12.dp), hero = false) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { month = month.minusMonths(1) }) { Icon(Icons.Filled.ChevronLeft, null) }
                    Text("${month.monthValue} / ${month.year}", color = Text1, fontSize = 19.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                    IconButton(onClick = { month = month.plusMonths(1) }) { Icon(Icons.Filled.ChevronRight, null) }
                }
                Row(Modifier.fillMaxWidth()) { listOf("أح", "إث", "ثل", "أر", "خم", "جم", "سب").forEach { Text(it, color = Text3, fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(1f)) } }
                Spacer(Modifier.height(8.dp))
                cells.chunked(7).forEach { week -> Row(Modifier.fillMaxWidth()) { week.forEach { date ->
                    Box(Modifier.weight(1f).height(70.dp).padding(2.dp).clip(RoundedCornerShape(10.dp)).background(if(date == selected) Accent.copy(.22f) else Surface2).clickable(enabled=date!=null){ if(date!=null) selected=date }, contentAlignment=Alignment.TopCenter) {
                        if(date!=null){ val result=grouped[date].orEmpty().sumOf{it.result}; Column(horizontalAlignment=Alignment.CenterHorizontally,modifier=Modifier.padding(top=6.dp)){Text(date.dayOfMonth.toString(),color=if(date==java.time.LocalDate.now()) Accent else Text1,fontWeight=FontWeight.Bold,fontSize=12.sp);if(grouped[date].orEmpty().isNotEmpty()){Text(grouped[date]!!.size.toString(),color=Text2,fontSize=10.sp);Text(if(result>=0)"+" else "−",color=if(result>0)Profit else if(result<0)Loss else Text2,fontSize=13.sp,fontWeight=FontWeight.Bold)}}}
                    }
                } } }
                Spacer(Modifier.height(12.dp)); Row { Text("صفقات الشهر: $monthTrades", color = Text2, modifier = Modifier.weight(1f)); Text(money(monthNet, account.currency), color = if(monthNet>=0) Profit else Loss, fontWeight = FontWeight.Bold) }
            }
            Spacer(Modifier.height(14.dp)); CardBox(Modifier.padding(horizontal=16.dp)) { Text("تفاصيل ${selected.dayOfMonth}/${selected.monthValue}", color=Text1, fontWeight=FontWeight.Bold); val day=grouped[selected].orEmpty(); if(day.isEmpty()) Text("لا توجد صفقات في هذا اليوم",color=Text2,modifier=Modifier.padding(top=8.dp)) else day.forEach { TradeCard(it,vm) } }
        }
    }
}

@Composable
private fun Performance(vm: DeskViewModel) {
    var period by remember { mutableStateOf("الكل") }
    var behavior by remember { mutableStateOf(false) }
    val now=java.time.LocalDate.now()
    val filtered=vm.accountTrades.filter { t -> val date=runCatching{java.time.LocalDate.parse(t.tradedAt.take(10))}.getOrNull() ?: now; when(period){"الأسبوع"->date>=now.minusDays(6);"الشهر"->date.month==now.month&&date.year==now.year;"30 يومًا"->date>=now.minusDays(29);else->true} }
    val account=vm.selectedAccount; val a=analytics(filtered,account?.startingBalance?:0.0)
    PageColumn {
        PageHeader("الأداء", account?.name ?: "تحليلات التداول")
        CardBox(Modifier.padding(horizontal=16.dp)) { Text("فترة التحليل",color=Text1,fontWeight=FontWeight.Bold); Row(Modifier.horizontalScroll(rememberScrollState()).padding(top=10.dp)){ listOf("الأسبوع","الشهر","30 يومًا","الكل").forEach{FilterPill(it,period==it){period=it}} }; Row(verticalAlignment=Alignment.CenterVertically,modifier=Modifier.padding(top=12.dp)){Text("مختبر السلوك",color=Text1,modifier=Modifier.weight(1f));Switch(behavior,{behavior=it})} }
        if(account==null) Empty("لا توجد حسابات","أضف حسابًا أولًا") { vm.navigateTo(Tab.ACCOUNTS) } else if(behavior) BehaviorPanel(filtered) else {
            MetricGrid(a); CardBox(Modifier.padding(16.dp)){Text("توزيع النتائج",color=Text1,fontSize=17.sp,fontWeight=FontWeight.Bold);StatRow("إجمالي الربح",money(filtered.filter{it.result>0}.sumOf{it.result},account.currency),Profit);StatRow("إجمالي الخسارة",money(filtered.filter{it.result<0}.sumOf{it.result},account.currency),Loss);StatRow("متوسط الربح",money(a.averageWin,account.currency),Profit);StatRow("متوسط الخسارة",money(-a.averageLoss,account.currency),Loss);StatRow("أقصى تراجع",money(-a.maxDrawdown,account.currency),Loss)}
            CardBox(Modifier.padding(horizontal=16.dp)){Text("حسب الحالة النفسية",color=Text1,fontSize=17.sp,fontWeight=FontWeight.Bold);filtered.groupBy{it.mood.ifBlank{"غير مسجل"}}.entries.sortedByDescending{it.value.size}.forEach{(mood,list)->StatRow(mood,"${list.size} · ${money(list.sumOf{it.result},account.currency)}",if(list.sumOf{it.result}>=0)Profit else Loss)}}
        }
    }
}
@Composable private fun StatRow(label:String,value:String,color:Color){Row(Modifier.fillMaxWidth().padding(vertical=7.dp)){Text(label,color=Text2,modifier=Modifier.weight(1f));Text(value,color=color,fontWeight=FontWeight.Bold)}}
@Composable private fun BehaviorPanel(trades:List<Trade>){val followed=trades.count{it.strategyAdherence==Adherence.FOLLOWED};val deviations=trades.count{it.strategyAdherence==Adherence.NOT_FOLLOWED};CardBox(Modifier.padding(16.dp)){Text("تحليل السلوك المسجل",color=Text1,fontSize=18.sp,fontWeight=FontWeight.Bold);Text("هذا التحليل يصف ما سجلته عن قراراتك ولا يشخص السبب.",color=Text2,fontSize=12.sp,modifier=Modifier.padding(vertical=8.dp));StatRow("الالتزام بالخطة",followed.toString(),Profit);StatRow("الخروج عن الخطة",deviations.toString(),Warn);StatRow("تغطية المزاج","${trades.count{it.mood.isNotBlank()}} / ${trades.size}",Accent);trades.flatMap{it.executionReviewKeys}.groupingBy{it}.eachCount().entries.sortedByDescending{it.value}.take(8).forEach{StatRow(it.key,it.value.toString(),Loss)};AnimatedEmoji(R.drawable.emoji_hesitation_partial_size)}}
